package com.itu.socialcom.demo.socialmedia.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.socialmedia.dto.ManagedEntity;
import com.itu.socialcom.demo.socialmedia.dto.ManagedPageWithToken;
import com.itu.socialcom.demo.socialmedia.entity.AccessToken;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import com.itu.socialcom.demo.socialmedia.entity.RefreshToken;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageRepository;
import com.itu.socialcom.demo.socialmedia.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Service
public class FacebookAuth implements AuthService {
    @Value("${facebook.appId}")
    String APP_ID;
    @Value("${facebook.redirect_uri}")
    String redirect_uri;
    @Value("${facebook.secret}")
    String APP_SECRET;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    ManagedPageRepository managedPageRepository;
    @Autowired
    CacheV1 cacheV1;
    @Value("${facebook.token.expiration}")
    int expiration;
    @Autowired
    TokenV2ServiceImpl tokenService;

    private final okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient();
    private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
    private String currentUserAccessToken;

    @Override
    public String exchangeForAccessToken(String code) {
        try {
            String tokenUrl = String.format(
                    "https://graph.facebook.com/v20.0/oauth/access_token?" +
                            "client_id=%s&redirect_uri=%s&client_secret=%s&code=%s&state=%s",
                    APP_ID,
                    URLEncoder.encode(redirect_uri, StandardCharsets.UTF_8),
                    APP_SECRET,
                    code,
                    APP_SECRET
            );
            Request request = new Request.Builder().url(tokenUrl).build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    throw new RuntimeException("Failed to exchange code: " + response.message() + " - " + errorBody);
                }
                String responseBody = response.body().string();
                JsonNode node = mapper.readTree(responseBody);
                String accessToken = node.get("access_token").asText();
                // Store the user access token for subsequent calls to getManagedPages
                this.currentUserAccessToken = accessToken;
                return accessToken;
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception while exchanging code for access token", e);
        }
    }
    @Override
    public String getLoginUrl() {
        String scopes = "pages_show_list,pages_manage_posts,pages_read_engagement,pages_manage_metadata";
        String url = String.format(
                "https://www.facebook.com/v20.0/dialog/oauth?client_id=%s&redirect_uri=%s&scope=%s&response_type=code",
                APP_ID,
                URLEncoder.encode(redirect_uri, StandardCharsets.UTF_8),
                scopes
        );
        return url;
    }

    @Override
    @Transactional
    public List<ManagedPageWithToken> getManagedPages() throws Exception {
        if (this.currentUserAccessToken == null || this.currentUserAccessToken.isEmpty()) {
            throw new IllegalStateException("User Access Token is not available. Please call exchangeForAccessToken first.");
        }

        List<ManagedPageWithToken> managedPages = new ArrayList<>();
        String accountsUrl = String.format(
                "https://graph.facebook.com/v20.0/me/accounts?fields=id,name,picture.type(large),link,access_token&access_token=%s",
                this.currentUserAccessToken
        );

        Request request = new Request.Builder().url(accountsUrl).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                throw new RuntimeException("Failed to get managed pages: " + response.message() + " - " + errorBody);
            }
            String responseBody = response.body().string();
            JsonNode rootNode = mapper.readTree(responseBody);
            JsonNode dataNode = rootNode.get("data");

            if (dataNode != null && dataNode.isArray()) {
                for (JsonNode pageNode : dataNode) {
                    String pageId = pageNode.get("id").asText();
                    String pageName = pageNode.get("name").asText();
                    String pageLink = pageNode.has("link") ? pageNode.get("link").asText() : "";
                    String pageAccessToken = pageNode.get("access_token").asText();
                    String profilePictureUrl = "";
                    if (pageNode.has("picture") && pageNode.get("picture").has("data") && pageNode.get("picture").get("data").has("url")) {
                        profilePictureUrl = pageNode.get("picture").get("data").get("url").asText();
                    }
                    ManagedPageWithToken managedPageWithToken = new ManagedPageWithToken();
                    ManagedPage managedPage = new ManagedPage();
                    managedPage.setPlatformIdentifier(pageId);
                    managedPage.setPageTitle(pageName);
                    managedPage.setAssociatedMedia(profilePictureUrl);
                    managedPage.setLinkToPlatform(pageLink);
                    managedPage.setPlatformId(1L);
                    managedPageWithToken.setManagedPage(managedPage);
                    managedPageWithToken.setPageRefreshToken(pageAccessToken);
                    managedPages.add(managedPageWithToken);
                }
            }
        }
        return managedPages;
    }

    @Override
    @Transactional
    public List<ManagedPage> savePages(String tempUUID,String userToken) throws Exception {
        Seller seller = tokenService.findSellerByToken(userToken).orElse(null);
        if (seller == null) {
            throw new IllegalArgumentException("User not found");
        }
        List<ManagedPageWithToken> managedPageWithTokens = cacheV1.getManagedPages(tempUUID);
        System.out.println("managedPageWithTokens: " + managedPageWithTokens.get(0).getPageRefreshToken());
        List<ManagedPage> managedPages = new ArrayList<>();

        for (ManagedPageWithToken managedPageWithToken : managedPageWithTokens) {
            ManagedPage managedPage = managedPageWithToken.getManagedPage();
            managedPage.setSellerId(seller.getId());
            managedPages.add(managedPage);
            managedPageRepository.save(managedPage);
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setRefreshToken(managedPageWithToken.getPageRefreshToken());
            refreshToken.setManagedPageId(managedPage.getId());
            refreshToken.setCreatedAt(managedPage.getCreatedAt());
            refreshToken.setExpirationDate(refreshToken.getCreatedAt().plusDays(expiration));
            refreshTokenRepository.save(refreshToken);
        }
        return managedPages;
    }
}
