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
 * Facebook OAuth strategy implementation.
 * Handles Facebook-specific OAuth 2.0 flow and Pages API integration.
 */
@Component("facebook")
public class FacebookOAuthStrategy implements OAuthStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(FacebookOAuthStrategy.class);
    private static final String PLATFORM_NAME = "facebook";
    
    private final OAuthConfig oAuthConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public FacebookOAuthStrategy(OAuthConfig oAuthConfig, RestTemplate restTemplate, ObjectMapper objectMapper) {
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
            logger.error("Facebook token exchange failed: {}", e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new InvalidAuthorizationCodeException(PLATFORM_NAME, "INVALID_CODE", 
                    "Invalid authorization code provided");
            }
            throw new OAuthException(PLATFORM_NAME, "TOKEN_EXCHANGE_ERROR", 
                "Error during token exchange: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during Facebook token exchange", e);
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
            params.add("client_id", config.getClientId());
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
            logger.error("Facebook token refresh failed: {}", e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new TokenExpiredException(PLATFORM_NAME, "INVALID_REFRESH_TOKEN", 
                    "Refresh token is invalid or expired");
            }
            throw new OAuthException(PLATFORM_NAME, "REFRESH_ERROR", 
                "Error during token refresh: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during Facebook token refresh", e);
            throw new OAuthException(PLATFORM_NAME, "UNEXPECTED_ERROR", 
                "Unexpected error during token refresh: " + e.getMessage());
        }
    }
    
    @Override
    public List<SocialMediaPage> getUserPages(String accessToken) {
        OAuthConfig.PlatformConfig config = getPlatformConfig();
        
        try {
            String url = UriComponentsBuilder.fromHttpUrl(config.getUserInfoUrl())
                    .queryParam("access_token", accessToken)
                    .queryParam("fields", "id,name,category,picture,access_token,perms")
                    .build()
                    .toUriString();
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return parsePagesResponse(response.getBody());
            } else {
                throw new OAuthException(PLATFORM_NAME, "PAGES_FETCH_FAILED", 
                    "Failed to fetch user pages: " + response.getStatusCode());
            }
            
        } catch (HttpClientErrorException e) {
            logger.error("Facebook pages fetch failed: {}", e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new TokenExpiredException(PLATFORM_NAME, "TOKEN_EXPIRED", 
                    "Access token is invalid or expired");
            }
            throw new OAuthException(PLATFORM_NAME, "PAGES_FETCH_ERROR", 
                "Error fetching user pages: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during Facebook pages fetch", e);
            throw new OAuthException(PLATFORM_NAME, "UNEXPECTED_ERROR", 
                "Unexpected error fetching pages: " + e.getMessage());
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
            
            ResponseEntity<String> response = restTemplate.exchange(
                config.getRevokeUrl(), HttpMethod.DELETE, request, String.class);
            
            if (response.getStatusCode() != HttpStatus.OK) {
                logger.warn("Facebook access revocation returned status: {}", response.getStatusCode());
            }
            
        } catch (HttpClientErrorException e) {
            logger.warn("Facebook access revocation failed: {}", e.getResponseBodyAsString());
            // Don't throw exception for revocation failures as the token might already be invalid
        } catch (Exception e) {
            logger.warn("Unexpected error during Facebook access revocation", e);
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
                "Facebook OAuth configuration is missing");
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
            
            if (jsonNode.has("refresh_token")) {
                tokenResponse.setRefreshToken(jsonNode.get("refresh_token").asText());
            }
            
            // Facebook doesn't typically return scopes in token response, use configured scopes
            OAuthConfig.PlatformConfig config = getPlatformConfig();
            tokenResponse.setScopes(config.getScopes());
            
            return tokenResponse;
            
        } catch (Exception e) {
            logger.error("Failed to parse Facebook token response: {}", responseBody, e);
            throw new OAuthException(PLATFORM_NAME, "PARSE_ERROR", 
                "Failed to parse token response: " + e.getMessage());
        }
    }
    
    private List<SocialMediaPage> parsePagesResponse(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            List<SocialMediaPage> pages = new ArrayList<>();
            
            if (jsonNode.has("data")) {
                JsonNode dataNode = jsonNode.get("data");
                for (JsonNode pageNode : dataNode) {
                    SocialMediaPage page = new SocialMediaPage();
                    page.setId(pageNode.get("id").asText());
                    page.setName(pageNode.get("name").asText());
                    page.setPlatform(PLATFORM_NAME);
                    
                    if (pageNode.has("category")) {
                        page.setCategory(pageNode.get("category").asText());
                    }
                    
                    if (pageNode.has("picture") && pageNode.get("picture").has("data") 
                        && pageNode.get("picture").get("data").has("url")) {
                        page.setProfilePictureUrl(pageNode.get("picture").get("data").get("url").asText());
                    }
                    
                    if (pageNode.has("access_token")) {
                        page.setAccessToken(pageNode.get("access_token").asText());
                    }
                    
                    if (pageNode.has("perms")) {
                        List<String> permissions = new ArrayList<>();
                        for (JsonNode permNode : pageNode.get("perms")) {
                            permissions.add(permNode.asText());
                        }
                        page.setPermissions(permissions);
                    }
                    
                    pages.add(page);
                }
            }
            
            return pages;
            
        } catch (Exception e) {
            logger.error("Failed to parse Facebook pages response: {}", responseBody, e);
            throw new OAuthException(PLATFORM_NAME, "PARSE_ERROR", 
                "Failed to parse pages response: " + e.getMessage());
        }
    }
}