package com.itu.socialcom.demo.socialmedia.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
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

class FacebookOAuthStrategyTest {

    @Mock
    private OAuthConfig oAuthConfig;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private FacebookOAuthStrategy facebookStrategy;
    private OAuthConfig.PlatformConfig platformConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        platformConfig = new OAuthConfig.PlatformConfig();
        platformConfig.setClientId("test-client-id");
        platformConfig.setClientSecret("test-client-secret");
        platformConfig.setRedirectUri("http://localhost:8080/api/oauth/facebook/callback");
        platformConfig.setScopes(Arrays.asList("pages_manage_posts", "pages_read_engagement"));
        platformConfig.setAuthorizationUrl("https://www.facebook.com/v18.0/dialog/oauth");
        platformConfig.setTokenUrl("https://graph.facebook.com/v18.0/oauth/access_token");
        platformConfig.setUserInfoUrl("https://graph.facebook.com/v18.0/me/accounts");
        platformConfig.setRevokeUrl("https://graph.facebook.com/v18.0/me/permissions");
        
        when(oAuthConfig.getPlatformConfig("facebook")).thenReturn(platformConfig);
        
        facebookStrategy = new FacebookOAuthStrategy(oAuthConfig, restTemplate, objectMapper);
    }

    @Test
    void testGetPlatformName() {
        assertEquals("facebook", facebookStrategy.getPlatformName());
    }

    @Test
    void testGetAuthorizationUrl() {
        String state = "test-state-123";
        String authUrl = facebookStrategy.getAuthorizationUrl(state);
        
        assertNotNull(authUrl);
        assertTrue(authUrl.contains("client_id=test-client-id"));
        assertTrue(authUrl.contains("state=test-state-123"));
        assertTrue(authUrl.contains("scope=pages_manage_posts,pages_read_engagement"));
        assertTrue(authUrl.contains("response_type=code"));
        assertTrue(authUrl.startsWith("https://www.facebook.com/v18.0/dialog/oauth"));
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
        
        OAuthTokenResponse result = facebookStrategy.exchangeCodeForTokens(code, state);
        
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
            facebookStrategy.exchangeCodeForTokens(code, state);
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
        
        OAuthTokenResponse result = facebookStrategy.refreshTokens(refreshToken);
        
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
            facebookStrategy.refreshTokens(refreshToken);
        });
    }

    @Test
    void testGetUserPages_Success() throws Exception {
        String accessToken = "test-access-token";
        String pagesResponseJson = "{\"data\":[{\"id\":\"123\",\"name\":\"Test Page\",\"category\":\"Business\",\"access_token\":\"page-token\"}]}";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>(pagesResponseJson, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(mockResponse);
        
        // Mock ObjectMapper parsing
        com.fasterxml.jackson.databind.JsonNode mockJsonNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockDataNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockPageNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockIdNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockNameNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockCategoryNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockAccessTokenNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        
        when(objectMapper.readTree(pagesResponseJson)).thenReturn(mockJsonNode);
        when(mockJsonNode.has("data")).thenReturn(true);
        when(mockJsonNode.get("data")).thenReturn(mockDataNode);
        when(mockDataNode.iterator()).thenReturn(Arrays.asList(mockPageNode).iterator());
        when(mockPageNode.get("id")).thenReturn(mockIdNode);
        when(mockIdNode.asText()).thenReturn("123");
        when(mockPageNode.get("name")).thenReturn(mockNameNode);
        when(mockNameNode.asText()).thenReturn("Test Page");
        when(mockPageNode.has("category")).thenReturn(true);
        when(mockPageNode.get("category")).thenReturn(mockCategoryNode);
        when(mockCategoryNode.asText()).thenReturn("Business");
        when(mockPageNode.has("access_token")).thenReturn(true);
        when(mockPageNode.get("access_token")).thenReturn(mockAccessTokenNode);
        when(mockAccessTokenNode.asText()).thenReturn("page-token");
        when(mockPageNode.has("picture")).thenReturn(false);
        when(mockPageNode.has("perms")).thenReturn(false);
        
        List<SocialMediaPage> result = facebookStrategy.getUserPages(accessToken);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        SocialMediaPage page = result.get(0);
        assertEquals("123", page.getId());
        assertEquals("Test Page", page.getName());
        assertEquals("Business", page.getCategory());
        assertEquals("page-token", page.getAccessToken());
        assertEquals("facebook", page.getPlatform());
    }

    @Test
    void testGetUserPages_UnauthorizedToken() {
        String accessToken = "invalid-access-token";
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        
        assertThrows(TokenExpiredException.class, () -> {
            facebookStrategy.getUserPages(accessToken);
        });
    }

    @Test
    void testRevokeAccess_Success() {
        String accessToken = "test-access-token";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>("", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class))).thenReturn(mockResponse);
        
        // Should not throw any exception
        assertDoesNotThrow(() -> {
            facebookStrategy.revokeAccess(accessToken);
        });
    }

    @Test
    void testRevokeAccess_Failure() {
        String accessToken = "test-access-token";
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));
        
        // Should not throw exception even on failure (graceful handling)
        assertDoesNotThrow(() -> {
            facebookStrategy.revokeAccess(accessToken);
        });
    }

    @Test
    void testMissingConfiguration() {
        when(oAuthConfig.getPlatformConfig("facebook")).thenReturn(null);
        
        assertThrows(OAuthException.class, () -> {
            facebookStrategy.getAuthorizationUrl("test-state");
        });
    }

    @Test
    void testExchangeCodeForTokens_NetworkError() {
        String code = "test-auth-code";
        String state = "test-state";
        
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenThrow(new RuntimeException("Network connection failed"));
        
        OAuthException exception = assertThrows(OAuthException.class, () -> {
            facebookStrategy.exchangeCodeForTokens(code, state);
        });
        
        assertEquals("facebook", exception.getPlatform());
        assertEquals("UNEXPECTED_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Network connection failed"));
    }

    @Test
    void testExchangeCodeForTokens_InvalidJsonResponse() throws Exception {
        String code = "test-auth-code";
        String state = "test-state";
        String invalidJson = "invalid-json-response";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>(invalidJson, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(mockResponse);
        when(objectMapper.readTree(invalidJson)).thenThrow(new RuntimeException("Invalid JSON"));
        
        OAuthException exception = assertThrows(OAuthException.class, () -> {
            facebookStrategy.exchangeCodeForTokens(code, state);
        });
        
        assertEquals("facebook", exception.getPlatform());
        assertEquals("PARSE_ERROR", exception.getErrorCode());
    }

    @Test
    void testRefreshTokens_NetworkError() {
        String refreshToken = "test-refresh-token";
        
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenThrow(new RuntimeException("Connection timeout"));
        
        OAuthException exception = assertThrows(OAuthException.class, () -> {
            facebookStrategy.refreshTokens(refreshToken);
        });
        
        assertEquals("facebook", exception.getPlatform());
        assertEquals("UNEXPECTED_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Connection timeout"));
    }

    @Test
    void testGetUserPages_InvalidJsonResponse() throws Exception {
        String accessToken = "test-access-token";
        String invalidJson = "invalid-json-response";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>(invalidJson, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(mockResponse);
        when(objectMapper.readTree(invalidJson)).thenThrow(new RuntimeException("Invalid JSON"));
        
        OAuthException exception = assertThrows(OAuthException.class, () -> {
            facebookStrategy.getUserPages(accessToken);
        });
        
        assertEquals("facebook", exception.getPlatform());
        assertEquals("PARSE_ERROR", exception.getErrorCode());
    }

    @Test
    void testGetUserPages_EmptyDataResponse() throws Exception {
        String accessToken = "test-access-token";
        String emptyResponseJson = "{\"data\":[]}";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>(emptyResponseJson, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(mockResponse);
        
        // Mock ObjectMapper parsing
        com.fasterxml.jackson.databind.JsonNode mockJsonNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode mockDataNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        
        when(objectMapper.readTree(emptyResponseJson)).thenReturn(mockJsonNode);
        when(mockJsonNode.has("data")).thenReturn(true);
        when(mockJsonNode.get("data")).thenReturn(mockDataNode);
        when(mockDataNode.iterator()).thenReturn(java.util.Collections.emptyIterator());
        
        List<SocialMediaPage> result = facebookStrategy.getUserPages(accessToken);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetUserPages_RateLimitError() {
        String accessToken = "test-access-token";
        
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded"));
        
        OAuthException exception = assertThrows(OAuthException.class, () -> {
            facebookStrategy.getUserPages(accessToken);
        });
        
        assertEquals("facebook", exception.getPlatform());
        assertEquals("PAGES_FETCH_ERROR", exception.getErrorCode());
    }

    @Test
    void testTokenResponseWithRefreshToken() throws Exception {
        String code = "test-auth-code";
        String state = "test-state";
        String tokenResponseJson = "{\"access_token\":\"test-access-token\",\"token_type\":\"Bearer\",\"expires_in\":3600,\"refresh_token\":\"test-refresh-token\"}";
        
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
        when(mockAccessTokenNode.asText()).thenReturn("test-access-token");
        when(mockJsonNode.has("token_type")).thenReturn(true);
        when(mockJsonNode.get("token_type")).thenReturn(mockTokenTypeNode);
        when(mockTokenTypeNode.asText()).thenReturn("Bearer");
        when(mockJsonNode.has("expires_in")).thenReturn(true);
        when(mockJsonNode.get("expires_in")).thenReturn(mockExpiresInNode);
        when(mockExpiresInNode.asLong()).thenReturn(3600L);
        when(mockJsonNode.has("refresh_token")).thenReturn(true);
        when(mockJsonNode.get("refresh_token")).thenReturn(mockRefreshTokenNode);
        when(mockRefreshTokenNode.asText()).thenReturn("test-refresh-token");
        
        OAuthTokenResponse result = facebookStrategy.exchangeCodeForTokens(code, state);
        
        assertNotNull(result);
        assertEquals("test-access-token", result.getAccessToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(3600L, result.getExpiresIn());
        assertEquals("test-refresh-token", result.getRefreshToken());
        assertEquals(platformConfig.getScopes(), result.getScopes());
    }
}