package com.itu.socialcom.demo.socialmedia.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.socialmedia.dto.ManagedPageWithToken;
import com.itu.socialcom.demo.socialmedia.entity.AccessToken;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import com.itu.socialcom.demo.socialmedia.entity.RefreshToken;
import com.itu.socialcom.demo.socialmedia.repository.AccessTokenRepository;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageRepository;
import com.itu.socialcom.demo.socialmedia.repository.RefreshTokenRepository;
import okhttp3.*;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class XAuth implements AuthService{
    @Value("${x.auth_id}")
    String clientId;
    @Value("${x.redirect_uri}")
    String redirectUri;
    @Value("${x.clentsecret}")
    String clientSecret;
    @Value("${x.scopes}")
    String scopes;
    @Autowired
    TokenV2ServiceImpl tokenService;
    @Autowired
    ManagedPageRepository managedPageRepository;
    @Autowired
    CacheV1 cacheV1;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    AccessTokenRepository accessTokenRepository;
    private final Map<String, String> pkceStore = new ConcurrentHashMap<>();
    private final okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient();
    private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
    private String currentUserAccessToken;
    private String currentUserRefreshToken;
    @Override
    public String exchangeForAccessToken(Map<String,String> params) throws IOException {
        // 1. Correctly create the Basic Auth header value.
        String passedState = params.get("state");
        String code = params.get("code");
        String credentials = clientId + ":" + clientSecret;
        String basicAuth = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
        String verifier = pkceStore.remove(passedState);
        if (verifier == null) throw new RuntimeException("PKCE verifier missing/expired");

        // 2. Move parameters from the URL to a FormBody for the POST request.
        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", redirectUri)
                .add("client_id", clientId)
                .add("code_verifier", verifier) // This is your PKCE code verifier
                .build();

        // 3. The token URL should not contain query parameters.
        String tokenUrl = "https://api.x.com/2/oauth2/token";

        Request request = new Request.Builder()
                .url(tokenUrl)
                .post(formBody)
                .header("Authorization", basicAuth)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                throw new RuntimeException("Token exchange failed: " + response.message() + " - " + errorBody);
            }
            JsonNode node = mapper.readTree(response.body().string());
            String accessToken = node.get("access_token").asText();
            String refreshToken = node.get("refresh_token").asText();
            this.currentUserAccessToken = accessToken;
            this.currentUserRefreshToken = refreshToken;
            return accessToken;
        }
    }
    private String generateCodeVerifier() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String generateCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }
    @Override
    public String getLoginUrl() {
        try {
            String state = generateState();
            String verifier = generateCodeVerifier(); // Base64-url random 32 bytes, 43+ chars
            String challenge = generateCodeChallenge(verifier); // SHA-256 + base64-url

            pkceStore.put(state, verifier);

            return String.format(
                    "https://x.com/i/oauth2/authorize?response_type=code&client_id=%s&redirect_uri=%s&scope=%s&state=%s&code_challenge=%s&code_challenge_method=S256",
                    clientId, redirectUri, scopes, state, challenge);
        } catch (Exception e) {
            throw new RuntimeException("Error generating login URL", e);
        }
    }
    private static String generateState() {
        byte[] stateBytes = new byte[32];
        new SecureRandom().nextBytes(stateBytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(stateBytes);
    }

    @Override
    public List<ManagedPageWithToken> getManagedPages() throws Exception {
        if (this.currentUserAccessToken == null) {
            throw new IllegalStateException("User Access Token is not available. Please call exchangeForAccessToken first.");
        }

        String url = "https://api.x.com/2/users/me?user.fields=id,name,username,profile_image_url";
        Request req = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + this.currentUserAccessToken)
                .build();
        List<ManagedPageWithToken> managedPageWithTokens = new ArrayList<>();
        try (Response resp = httpClient.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                String body = resp.body() != null ? resp.body().string() : "No body";
                throw new RuntimeException("Failed to get user: " + resp.message() + " - " + body);
            }

            JsonNode data = mapper.readTree(resp.body().string()).get("data");
            ManagedPageWithToken mp = new ManagedPageWithToken();
            ManagedPage p = new ManagedPage();

            p.setPlatformIdentifier(data.get("id").asText());
            p.setPageTitle(data.get("name").asText());
            p.setAssociatedMedia(data.has("profile_image_url") ? data.get("profile_image_url").asText() : "");
            p.setLinkToPlatform("https://x.com/" + data.get("username").asText());
            p.setPlatformId(3L);

            mp.setManagedPage(p);
            mp.setPageRefreshToken(this.currentUserRefreshToken);
            mp.setPageAccessToken(this.currentUserAccessToken);

            managedPageWithTokens.add(mp);

            return managedPageWithTokens;
        }
    }

    @Override
    @Transactional
    public List<ManagedPage> savePages(String tempUUID, String userToken) throws Exception {
        Seller seller = tokenService.findSellerByToken(userToken).orElse(null);
        if (seller == null) {
            throw new IllegalArgumentException("User not found");
        }
        List<ManagedPage> actualPages = managedPageRepository.findBySellerAndPlatform(seller.getId(),1L);
        List<ManagedPageWithToken> managedPageWithTokens = cacheV1.getManagedPages(tempUUID);
        HashMap<String,ManagedPage> hashMap = new HashMap<>();
        for (int i = 0; i < actualPages.size(); i++) {
            hashMap.put(actualPages.get(i).getPlatformIdentifier()+"-"+actualPages.get(i).getSellerId(),actualPages.get(i));
        }
        List<ManagedPage> managedPages = new ArrayList<>();
        for (ManagedPageWithToken managedPageWithToken : managedPageWithTokens) {
            ManagedPage managedPage = managedPageWithToken.getManagedPage();
            Long managedPageId;
            if (!hashMap.containsKey(managedPage.getPlatformIdentifier()+"-"+seller.getId())) {
                managedPage.setSellerId(seller.getId());
                managedPages.add(managedPage);
                managedPageRepository.save(managedPage);
                managedPageId = managedPage.getId();
            } else managedPageId = hashMap.get(managedPage.getPlatformIdentifier()+"-"+seller.getId()).getId();
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setRefreshToken(managedPageWithToken.getPageRefreshToken());
            refreshToken.setManagedPageId(managedPageId);
            refreshToken.setCreatedAt(LocalDateTime.now());
            refreshToken.setExpirationDate(refreshToken.getCreatedAt().plusMonths(6));
            refreshTokenRepository.save(refreshToken);
            AccessToken accessToken = new AccessToken();
            accessToken.setAccessToken(managedPageWithToken.getPageAccessToken());
            accessToken.setCreatedAt(LocalDateTime.now());
            accessToken.setExpirationDate(accessToken.getCreatedAt().plusMinutes(120));
            accessToken.setIdRefreshToken(refreshToken.getId());
            accessTokenRepository.save(accessToken);
        }
        return managedPages;
    }
}
