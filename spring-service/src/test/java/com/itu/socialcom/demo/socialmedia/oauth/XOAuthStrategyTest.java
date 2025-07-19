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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class XOAuthStrategyTest {

    @Mock
    private OAuthConfig oAuthConfig;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private XOAuthStrategy xStrategy;
    private OAuthConfig.PlatformConfig platformConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        platformConfig = new OAuthConfig.PlatformConfig();
        platformConfig.setClientId("test-client-id");
        platformConfig.setClientSecret("test-client-secret");
        platformConfig.setRedirectUri("http://localhost:8080/api/oauth/x/callback");
        platformConfig.setScopes(Arrays.asList("tweet.read", "tweet.write", "users.read"));
        platformConfig.setAuthorizationUrl("https://twitter.com/i/oauth2/authorize");
        platformConfig.setTokenUrl("https://api.twitter.com/2/oauth2/token");
        platformConfig.setUserInfoUrl("https://api.twitter.com/2/users/me");
        platformConfig.setRevokeUrl("https://api.twitter.com/2/oauth2/revoke");
        
        when(oAuthConfig.getPlatformConfig("x")).thenReturn(platformConfig);
        
        xStrategy = new XOAuthStrategy(oAuthConfig, restTemplate, objectMapper);
    }

    @Test
    void testGetPlatformName() {
        assertEquals("x", xStrategy.getPlatformName());
    }

    @Test
    void testGetAuthorizationUrl() {
        String state = "test-state-123";
        String authUrl = xStrategy.getAuthorizationUrl(state);
        
        assertNotNull(authUrl);
        assertTrue(authUrl.contains("client_id=test-client-id"));
        assertTrue(authUrl.contains("state=test-state-123"));
        // X uses space-separated scopes, which get URL encoded as %20
        assertTrue(authUrl.contains("scope=tweet.read") && authUrl.contains("tweet.write") && authUrl.contains("users.read"));
        assertTrue(authUrl.contains("response_type=code"));
        assertTrue(authUrl.contains("code_challenge="));
        assertTrue(authUrl.contains("code_challenge_method=S256"));
        assertTrue(authUrl.startsWith("https://twitter.com/i/oauth2/authorize"));
    }

    @Test
    void testExchangeCodeForTokens_Success() throws Exception {
        String code = "test-auth-code";
        String state = "test-state";
        String tokenResponseJson = "{\"access_token\":\"test-access-token\",\"token_type\":\"Bearer\",\"expires_in\":7200,\"refresh_token\":\"test-refresh-token\",\"scope\":\"tweet.read tweet.write users.read\"}";
        
        // First generate authorization URL to store code verifier
        xStrategy.getAuthorizationUrl(state);
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>(tokenResponseJson, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(mockResponse);
        
        // Mock ObjectMapper parsing
        com.fasterxml.jackson.databind.JsonNode mockJsonNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockAccessTokenNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockTokenTypeNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockExpiresInNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockRefreshTokenNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockScopeNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        
        when(objectMapper.readTree(tokenResponseJson)).thenReturn(mockJsonNode);
        when(mockJsonNode.get("access_token")).thenReturn(mockAccessTokenNode);
        when(mockAccessTokenNode.asText()).thenReturn("test-access-token");
        when(mockJsonNode.has("token_type")).thenReturn(true);
        when(mockJsonNode.get("token_type")).thenReturn(mockTokenTypeNode);
        when(mockTokenTypeNode.asText()).thenReturn("Bearer");
        when(mockJsonNode.has("expires_in")).thenReturn(true);
        when(mockJsonNode.get("expires_in")).thenReturn(mockExpiresInNode);
        when(mockExpiresInNode.asLong()).thenReturn(7200L);
        when(mockJsonNode.has("refresh_token")).thenReturn(true);
        when(mockJsonNode.get("refresh_token")).thenReturn(mockRefreshTokenNode);
        when(mockRefreshTokenNode.asText()).thenReturn("test-refresh-token");
        when(mockJsonNode.has("scope")).thenReturn(true);
        when(mockJsonNode.get("scope")).thenReturn(mockScopeNode);
        when(mockScopeNode.asText()).thenReturn("tweet.read tweet.write users.read");
        
        OAuthTokenResponse result = xStrategy.exchangeCodeForTokens(code, state);
        
        assertNotNull(result);
        assertEquals("test-access-token", result.getAccessToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(7200L, result.getExpiresIn());
        assertEquals("test-refresh-token", result.getRefreshToken());
        assertEquals(3, result.getScopes().size());
        assertTrue(result.getScopes().contains("tweet.read"));
        assertTrue(result.getScopes().contains("tweet.write"));
        assertTrue(result.getScopes().contains("users.read"));
    }

    @Test
    void testExchangeCodeForTokens_InvalidState() {
        String code = "test-auth-code";
        String state = "invalid-state";
        
        assertThrows(InvalidAuthorizationCodeException.class, () -> {
            xStrategy.exchangeCodeForTokens(code, state);
        });
    }

    @Test
    void testExchangeCodeForTokens_InvalidCode() {
        String code = "invalid-code";
        String state = "test-state";
        
        // First generate authorization URL to store code verifier
        xStrategy.getAuthorizationUrl(state);
        
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));
        
        assertThrows(InvalidAuthorizationCodeException.class, () -> {
            xStrategy.exchangeCodeForTokens(code, state);
        });
    }

    @Test
    void testRefreshTokens_Success() throws Exception {
        String refreshToken = "test-refresh-token";
        String tokenResponseJson = "{\"access_token\":\"new-access-token\",\"token_type\":\"Bearer\",\"expires_in\":7200,\"refresh_token\":\"new-refresh-token\"}";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>(tokenResponseJson, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(mockResponse);
        
        // Mock ObjectMapper parsing
        com.fasterxml.jackson.databind.JsonNode mockJsonNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockAccessTokenNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockTokenTypeNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockExpiresInNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockRefreshTokenNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        
        when(objectMapper.readTree(tokenResponseJson)).thenReturn(mockJsonNode);
        when(mockJsonNode.get("access_token")).thenReturn(mockAccessTokenNode);
        when(mockAccessTokenNode.asText()).thenReturn("new-access-token");
        when(mockJsonNode.has("token_type")).thenReturn(true);
        when(mockJsonNode.get("token_type")).thenReturn(mockTokenTypeNode);
        when(mockTokenTypeNode.asText()).thenReturn("Bearer");
        when(mockJsonNode.has("expires_in")).thenReturn(true);
        when(mockJsonNode.get("expires_in")).thenReturn(mockExpiresInNode);
        when(mockExpiresInNode.asLong()).thenReturn(7200L);
        when(mockJsonNode.has("refresh_token")).thenReturn(true);
        when(mockJsonNode.get("refresh_token")).thenReturn(mockRefreshTokenNode);
        when(mockRefreshTokenNode.asText()).thenReturn("new-refresh-token");
        when(mockJsonNode.has("scope")).thenReturn(false);
        
        OAuthTokenResponse result = xStrategy.refreshTokens(refreshToken);
        
        assertNotNull(result);
        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(7200L, result.getExpiresIn());
        assertEquals("new-refresh-token", result.getRefreshToken());
    }

    @Test
    void testRefreshTokens_ExpiredToken() {
        String refreshToken = "expired-refresh-token";
        
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        
        assertThrows(TokenExpiredException.class, () -> {
            xStrategy.refreshTokens(refreshToken);
        });
    }

    @Test
    void testGetUserPages_Success() throws Exception {
        String accessToken = "test-access-token";
        String userResponseJson = "{\"data\":{\"id\":\"123456789\",\"name\":\"Test User\",\"username\":\"testuser\",\"profile_image_url\":\"https://example.com/profile.jpg\"}}";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>(userResponseJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class))).thenReturn(mockResponse);
        
        // Mock ObjectMapper parsing
        com.fasterxml.jackson.databind.JsonNode mockJsonNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockDataNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockIdNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockNameNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockUsernameNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockProfileImageNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        
        when(objectMapper.readTree(userResponseJson)).thenReturn(mockJsonNode);
        when(mockJsonNode.has("data")).thenReturn(true);
        when(mockJsonNode.get("data")).thenReturn(mockDataNode);
        when(mockDataNode.get("id")).thenReturn(mockIdNode);
        when(mockIdNode.asText()).thenReturn("123456789");
        when(mockDataNode.has("name")).thenReturn(true);
        when(mockDataNode.get("name")).thenReturn(mockNameNode);
        when(mockNameNode.asText()).thenReturn("Test User");
        when(mockDataNode.has("username")).thenReturn(true);
        when(mockDataNode.get("username")).thenReturn(mockUsernameNode);
        when(mockUsernameNode.asText()).thenReturn("testuser");
        when(mockDataNode.has("profile_image_url")).thenReturn(true);
        when(mockDataNode.get("profile_image_url")).thenReturn(mockProfileImageNode);
        when(mockProfileImageNode.asText()).thenReturn("https://example.com/profile.jpg");
        
        List<SocialMediaPage> result = xStrategy.getUserPages(accessToken);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        SocialMediaPage page = result.get(0);
        assertEquals("123456789", page.getId());
        assertEquals("Test User", page.getName());
        assertEquals("@testuser", page.getCategory());
        assertEquals("https://example.com/profile.jpg", page.getProfilePictureUrl());
        assertEquals("test-access-token", page.getAccessToken());
        assertEquals("x", page.getPlatform());
        assertEquals(platformConfig.getScopes(), page.getPermissions());
    }

    @Test
    void testGetUserPages_UnauthorizedToken() {
        String accessToken = "invalid-access-token";
        
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        
        assertThrows(TokenExpiredException.class, () -> {
            xStrategy.getUserPages(accessToken);
        });
    }

    @Test
    void testRevokeAccess_Success() {
        String accessToken = "test-access-token";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>("", HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(mockResponse);
        
        // Should not throw any exception
        assertDoesNotThrow(() -> {
            xStrategy.revokeAccess(accessToken);
        });
    }

    @Test
    void testRevokeAccess_Failure() {
        String accessToken = "test-access-token";
        
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));
        
        // Should not throw exception even on failure (graceful handling)
        assertDoesNotThrow(() -> {
            xStrategy.revokeAccess(accessToken);
        });
    }

    @Test
    void testMissingConfiguration() {
        when(oAuthConfig.getPlatformConfig("x")).thenReturn(null);
        
        assertThrows(OAuthException.class, () -> {
            xStrategy.getAuthorizationUrl("test-state");
        });
    }
}