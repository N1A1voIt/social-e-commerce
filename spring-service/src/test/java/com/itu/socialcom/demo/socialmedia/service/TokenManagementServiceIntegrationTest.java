package com.itu.socialcom.demo.socialmedia.service;

import com.itu.socialcom.demo.socialmedia.entity.AccessToken;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import com.itu.socialcom.demo.socialmedia.entity.RefreshToken;
import com.itu.socialcom.demo.socialmedia.entity.SupportedPlatform;
import com.itu.socialcom.demo.socialmedia.exception.TokenExpiredException;
import com.itu.socialcom.demo.socialmedia.oauth.OAuthStrategy;
import com.itu.socialcom.demo.socialmedia.repository.AccessTokenRepository;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageRepository;
import com.itu.socialcom.demo.socialmedia.repository.RefreshTokenRepository;
import com.itu.socialcom.demo.socialmedia.repository.SupportedPlatformRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for TokenManagementService.
 * Tests token management and rotation scenarios, token encryption/decryption,
 * and error handling and recovery mechanisms.
 * 
 * Requirements covered:
 * - 2.1: Token storage and retrieval workflows
 * - 4.1: Token management and refresh scenarios
 * - 4.2: Token encryption and security
 * - 6.1: Error handling and recovery mechanisms
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class TokenManagementServiceIntegrationTest {

    @Mock
    private AccessTokenRepository accessTokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private ManagedPageRepository pageRepository;

    @Mock
    private SupportedPlatformRepository platformRepository;

    @Mock
    private OAuthStrategyFactory strategyFactory;

    @Mock
    private OAuthStrategy mockOAuthStrategy;

    private TokenManagementServiceImpl tokenManagementService;

    private ManagedPage testPage;
    private SupportedPlatform testPlatform;
    private OAuthTokenResponse testTokens;
    private AccessToken testAccessToken;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        tokenManagementService = new TokenManagementServiceImpl(
            accessTokenRepository, refreshTokenRepository, pageRepository,
            platformRepository, strategyFactory
        );

        // Set encryption key for testing
        ReflectionTestUtils.setField(tokenManagementService, "encryptionKey", "test-encryption-key-32-chars!!");
        ReflectionTestUtils.setField(tokenManagementService, "encryptionAlgorithm", "AES");
        ReflectionTestUtils.setField(tokenManagementService, "encryptionTransformation", "AES/ECB/PKCS5Padding");

        // Setup test data
        testPlatform = new SupportedPlatform();
        testPlatform.setId(1L);
        testPlatform.setLabel("facebook");

        testPage = new ManagedPage();
        testPage.setId(1L);
        testPage.setSellerId(100L);
        testPage.setPlatformId(1L);
        testPage.setPlatformIdentifier("test-page-id");
        testPage.setPageTitle("Test Page");
        testPage.setActive();

        testTokens = new OAuthTokenResponse(
            "test-access-token",
            "test-refresh-token",
            3600L,
            "Bearer",
            Arrays.asList("pages_manage_posts", "pages_read_engagement")
        );

        testAccessToken = new AccessToken();
        testAccessToken.setId(1L);
        testAccessToken.setManagedPageId(1L);
        testAccessToken.setPlatformId(1L);
        testAccessToken.setPlatform("facebook");
        testAccessToken.setAccessToken("encrypted-access-token");
        testAccessToken.setExpirationDate(LocalDateTime.now().plusHours(1));

        testRefreshToken = new RefreshToken();
        testRefreshToken.setId(1L);
        testRefreshToken.setManagedPageId(1L);
        testRefreshToken.setPlatformId(1L);
        testRefreshToken.setPlatform("facebook");
        testRefreshToken.setRefreshToken("encrypted-refresh-token");
        testRefreshToken.setExpirationDate(LocalDateTime.now().plusDays(60));
    }

    @Test
    void testStoreTokens_Success() {
        // Arrange
        Long pageId = 1L;
        Long platformId = 1L;
        String platform = "facebook";

        // Act
        tokenManagementService.storeTokens(pageId, testTokens, platformId, platform);

        // Assert
        verify(accessTokenRepository).save(any(AccessToken.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void testStoreTokens_WithPageLookup() {
        // Arrange
        Long pageId = 1L;

        when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
        when(platformRepository.findById(testPage.getPlatformId())).thenReturn(Optional.of(testPlatform));

        // Act
        tokenManagementService.storeTokens(pageId, testTokens);

        // Assert
        verify(pageRepository).findById(pageId);
        verify(platformRepository).findById(testPage.getPlatformId());
        verify(accessTokenRepository).save(any(AccessToken.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void testStoreTokens_PageNotFound() {
        // Arrange
        Long pageId = 999L;

        when(pageRepository.findById(pageId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            tokenManagementService.storeTokens(pageId, testTokens);
        });

        verify(accessTokenRepository, never()).save(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void testStoreTokens_OnlyAccessToken() {
        // Arrange
        Long pageId = 1L;
        Long platformId = 1L;
        String platform = "facebook";
        OAuthTokenResponse tokensWithoutRefresh = new OAuthTokenResponse(
            "test-access-token", null, 3600L, "Bearer", null
        );

        // Act
        tokenManagementService.storeTokens(pageId, tokensWithoutRefresh, platformId, platform);

        // Assert
        verify(accessTokenRepository).save(any(AccessToken.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void testGetValidTokens_Success() {
        // Arrange
        Long pageId = 1L;
        LocalDateTime now = LocalDateTime.now();

        when(accessTokenRepository.findValidTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(testAccessToken));
        when(refreshTokenRepository.findValidRefreshTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(testRefreshToken));

        // Act
        Optional<OAuthTokenResponse> result = tokenManagementService.getValidTokens(pageId);

        // Assert
        assertTrue(result.isPresent());
        assertNotNull(result.get().getAccessToken());
        assertNotNull(result.get().getRefreshToken());
        assertEquals("Bearer", result.get().getTokenType());
    }

    @Test
    void testGetValidTokens_NoValidTokens() {
        // Arrange
        Long pageId = 1L;

        when(accessTokenRepository.findValidTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.empty());

        // Act
        Optional<OAuthTokenResponse> result = tokenManagementService.getValidTokens(pageId);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testGetValidTokens_DecryptionFailure() {
        // Arrange
        Long pageId = 1L;
        AccessToken corruptedToken = new AccessToken();
        corruptedToken.setAccessToken("corrupted-encrypted-token");
        corruptedToken.setExpirationDate(LocalDateTime.now().plusHours(1));

        when(accessTokenRepository.findValidTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(corruptedToken));

        // Act
        Optional<OAuthTokenResponse> result = tokenManagementService.getValidTokens(pageId);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testRefreshTokensIfNeeded_NotNeeded() {
        // Arrange
        Long pageId = 1L;
        AccessToken futureToken = new AccessToken();
        futureToken.setExpirationDate(LocalDateTime.now().plusHours(2));

        when(accessTokenRepository.findValidTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(futureToken));

        // Act
        boolean result = tokenManagementService.refreshTokensIfNeeded(pageId);

        // Assert
        assertTrue(result);
        verify(strategyFactory, never()).getStrategy(any());
    }

    @Test
    void testRefreshTokensIfNeeded_RefreshNeeded() {
        // Arrange
        Long pageId = 1L;
        AccessToken nearExpiryToken = new AccessToken();
        nearExpiryToken.setExpirationDate(LocalDateTime.now().plusMinutes(15));

        when(accessTokenRepository.findValidTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(nearExpiryToken));
        when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
        when(platformRepository.findById(testPage.getPlatformId())).thenReturn(Optional.of(testPlatform));
        when(refreshTokenRepository.findValidRefreshTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(testRefreshToken));
        when(strategyFactory.getStrategy(testPlatform.getLabel())).thenReturn(mockOAuthStrategy);
        when(mockOAuthStrategy.refreshTokens(any())).thenReturn(testTokens);

        // Act
        boolean result = tokenManagementService.refreshTokensIfNeeded(pageId);

        // Assert
        assertTrue(result);
        verify(mockOAuthStrategy).refreshTokens(any());
        verify(accessTokenRepository).save(any(AccessToken.class));
    }

    @Test
    void testForceRefreshTokens_Success() {
        // Arrange
        Long pageId = 1L;

        when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
        when(platformRepository.findById(testPage.getPlatformId())).thenReturn(Optional.of(testPlatform));
        when(refreshTokenRepository.findValidRefreshTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(testRefreshToken));
        when(strategyFactory.getStrategy(testPlatform.getLabel())).thenReturn(mockOAuthStrategy);
        when(mockOAuthStrategy.refreshTokens(any())).thenReturn(testTokens);

        // Act
        Optional<OAuthTokenResponse> result = tokenManagementService.forceRefreshTokens(pageId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testTokens.getAccessToken(), result.get().getAccessToken());
        verify(mockOAuthStrategy).refreshTokens(any());
        verify(accessTokenRepository).save(any(AccessToken.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void testForceRefreshTokens_NoRefreshToken() {
        // Arrange
        Long pageId = 1L;

        when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
        when(platformRepository.findById(testPage.getPlatformId())).thenReturn(Optional.of(testPlatform));
        when(refreshTokenRepository.findValidRefreshTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.empty());

        // Act
        Optional<OAuthTokenResponse> result = tokenManagementService.forceRefreshTokens(pageId);

        // Assert
        assertFalse(result.isPresent());
        verify(mockOAuthStrategy, never()).refreshTokens(any());
    }

    @Test
    void testForceRefreshTokens_RefreshTokenExpired() {
        // Arrange
        Long pageId = 1L;

        when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
        when(platformRepository.findById(testPage.getPlatformId())).thenReturn(Optional.of(testPlatform));
        when(refreshTokenRepository.findValidRefreshTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(testRefreshToken));
        when(strategyFactory.getStrategy(testPlatform.getLabel())).thenReturn(mockOAuthStrategy);
        when(mockOAuthStrategy.refreshTokens(any()))
            .thenThrow(new TokenExpiredException("facebook", "EXPIRED_REFRESH_TOKEN", "Refresh token expired"));

        // Act
        Optional<OAuthTokenResponse> result = tokenManagementService.forceRefreshTokens(pageId);

        // Assert
        assertFalse(result.isPresent());
        verify(mockOAuthStrategy).refreshTokens(any());
    }

    @Test
    void testRevokeTokens_Success() {
        // Arrange
        Long pageId = 1L;

        // Act
        tokenManagementService.revokeTokens(pageId);

        // Assert
        verify(accessTokenRepository).deleteByManagedPageId(pageId);
        verify(refreshTokenRepository).deleteByManagedPageId(pageId);
    }

    @Test
    void testRevokeTokensOnPlatform_Success() {
        // Arrange
        Long pageId = 1L;
        String platform = "facebook";

        when(accessTokenRepository.findValidTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(testAccessToken));
        when(strategyFactory.getStrategy(platform)).thenReturn(mockOAuthStrategy);

        // Act
        tokenManagementService.revokeTokensOnPlatform(pageId, platform);

        // Assert
        verify(mockOAuthStrategy).revokeAccess(any());
        verify(accessTokenRepository).deleteByManagedPageId(pageId);
        verify(refreshTokenRepository).deleteByManagedPageId(pageId);
    }

    @Test
    void testRevokeTokensOnPlatform_PlatformRevocationFailure() {
        // Arrange
        Long pageId = 1L;
        String platform = "facebook";

        when(accessTokenRepository.findValidTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(testAccessToken));
        when(strategyFactory.getStrategy(platform)).thenReturn(mockOAuthStrategy);
        doThrow(new RuntimeException("Platform API error")).when(mockOAuthStrategy).revokeAccess(any());

        // Act - Should not throw exception, should continue with local cleanup
        assertDoesNotThrow(() -> {
            tokenManagementService.revokeTokensOnPlatform(pageId, platform);
        });

        // Assert - Local cleanup should still happen
        verify(accessTokenRepository).deleteByManagedPageId(pageId);
        verify(refreshTokenRepository).deleteByManagedPageId(pageId);
    }

    @Test
    void testCleanupExpiredTokens_Success() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<AccessToken> expiredAccessTokens = Arrays.asList(testAccessToken);
        List<RefreshToken> expiredRefreshTokens = Arrays.asList(testRefreshToken);

        when(accessTokenRepository.findExpiredTokens(any(LocalDateTime.class)))
            .thenReturn(expiredAccessTokens);
        when(refreshTokenRepository.findExpiredTokens(any(LocalDateTime.class)))
            .thenReturn(expiredRefreshTokens);

        // Act
        int result = tokenManagementService.cleanupExpiredTokens();

        // Assert
        assertEquals(2, result); // 1 access token + 1 refresh token
        verify(accessTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
        verify(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
    }

    @Test
    void testValidateTokenIntegrity_Success() {
        // Arrange
        Long pageId = 1L;

        when(accessTokenRepository.findLatestTokenByPageId(pageId))
            .thenReturn(Optional.of(testAccessToken));
        when(refreshTokenRepository.findLatestRefreshTokenByPageId(pageId))
            .thenReturn(Optional.of(testRefreshToken));

        // Act
        boolean result = tokenManagementService.validateTokenIntegrity(pageId);

        // Assert
        assertTrue(result);
    }

    @Test
    void testValidateTokenIntegrity_CorruptedToken() {
        // Arrange
        Long pageId = 1L;
        AccessToken corruptedToken = new AccessToken();
        corruptedToken.setAccessToken("corrupted-token");

        when(accessTokenRepository.findLatestTokenByPageId(pageId))
            .thenReturn(Optional.of(corruptedToken));

        // Act
        boolean result = tokenManagementService.validateTokenIntegrity(pageId);

        // Assert
        assertFalse(result);
    }

    @Test
    void testHasValidAccessTokens() {
        // Arrange
        Long pageId = 1L;

        when(accessTokenRepository.hasValidTokens(pageId, any(LocalDateTime.class))).thenReturn(true);

        // Act
        boolean result = tokenManagementService.hasValidAccessTokens(pageId);

        // Assert
        assertTrue(result);
        verify(accessTokenRepository).hasValidTokens(eq(pageId), any(LocalDateTime.class));
    }

    @Test
    void testHasValidRefreshTokens() {
        // Arrange
        Long pageId = 1L;

        when(refreshTokenRepository.hasValidRefreshTokens(pageId, any(LocalDateTime.class))).thenReturn(true);

        // Act
        boolean result = tokenManagementService.hasValidRefreshTokens(pageId);

        // Assert
        assertTrue(result);
        verify(refreshTokenRepository).hasValidRefreshTokens(eq(pageId), any(LocalDateTime.class));
    }

    @Test
    void testGetAccessTokenExpiration() {
        // Arrange
        Long pageId = 1L;
        LocalDateTime expiration = LocalDateTime.now().plusHours(1);
        testAccessToken.setExpirationDate(expiration);

        when(accessTokenRepository.findValidTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(testAccessToken));

        // Act
        Optional<LocalDateTime> result = tokenManagementService.getAccessTokenExpiration(pageId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expiration, result.get());
    }

    @Test
    void testGetRefreshTokenExpiration() {
        // Arrange
        Long pageId = 1L;
        LocalDateTime expiration = LocalDateTime.now().plusDays(60);
        testRefreshToken.setExpirationDate(expiration);

        when(refreshTokenRepository.findValidRefreshTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(testRefreshToken));

        // Act
        Optional<LocalDateTime> result = tokenManagementService.getRefreshTokenExpiration(pageId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expiration, result.get());
    }

    @Test
    void testAreAccessTokensNearExpiration_True() {
        // Arrange
        Long pageId = 1L;
        int minutesThreshold = 30;
        AccessToken nearExpiryToken = new AccessToken();
        nearExpiryToken.setExpirationDate(LocalDateTime.now().plusMinutes(15));

        when(accessTokenRepository.findValidTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(nearExpiryToken));

        // Act
        boolean result = tokenManagementService.areAccessTokensNearExpiration(pageId, minutesThreshold);

        // Assert
        assertTrue(result);
    }

    @Test
    void testAreAccessTokensNearExpiration_False() {
        // Arrange
        Long pageId = 1L;
        int minutesThreshold = 30;
        AccessToken futureToken = new AccessToken();
        futureToken.setExpirationDate(LocalDateTime.now().plusHours(2));

        when(accessTokenRepository.findValidTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(futureToken));

        // Act
        boolean result = tokenManagementService.areAccessTokensNearExpiration(pageId, minutesThreshold);

        // Assert
        assertFalse(result);
    }

    @Test
    void testAreRefreshTokensNearExpiration_True() {
        // Arrange
        Long pageId = 1L;
        int daysThreshold = 7;
        RefreshToken nearExpiryToken = new RefreshToken();
        nearExpiryToken.setExpirationDate(LocalDateTime.now().plusDays(3));

        when(refreshTokenRepository.findValidRefreshTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(nearExpiryToken));

        // Act
        boolean result = tokenManagementService.areRefreshTokensNearExpiration(pageId, daysThreshold);

        // Assert
        assertTrue(result);
    }

    @Test
    void testAreRefreshTokensNearExpiration_False() {
        // Arrange
        Long pageId = 1L;
        int daysThreshold = 7;
        RefreshToken futureToken = new RefreshToken();
        futureToken.setExpirationDate(LocalDateTime.now().plusDays(30));

        when(refreshTokenRepository.findValidRefreshTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(futureToken));

        // Act
        boolean result = tokenManagementService.areRefreshTokensNearExpiration(pageId, daysThreshold);

        // Assert
        assertFalse(result);
    }

    @Test
    void testEncryptDecryptToken_Success() {
        // Arrange
        String originalToken = "test-token-12345";

        // Act
        String encrypted = tokenManagementService.encryptToken(originalToken);
        String decrypted = tokenManagementService.decryptToken(encrypted);

        // Assert
        assertNotEquals(originalToken, encrypted);
        assertEquals(originalToken, decrypted);
    }

    @Test
    void testEncryptToken_NullInput() {
        // Act
        String result = tokenManagementService.encryptToken(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testEncryptToken_EmptyInput() {
        // Act
        String result = tokenManagementService.encryptToken("");

        // Assert
        assertEquals("", result);
    }

    @Test
    void testDecryptToken_NullInput() {
        // Act
        String result = tokenManagementService.decryptToken(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testDecryptToken_EmptyInput() {
        // Act
        String result = tokenManagementService.decryptToken("");

        // Assert
        assertEquals("", result);
    }

    @Test
    void testUpdateTokens_Success() {
        // Arrange
        Long pageId = 1L;

        // Act
        tokenManagementService.updateTokens(pageId, testTokens);

        // Assert
        verify(accessTokenRepository).markTokensAsExpired(eq(pageId), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(refreshTokenRepository).markTokensAsExpired(eq(pageId), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(accessTokenRepository).save(any(AccessToken.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void testMarkOldTokensAsExpired() {
        // Arrange
        Long pageId = 1L;

        // Act
        tokenManagementService.markOldTokensAsExpired(pageId);

        // Assert
        verify(accessTokenRepository).markTokensAsExpired(eq(pageId), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(refreshTokenRepository).markTokensAsExpired(eq(pageId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGetTokenStatistics_Success() {
        // Arrange
        when(accessTokenRepository.count()).thenReturn(10L);
        when(refreshTokenRepository.count()).thenReturn(8L);
        when(accessTokenRepository.findAll()).thenReturn(Arrays.asList(testAccessToken));
        when(refreshTokenRepository.findAll()).thenReturn(Arrays.asList(testRefreshToken));
        when(accessTokenRepository.findTokensExpiringBefore(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList());
        when(refreshTokenRepository.findTokensExpiringBefore(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList());
        when(pageRepository.findAll()).thenReturn(Arrays.asList(testPage));

        // Act
        TokenStatistics result = tokenManagementService.getTokenStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getTotalAccessTokens());
        assertEquals(8L, result.getTotalRefreshTokens());
        assertNotNull(result.getLastCleanupTime());
    }
}