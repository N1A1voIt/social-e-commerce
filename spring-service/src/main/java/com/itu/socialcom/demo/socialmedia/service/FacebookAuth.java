package com.itu.socialcom.demo.socialmedia.service;

import com.itu.socialcom.demo.socialmedia.dto.ManagedEntity;
import org.springframework.beans.factory.annotation.Value;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FacebookAuth implements AuthService{
    @Value("${facebook.appId}")
    String APP_ID;
    @Value("${redirect_uri}")
    String redirect_uri;

    @Override
    public String exchangeForAccessToken() {
        return "";
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
    public List<ManagedEntity> getManagedEntities() throws Exception {
        return List.of();
    }

    @Override
    public String getEntityAccessToken(String entityId) throws Exception {
        return "";
    }
}
