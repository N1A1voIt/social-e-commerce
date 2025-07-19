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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * X (Twitter) OAuth strategy implementation.
 * Handles X-specific OAuth 2.0 flow with PKCE and API v2 integration.
 */
@Component("x")
public class XOAuthStrategy implements OAuthStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(XOAuthStrategy.class);
    private static final String PLATFORM_NAME = "x";
    
    private final OAuthConfig oAuthConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // Store PKCE code verifiers temporarily (in production, use Redis or database)
    private final ConcurrentHashMap<String, String> codeVerifiers = new ConcurrentHashMap<>();
    
    public XOAuthStrategy(OAuthConfig oAuthConfig, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.oAuthConfig = oAuthConfig;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public String getAuthorizationUrl(String state) {
        OAuthConfig.PlatformConfig config = getPlatformConfig();
        
        // Generate PKCE code verifier and challenge
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        
        // Store code verifier for later use in token exchange
        codeVerifiers.put(state, codeVerifier);
        
        return UriComponentsBuilder.fromHttpUrl(config.getAuthorizationUrl())
                .queryParam("response_type", "code")
                .queryParam("client_id", config.getClientId())
                .queryParam("redirect_uri", config.getRedirectUri())
                .queryParam("scope", String.join(" ", config.getScopes()))
                .queryParam("state", state)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .build()
                .toUriString();
    }
    
    @Override
    public OAuthTokenResponse exchangeCodeForTokens(String code, String state) {
        OAuthConfig.PlatformConfig config = getPlatformConfig();
        
        // Retrieve stored code verifier
        String codeVerifier = codeVerifiers.remove(state);
        if (codeVerifier == null) {
            throw new InvalidAuthorizationCodeException(PLATFORM_NAME, "INVALID_STATE", 
                "Invalid state parameter or code verifier not found");
        }
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(config.getClientId(), config.getClientSecret());
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("grant_type", "authorization_code");
            params.add("client_id", config.getClientId());
            params.add("redirect_uri", config.getRedirectUri());
            params.add("code_verifier", codeVerifier);
            
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
            logger.error("X token exchange failed: {}", e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new InvalidAuthorizationCodeException(PLATFORM_NAME, "INVALID_CODE", 
                    "Invalid authorization code provided");
            }
            throw new OAuthException(PLATFORM_NAME, "TOKEN_EXCHANGE_ERROR", 
                "Error during token exchange: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during X token exchange", e);
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
            headers.setBasicAuth(config.getClientId(), config.getClientSecret());
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("refresh_token", refreshToken);
            params.add("grant_type", "refresh_token");
            params.add("client_id", config.getClientId());
            
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
            logger.error("X token refresh failed: {}", e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST || e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new TokenExpiredException(PLATFORM_NAME, "INVALID_REFRESH_TOKEN", 
                    "Refresh token is invalid or expired");
            }
            throw new OAuthException(PLATFORM_NAME, "REFRESH_ERROR", 
                "Error during token refresh: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during X token refresh", e);
            throw new OAuthException(PLATFORM_NAME, "UNEXPECTED_ERROR", 
                "Unexpected error during token refresh: " + e.getMessage());
        }
    }
    
    @Override
    public List<SocialMediaPage> getUserPages(String accessToken) {
        OAuthConfig.PlatformConfig config = getPlatformConfig();
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            
            String url = UriComponentsBuilder.fromHttpUrl(config.getUserInfoUrl())
                    .queryParam("user.fields", "id,name,username,profile_image_url,public_metrics,verified")
                    .build()
                    .toUriString();
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return parseUserResponse(response.getBody(), accessToken);
            } else {
                throw new OAuthException(PLATFORM_NAME, "USER_INFO_FETCH_FAILED", 
                    "Failed to fetch user info: " + response.getStatusCode());
            }
            
        } catch (HttpClientErrorException e) {
            logger.error("X user info fetch failed: {}", e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new TokenExpiredException(PLATFORM_NAME, "TOKEN_EXPIRED", 
                    "Access token is invalid or expired");
            }
            throw new OAuthException(PLATFORM_NAME, "USER_INFO_FETCH_ERROR", 
                "Error fetching user info: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during X user info fetch", e);
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
            headers.setBasicAuth(config.getClientId(), config.getClientSecret());
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("token", accessToken);
            params.add("client_id", config.getClientId());
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                config.getRevokeUrl(), request, String.class);
            
            if (response.getStatusCode() != HttpStatus.OK) {
                logger.warn("X access revocation returned status: {}", response.getStatusCode());
            }
            
        } catch (HttpClientErrorException e) {
            logger.warn("X access revocation failed: {}", e.getResponseBodyAsString());
            // Don't throw exception for revocation failures as the token might already be invalid
        } catch (Exception e) {
            logger.warn("Unexpected error during X access revocation", e);
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
                "X OAuth configuration is missing");
        }
        return config;
    }
    
    private String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }
    
    private String generateCodeChallenge(String codeVerifier) {
        try {
            byte[] bytes = codeVerifier.getBytes(StandardCharsets.US_ASCII);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(bytes, 0, bytes.length);
            byte[] digest = messageDigest.digest();
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new OAuthException(PLATFORM_NAME, "PKCE_ERROR", 
                "Failed to generate PKCE code challenge: " + e.getMessage());
        }
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
            
            if (jsonNode.has("scope")) {
                String scopeString = jsonNode.get("scope").asText();
                List<String> scopes = List.of(scopeString.split(" "));
                tokenResponse.setScopes(scopes);
            } else {
                // Use configured scopes if not returned
                OAuthConfig.PlatformConfig config = getPlatformConfig();
                tokenResponse.setScopes(config.getScopes());
            }
            
            return tokenResponse;
            
        } catch (Exception e) {
            logger.error("Failed to parse X token response: {}", responseBody, e);
            throw new OAuthException(PLATFORM_NAME, "PARSE_ERROR", 
                "Failed to parse token response: " + e.getMessage());
        }
    }
    
    private List<SocialMediaPage> parseUserResponse(String responseBody, String accessToken) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            List<SocialMediaPage> pages = new ArrayList<>();
            
            if (jsonNode.has("data")) {
                JsonNode userData = jsonNode.get("data");
                
                // X API returns user info, not pages like Facebook
                // We create a single "page" representing the user's X account
                SocialMediaPage page = new SocialMediaPage();
                page.setId(userData.get("id").asText());
                page.setPlatform(PLATFORM_NAME);
                page.setAccessToken(accessToken);
                
                if (userData.has("name")) {
                    page.setName(userData.get("name").asText());
                }
                
                if (userData.has("username")) {
                    // Use username as category or additional info
                    page.setCategory("@" + userData.get("username").asText());
                }
                
                if (userData.has("profile_image_url")) {
                    page.setProfilePictureUrl(userData.get("profile_image_url").asText());
                }
                
                // Set permissions based on configured scopes
                OAuthConfig.PlatformConfig config = getPlatformConfig();
                page.setPermissions(config.getScopes());
                
                pages.add(page);
            }
            
            return pages;
            
        } catch (Exception e) {
            logger.error("Failed to parse X user response: {}", responseBody, e);
            throw new OAuthException(PLATFORM_NAME, "PARSE_ERROR", 
                "Failed to parse user response: " + e.getMessage());
        }
    }
}