package com.itu.socialcom.demo.socialmedia.service;

import com.facebook.ads.sdk.APIContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.socialmedia.dto.ManagedPageWithToken;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageRepository;
import com.itu.socialcom.demo.socialmedia.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InstagramAuth implements AuthService{
    @Value("${instagram.appId}")
    String APP_ID;
    @Value("${instagram.redirect_uri}")
    String redirect_uri;
    @Value("${instagram.secret}")
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

    @Override
    public String exchangeForAccessToken(Map<String,String> params) throws IOException {
        String code = params.get("code");
        // 1️⃣ Échange du code pour un token court
        URL tokenUrl = new URL("https://api.instagram.com/oauth/access_token");
        HttpURLConnection conn = (HttpURLConnection) tokenUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        String postData = "client_id=" + URLEncoder.encode(APP_ID, "UTF-8") +
                "&client_secret=" + URLEncoder.encode(APP_SECRET, "UTF-8") +
                "&redirect_uri=" + URLEncoder.encode(redirect_uri, "UTF-8") +
                "&grant_type=authorization_code" +
                "&code=" + URLEncoder.encode(code, "UTF-8");
        try (OutputStream os = conn.getOutputStream()) {
            os.write(postData.getBytes(StandardCharsets.UTF_8));
        }
        if (conn.getResponseCode() >= 300) {
            String err = new BufferedReader(new InputStreamReader(conn.getErrorStream()))
                    .lines().collect(Collectors.joining("\n"));
            throw new IOException("Error exchanging code: " + err);
        }
        String response = new BufferedReader(new InputStreamReader(conn.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        JsonObject jsonShort = JsonParser.parseString(response).getAsJsonObject();
        String shortToken = jsonShort.get("access_token").getAsString();
//
        System.out.println(shortToken);
//
        String query = String.format(
                "grant_type=ig_exchange_token&client_secret=%s&access_token=%s",
                URLEncoder.encode(APP_SECRET, "UTF-8"),
                URLEncoder.encode(shortToken, "UTF-8")
        );
        URL llUrl = new URL("https://graph.instagram.com/access_token?" + query);
        HttpURLConnection conn2 = (HttpURLConnection) llUrl.openConnection();
        conn2.setRequestMethod("GET");  // Correct method for this endpoint


        if (conn2.getResponseCode() >= 300) {
            String err = new BufferedReader(new InputStreamReader(conn2.getErrorStream()))
                    .lines().collect(Collectors.joining("\n"));
            throw new IOException("Error exchanging long-lived token: " + err);
        }

        String longResponse = new BufferedReader(new InputStreamReader(conn2.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        JsonObject jsonLong = JsonParser.parseString(longResponse).getAsJsonObject();
        return jsonLong.get("access_token").getAsString();
    }
/*
    @Override
    public String exchangeForAccessToken(String code) {
        return "";
    }*/

    @Override
    public String getLoginUrl() {
        String scope = "instagram_business_basic,instagram_business_manage_messages,instagram_business_manage_comments,instagram_business_content_publish,instagram_business_manage_insights";
        String url = String.format(
                "https://www.instagram.com/oauth/authorize?force_reauth=true" +
                "&client_id=%s&redirect_uri=%s&response_type=code&scope=%s",
                APP_ID,redirect_uri,scope);
        return url;
    }

    @Override
    public List<ManagedPageWithToken> getManagedPages() throws Exception {

        return List.of();
    }

    @Override
    public List<ManagedPage> savePages(String entityId, String userToken) throws Exception {
        return List.of();
    }
}
