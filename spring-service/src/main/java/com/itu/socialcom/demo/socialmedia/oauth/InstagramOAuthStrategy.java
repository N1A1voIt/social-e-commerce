package com.itu.socialcom.demo.socialmedia.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itu.socialcom.demo.socialmedia.config.OAuthConfig;
import com.itu.socialcom.demo.socialmedia.dto.OAuthTokenResponse;
import com.itu.socialcom.demo.socialmedia.dto.SocialMediaPage;
import com.itu.socialcom.demo.socialmedia.exception.InvalidAuthorizationCodeException;
import com.itu.socialcom.demo.socialmedia.exception.OAuthException;
import com.itu.socialcom.demo.socialmedia.exception.TokenExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Instagram OAuth strategy implementation.
 * Handles Instagram-specific OAuth 2.0 flow and Basic Display API integration.
 */
@Component("instagram")
public class InstagramOAuthStrategy implements OAuthStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(InstagramOAuthStrategy.class);
    private static final String PLATFORM_NAME = "instagram";
    
    private final OAuthConfig oAuthConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public InstagramOAuthStrategy(OAuthConfig oAuthConfig, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.oAuthConfig = oAuthConfig;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public String getAuthorizationUrl(String state) {
        OAuthConfig.PlatformConfig config = getPlatformConfig();
        
        return UriComponentsBuilder.fromHttpUrl(config.getAuthorizationUrl())
                .queryParam("client_id", config.getClientId())
                .queryParam("redirect_uri", config.getRedirectUri())
                .queryParam("scope", String.join(",", config.getScopes()))
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .build()
                .toUriString();
    }
    
    @Override
    public OAuthTokenResponse exchangeCodeForTokens(String code, String state) {
        OAuthConfig.PlatformConfig config = getPlatformConfig();
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", config.getClientId());
            params.add("client_secret", config.getClientSecret());
            params.add("grant_type", "authorization_code");
            params.add("redirect_uri", config.getRedirectUri());
            params.add("code", code);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                config.getTokenUrl(), request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return parseTokenResponse(response.getBody());
            } else {
                throw new OAuthException(PLATFORM_NAME, "TOKEN_EXCHANGE_FAILED", 
                    "Failed to exchange code for tokens: " + response.getStatusCode());
            }
            
        } catch (HttpClientErrorException e) {
            logger.error("Instagram token exchange failed: {}", e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new InvalidAuthorizationCodeException(PLATFORM_NAME, "INVALID_CODE", 
                    "Invalid authorization code provided");
            }
            throw new OAuthException(PLATFORM_NAME, "TOKEN_EXCHANGE_ERROR", 
                "Error during token exchange: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during Instagram token exchange", e);
            throw new OAuthException(PLATFORM_NAME, "UNEXPECTED_ERROR", 
                "Unexpected error during token exchange: " + e.getMessage());
        }
    }
    
    @Override
    public OAuthTokenResponse refreshTokens(String refreshToken) {
        OAuthConfig.PlatformConfig config = getPlatformConfig();
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "refresh_token");
            params.add("refresh_token", refreshToken);
            params.add("client_secret", config.getClientSecret());
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                config.getTokenUrl(), request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return parseTokenResponse(response.getBody());
            } else {
                throw new TokenExpiredException(PLATFORM_NAME, "REFRESH_FAILED", 
                    "Failed to refresh tokens: " + response.getStatusCode());
            }
            
        } catch (HttpClientErrorException e) {
            logger.error("Instagram token refresh failed: {}", e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new TokenExpiredException(PLATFORM_NAME, "INVALID_REFRESH_TOKEN", 
                    "Refresh token is invalid or expired");
            }
            throw new OAuthException(PLATFORM_NAME, "REFRESH_ERROR", 
                "Error during token refresh: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during Instagram token refresh", e);
            throw new OAuthException(PLATFORM_NAME, "UNEXPECTED_ERROR", 
                "Unexpected error during token refresh: " + e.getMessage());
        }
    }
    
    @Override
    public List<SocialMediaPage> getUserPages(String accessToken) {
        OAuthConfig.PlatformConfig config = getPlatformConfig();
        
        try {
            String url = UriComponentsBuilder.fromHttpUrl(config.getUserInfoUrl())
                    .queryParam("fields", "id,username,account_type,media_count")
                    .queryParam("access_token", accessToken)
                    .build()
                    .toUriString();
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return parseUserResponse(response.getBody(), accessToken);
            } else {
                throw new OAuthException(PLATFORM_NAME, "USER_INFO_FETCH_FAILED", 
                    "Failed to fetch user info: " + response.getStatusCode());
            }
            
        } catch (HttpClientErrorException e) {
            logger.error("Instagram user info fetch failed: {}", e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new TokenExpiredException(PLATFORM_NAME, "TOKEN_EXPIRED", 
                    "Access token is invalid or expired");
            }
            throw new OAuthException(PLATFORM_NAME, "USER_INFO_FETCH_ERROR", 
                "Error fetching user info: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during Instagram user info fetch", e);
            throw new OAuthException(PLATFORM_NAME, "UNEXPECTED_ERROR", 
                "Unexpected error fetching user info: " + e.getMessage());
        }
    }
    
    @Override
    public void revokeAccess(String accessToken) {
        OAuthConfig.PlatformConfig config = getPlatformConfig();
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("access_token", accessToken);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                config.getRevokeUrl(), request, String.class);
            
            if (response.getStatusCode() != HttpStatus.OK) {
                logger.warn("Instagram access revocation returned status: {}", response.getStatusCode());
            }
            
        } catch (HttpClientErrorException e) {
            logger.warn("Instagram access revocation failed: {}", e.getResponseBodyAsString());
            // Don't throw exception for revocation failures as the token might already be invalid
        } catch (Exception e) {
            logger.warn("Unexpected error during Instagram access revocation", e);
            // Don't throw exception for revocation failures
        }
    }
    
    @Override
    public String getPlatformName() {
        return PLATFORM_NAME;
    }
    
    private OAuthConfig.PlatformConfig getPlatformConfig() {
        OAuthConfig.PlatformConfig config = oAuthConfig.getPlatformConfig(PLATFORM_NAME);
        if (config == null) {
            throw new OAuthException(PLATFORM_NAME, "CONFIG_MISSING", 
                "Instagram OAuth configuration is missing");
        }
        return config;
    }
    
    private OAuthTokenResponse parseTokenResponse(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            OAuthTokenResponse tokenResponse = new OAuthTokenResponse();
            tokenResponse.setAccessToken(jsonNode.get("access_token").asText());
            tokenResponse.setTokenType(jsonNode.has("token_type") ? 
                jsonNode.get("token_type").asText() : "Bearer");
            
            if (jsonNode.has("expires_in")) {
                tokenResponse.setExpiresIn(jsonNode.get("expires_in").asLong());
            }
            
            // Instagram Basic Display API doesn't provide refresh tokens in the initial response
            // Long-lived tokens are obtained through a separate exchange process
            if (jsonNode.has("refresh_token")) {
                tokenResponse.setRefreshToken(jsonNode.get("refresh_token").asText());
            }
            
            // Use configured scopes
            OAuthConfig.PlatformConfig config = getPlatformConfig();
            tokenResponse.setScopes(config.getScopes());
            
            return tokenResponse;
            
        } catch (Exception e) {
            logger.error("Failed to parse Instagram token response: {}", responseBody, e);
            throw new OAuthException(PLATFORM_NAME, "PARSE_ERROR", 
                "Failed to parse token response: " + e.getMessage());
        }
    }
    
    private List<SocialMediaPage> parseUserResponse(String responseBody, String accessToken) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            List<SocialMediaPage> pages = new ArrayList<>();
            
            // Instagram Basic Display API returns user info, not pages like Facebook
            // We create a single "page" representing the user's Instagram account
            SocialMediaPage page = new SocialMediaPage();
            page.setId(jsonNode.get("id").asText());
            page.setPlatform(PLATFORM_NAME);
            page.setAccessToken(accessToken);
            
            if (jsonNode.has("username")) {
                page.setName(jsonNode.get("username").asText());
            }
            
            if (jsonNode.has("account_type")) {
                page.setCategory(jsonNode.get("account_type").asText());
            }
            
            // Instagram Basic Display API doesn't provide profile picture in user info
            // Would need separate call to get media and find profile picture
            
            // Set basic permissions based on configured scopes
            OAuthConfig.PlatformConfig config = getPlatformConfig();
            page.setPermissions(config.getScopes());
            
            pages.add(page);
            return pages;
            
        } catch (Exception e) {
            logger.error("Failed to parse Instagram user response: {}", responseBody, e);
            throw new OAuthException(PLATFORM_NAME, "PARSE_ERROR", 
                "Failed to parse user response: " + e.getMessage());
        }
    }
}