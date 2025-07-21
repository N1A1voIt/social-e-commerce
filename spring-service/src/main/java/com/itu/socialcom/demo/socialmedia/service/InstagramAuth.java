package com.itu.socialcom.demo.socialmedia.service;

import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.socialmedia.dto.ManagedPageWithToken;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageRepository;
import com.itu.socialcom.demo.socialmedia.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public String exchangeForAccessToken(String code) {
        return "";
    }

    @Override
    public String getLoginUrl() {
        String scope = "instagram_business_basic,instagram_business_manage_messages,instagram_business_manage_comments,instagram_business_content_publish,instagram_business_manage_insights";
        String url = String.format(
                "force_reauth=true" +
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
