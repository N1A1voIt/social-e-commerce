package com.itu.socialcom.demo.socialmedia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration properties for OAuth platform settings.
 * Maps to oauth.* properties in application configuration files.
 */
@ConfigurationProperties(prefix = "oauth")
@Component
public class OAuthConfig {
    
    private Map<String, PlatformConfig> platforms = new HashMap<>();
    
    public Map<String, PlatformConfig> getPlatforms() {
        return platforms;
    }
    
    public void setPlatforms(Map<String, PlatformConfig> platforms) {
        this.platforms = platforms;
    }
    
    /**
     * Get configuration for a specific platform
     * @param platform Platform identifier
     * @return Platform configuration or null if not found
     */
    public PlatformConfig getPlatformConfig(String platform) {
        return platforms.get(platform.toLowerCase());
    }
    
    /**
     * Configuration for individual social media platforms
     */
    public static class PlatformConfig {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private List<String> scopes;
        private String authorizationUrl;
        private String tokenUrl;
        private String userInfoUrl;
        private String revokeUrl;
        
        public String getClientId() {
            return clientId;
        }
        
        public void setClientId(String clientId) {
            this.clientId = clientId;
        }
        
        public String getClientSecret() {
            return clientSecret;
        }
        
        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }
        
        public String getRedirectUri() {
            return redirectUri;
        }
        
        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }
        
        public List<String> getScopes() {
            return scopes;
        }
        
        public void setScopes(List<String> scopes) {
            this.scopes = scopes;
        }
        
        public String getAuthorizationUrl() {
            return authorizationUrl;
        }
        
        public void setAuthorizationUrl(String authorizationUrl) {
            this.authorizationUrl = authorizationUrl;
        }
        
        public String getTokenUrl() {
            return tokenUrl;
        }
        
        public void setTokenUrl(String tokenUrl) {
            this.tokenUrl = tokenUrl;
        }
        
        public String getUserInfoUrl() {
            return userInfoUrl;
        }
        
        public void setUserInfoUrl(String userInfoUrl) {
            this.userInfoUrl = userInfoUrl;
        }
        
        public String getRevokeUrl() {
            return revokeUrl;
        }
        
        public void setRevokeUrl(String revokeUrl) {
            this.revokeUrl = revokeUrl;
        }
        
        @Override
        public String toString() {
            return "PlatformConfig{" +
                    "clientId='" + (clientId != null ? "[REDACTED]" : null) + '\'' +
                    ", clientSecret='" + (clientSecret != null ? "[REDACTED]" : null) + '\'' +
                    ", redirectUri='" + redirectUri + '\'' +
                    ", scopes=" + scopes +
                    ", authorizationUrl='" + authorizationUrl + '\'' +
                    ", tokenUrl='" + tokenUrl + '\'' +
                    ", userInfoUrl='" + userInfoUrl + '\'' +
                    ", revokeUrl='" + revokeUrl + '\'' +
                    '}';
        }
    }
}