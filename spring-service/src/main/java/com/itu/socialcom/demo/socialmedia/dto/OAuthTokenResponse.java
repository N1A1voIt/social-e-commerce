package com.itu.socialcom.demo.socialmedia.dto;

import java.util.List;

/**
 * DTO representing OAuth token response from social media platforms
 */
public class OAuthTokenResponse {
    
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String tokenType;
    private List<String> scopes;
    
    public OAuthTokenResponse() {}
    
    public OAuthTokenResponse(String accessToken, String refreshToken, Long expiresIn, String tokenType, List<String> scopes) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.tokenType = tokenType;
        this.scopes = scopes;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public List<String> getScopes() {
        return scopes;
    }
    
    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }
    
    @Override
    public String toString() {
        return "OAuthTokenResponse{" +
                "accessToken='" + (accessToken != null ? "[REDACTED]" : null) + '\'' +
                ", refreshToken='" + (refreshToken != null ? "[REDACTED]" : null) + '\'' +
                ", expiresIn=" + expiresIn +
                ", tokenType='" + tokenType + '\'' +
                ", scopes=" + scopes +
                '}';
    }
}