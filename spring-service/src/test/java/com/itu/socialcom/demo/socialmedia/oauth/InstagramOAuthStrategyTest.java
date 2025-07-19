package com.itu.socialcom.demo.socialmedia.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itu.socialcom.demo.socialmedia.config.OAuthConfig;
import com.itu.socialcom.demo.socialmedia.dto.OAuthTokenResponse;
import com.itu.socialcom.demo.socialmedia.dto.SocialMediaPage;
import com.itu.socialcom.demo.socialmedia.exception.InvalidAuthorizationCodeException;
import com.itu.socialcom.demo.socialmedia.exception.OAuthException;
import com.itu.socialcom.demo.socialmedia.exception.TokenExpiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InstagramOAuthStrategyTest {

    @Mock
    private OAuthConfig oAuthConfig;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private InstagramOAuthStrategy instagramStrategy;
    private OAuthConfig.PlatformConfig platformConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        platformConfig = new OAuthConfig.PlatformConfig();
        platformConfig.setClientId("test-client-id");
        platformConfig.setClientSecret("test-client-secret");
        platformConfig.setRedirectUri("http://localhost:8080/api/oauth/instagram/callback");
        platformConfig.setScopes(Arrays.asList("instagram_basic", "instagram_content_publish"));
        platformConfig.setAuthorizationUrl("https://api.instagram.com/oauth/authorize");
        platformConfig.setTokenUrl("https://api.instagram.com/oauth/access_token");
        platformConfig.setUserInfoUrl("https://graph.instagram.com/me");
        platformConfig.setRevokeUrl("https://graph.instagram.com/oauth/revoke");
        
        when(oAuthConfig.getPlatformConfig("instagram")).thenReturn(platformConfig);
        
        instagramStrategy = new InstagramOAuthStrategy(oAuthConfig, restTemplate, objectMapper);
    }

    @Test
    void testGetPlatformName() {
        assertEquals("instagram", instagramStrategy.getPlatformName());
    }

    @Test
    void testGetAuthorizationUrl() {
        String state = "test-state-123";
        String authUrl = instagramStrategy.getAuthorizationUrl(state);
        
        assertNotNull(authUrl);
        assertTrue(authUrl.contains("client_id=test-client-id"));
        assertTrue(authUrl.contains("state=test-state-123"));
        assertTrue(authUrl.contains("scope=instagram_basic,instagram_content_publish"));
        assertTrue(authUrl.contains("response_type=code"));
        assertTrue(authUrl.startsWith("https://api.instagram.com/oauth/authorize"));
    }

    @Test
    void testExchangeCodeForTokens_Success() throws Exception {
        String code = "test-auth-code";
        String state = "test-state";
        String tokenResponseJson = "{\"access_token\":\"test-access-token\",\"token_type\":\"Bearer\",\"expires_in\":3600}";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>(tokenResponseJson, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(mockResponse);
        
        // Mock ObjectMapper parsing
        com.fasterxml.jackson.databind.JsonNode mockJsonNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockAccessTokenNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockTokenTypeNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockExpiresInNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        
        when(objectMapper.readTree(tokenResponseJson)).thenReturn(mockJsonNode);
        when(mockJsonNode.get("access_token")).thenReturn(mockAccessTokenNode);
        when(mockAccessTokenNode.asText()).thenReturn("test-access-token");
        when(mockJsonNode.has("token_type")).thenReturn(true);
        when(mockJsonNode.get("token_type")).thenReturn(mockTokenTypeNode);
        when(mockTokenTypeNode.asText()).thenReturn("Bearer");
        when(mockJsonNode.has("expires_in")).thenReturn(true);
        when(mockJsonNode.get("expires_in")).thenReturn(mockExpiresInNode);
        when(mockExpiresInNode.asLong()).thenReturn(3600L);
        when(mockJsonNode.has("refresh_token")).thenReturn(false);
        
        OAuthTokenResponse result = instagramStrategy.exchangeCodeForTokens(code, state);
        
        assertNotNull(result);
        assertEquals("test-access-token", result.getAccessToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(3600L, result.getExpiresIn());
        assertEquals(platformConfig.getScopes(), result.getScopes());
    }

    @Test
    void testExchangeCodeForTokens_InvalidCode() {
        String code = "invalid-code";
        String state = "test-state";
        
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));
        
        assertThrows(InvalidAuthorizationCodeException.class, () -> {
            instagramStrategy.exchangeCodeForTokens(code, state);
        });
    }

    @Test
    void testRefreshTokens_Success() throws Exception {
        String refreshToken = "test-refresh-token";
        String tokenResponseJson = "{\"access_token\":\"new-access-token\",\"token_type\":\"Bearer\",\"expires_in\":3600}";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>(tokenResponseJson, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(mockResponse);
        
        // Mock ObjectMapper parsing
        com.fasterxml.jackson.databind.JsonNode mockJsonNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockAccessTokenNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockTokenTypeNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockExpiresInNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        
        when(objectMapper.readTree(tokenResponseJson)).thenReturn(mockJsonNode);
        when(mockJsonNode.get("access_token")).thenReturn(mockAccessTokenNode);
        when(mockAccessTokenNode.asText()).thenReturn("new-access-token");
        when(mockJsonNode.has("token_type")).thenReturn(true);
        when(mockJsonNode.get("token_type")).thenReturn(mockTokenTypeNode);
        when(mockTokenTypeNode.asText()).thenReturn("Bearer");
        when(mockJsonNode.has("expires_in")).thenReturn(true);
        when(mockJsonNode.get("expires_in")).thenReturn(mockExpiresInNode);
        when(mockExpiresInNode.asLong()).thenReturn(3600L);
        when(mockJsonNode.has("refresh_token")).thenReturn(false);
        
        OAuthTokenResponse result = instagramStrategy.refreshTokens(refreshToken);
        
        assertNotNull(result);
        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(3600L, result.getExpiresIn());
    }

    @Test
    void testRefreshTokens_ExpiredToken() {
        String refreshToken = "expired-refresh-token";
        
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));
        
        assertThrows(TokenExpiredException.class, () -> {
            instagramStrategy.refreshTokens(refreshToken);
        });
    }

    @Test
    void testGetUserPages_Success() throws Exception {
        String accessToken = "test-access-token";
        String userResponseJson = "{\"id\":\"123456789\",\"username\":\"testuser\",\"account_type\":\"PERSONAL\"}";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>(userResponseJson, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(mockResponse);
        
        // Mock ObjectMapper parsing
        com.fasterxml.jackson.databind.JsonNode mockJsonNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockIdNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockUsernameNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockAccountTypeNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        
        when(objectMapper.readTree(userResponseJson)).thenReturn(mockJsonNode);
        when(mockJsonNode.get("id")).thenReturn(mockIdNode);
        when(mockIdNode.asText()).thenReturn("123456789");
        when(mockJsonNode.has("username")).thenReturn(true);
        when(mockJsonNode.get("username")).thenReturn(mockUsernameNode);
        when(mockUsernameNode.asText()).thenReturn("testuser");
        when(mockJsonNode.has("account_type")).thenReturn(true);
        when(mockJsonNode.get("account_type")).thenReturn(mockAccountTypeNode);
        when(mockAccountTypeNode.asText()).thenReturn("PERSONAL");
        
        List<SocialMediaPage> result = instagramStrategy.getUserPages(accessToken);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        SocialMediaPage page = result.get(0);
        assertEquals("123456789", page.getId());
        assertEquals("testuser", page.getName());
        assertEquals("PERSONAL", page.getCategory());
        assertEquals("test-access-token", page.getAccessToken());
        assertEquals("instagram", page.getPlatform());
        assertEquals(platformConfig.getScopes(), page.getPermissions());
    }

    @Test
    void testGetUserPages_UnauthorizedToken() {
        String accessToken = "invalid-access-token";
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        
        assertThrows(TokenExpiredException.class, () -> {
            instagramStrategy.getUserPages(accessToken);
        });
    }

    @Test
    void testRevokeAccess_Success() {
        String accessToken = "test-access-token";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>("", HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(mockResponse);
        
        // Should not throw any exception
        assertDoesNotThrow(() -> {
            instagramStrategy.revokeAccess(accessToken);
        });
    }

    @Test
    void testRevokeAccess_Failure() {
        String accessToken = "test-access-token";
        
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));
        
        // Should not throw exception even on failure (graceful handling)
        assertDoesNotThrow(() -> {
            instagramStrategy.revokeAccess(accessToken);
        });
    }

    @Test
    void testMissingConfiguration() {
        when(oAuthConfig.getPlatformConfig("instagram")).thenReturn(null);
        
        assertThrows(OAuthException.class, () -> {
            instagramStrategy.getAuthorizationUrl("test-state");
        });
    }
}