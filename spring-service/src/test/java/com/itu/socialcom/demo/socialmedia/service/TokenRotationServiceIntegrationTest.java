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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for TokenRotationService.
 * Tests token rotation scenarios, expiration monitoring, and failure handling.
 * 
 * Requirements covered:
 * - 4.1: Token management and rotation scenarios
 * - 4.2: Token expiration monitoring
 * - 6.1: Error handling and recovery mechanisms
 * - 6.2: Failure handling and re-authentication workflows
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class TokenRotationServiceIntegrationTest {

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
    private TokenManagementService tokenManagementService;

    @Mock
    private OAuthStrategy mockOAuthStrategy;

    private TokenRotationService tokenRotationService;

    private ManagedPage testPage;
    private SupportedPlatform testPlatform;
    private AccessToken testAccessToken;
    private RefreshToken testRefreshToken;
    private OAuthTokenResponse newTokens;

    @BeforeEach
    void setUp() {
        tokenRotationService = new TokenRotationService(
            accessTokenRepository, refreshTokenRepository, pageRepository,
            platformRepository, strategyFactory, tokenManagementService
        );

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

        testAccessToken = new AccessToken();
        testAccessToken.setId(1L);
        testAccessToken.setManagedPageId(1L);
        testAccessToken.setPlatformId(1L);
        testAccessToken.setPlatform("facebook");
        testAccessToken.setAccessToken("encrypted-access-token");
        testAccessToken.setExpirationDate(LocalDateTime.now().plusMinutes(30));

        testRefreshToken = new RefreshToken();
        testRefreshToken.setId(1L);
        testRefreshToken.setManagedPageId(1L);
        testRefreshToken.setPlatformId(1L);
        testRefreshToken.setPlatform("facebook");
        testRefreshToken.setRefreshToken("encrypted-refresh-token");
        testRefreshToken.setExpirationDate(LocalDateTime.now().plusDays(30));

        newTokens = new OAuthTokenResponse(
            "new-access-token",
            "new-refresh-token",
            3600L,
            "Bearer",
            Arrays.asList("pages_manage_posts", "pages_read_engagement")
        );
    }

    @Test
    @DisplayName("Should successfully rotate expiring tokens")
    void testRotateExpiringTokens_Success() {
        // Arrange
        List<AccessToken> expiringTokens = Arrays.asList(testAccessToken);

        when(accessTokenRepository.findTokensExpiringBefore(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(expiringTokens);
        when(pageRepository.findById(1L)).thenReturn(Optional.of(testPage));
        when(refreshTokenRepository.findValidRefreshTokenByPageId(1L, any(LocalDateTime.class)))
            .thenReturn(Optional.of(testRefreshToken));
        when(platformRepository.findById(1L)).thenReturn(Optional.of(testPlatform));
        when(strategyFactory.getStrategy("facebook")).thenReturn(mockOAuthStrategy);
        when(tokenManagementService.decryptToken("encrypted-refresh-token"))
            .thenReturn("decrypted-refresh-token");
        when(mockOAuthStrategy.refreshTokens("decrypted-refresh-token")).thenReturn(newTokens);

        // Act
        tokenRotationService.rotateExpiringTokens();

        // Assert
        verify(accessTokenRepository).findTokensExpiringBefore(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(mockOAuthStrategy).refreshTokens("decrypted-refresh-token");
        verify(tokenManagementService).updateTokens(1L, newTokens);
    }

    @Test
    @DisplayName("Should handle no expiring tokens gracefully")
    void testRotateExpiringTokens_NoExpiringTokens() {
        // Arrange
        when(accessTokenRepository.findTokensExpiringBefore(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());

        // Act
        tokenRotationService.rotateExpiringTokens();

        // Assert
        verify(accessTokenRepository).findTokensExpiringBefore(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(mockOAuthStrategy, never()).refreshTokens(any());
        verify(tokenManagementService, never()).updateTokens(any(), any());
    }

    @Test
    @DisplayName("Should successfully rotate token for specific page")
    void testRotateTokenForPage_Success() {
        // Arrange
        Long pageId = 1L;

        when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
        when(refreshTokenRepository.findValidRefreshTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(testRefreshToken));
        when(platformRepository.findById(testPage.getPlatformId())).thenReturn(Optional.of(testPlatform));
        when(strategyFactory.getStrategy(testPlatform.getLabel())).thenReturn(mockOAuthStrategy);
        when(tokenManagementService.decryptToken(testRefreshToken.getRefreshToken()))
            .thenReturn("decrypted-refresh-token");
        when(mockOAuthStrategy.refreshTokens("decrypted-refresh-token")).thenReturn(newTokens);

        // Act
        boolean result = tokenRotationService.rotateTokenForPage(pageId);

        // Assert
        assertTrue(result);
        verify(mockOAuthStrategy).refreshTokens("decrypted-refresh-token");
        verify(tokenManagementService).updateTokens(pageId, newTokens);
    }

    @Test
    @DisplayName("Should handle page with no refresh token")
    void testRotateTokenForPage_NoRefreshToken() {
        // Arrange
        Long pageId = 1L;

        when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
        when(refreshTokenRepository.findValidRefreshTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.empty());

        // Act
        boolean result = tokenRotationService.rotateTokenForPage(pageId);

        // Assert
        assertFalse(result);
        verify(mockOAuthStrategy, never()).refreshTokens(any());
        verify(tokenManagementService, never()).updateTokens(any(), any());
        assertFalse(testPage.isActive()); // Page should be marked as inactive
    }

    @Test
    @DisplayName("Should handle refresh token expiration")
    void testRotateTokenForPage_RefreshTokenExpired() {
        // Arrange
        Long pageId = 1L;

        when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
        when(refreshTokenRepository.findValidRefreshTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(testRefreshToken));
        when(platformRepository.findById(testPage.getPlatformId())).thenReturn(Optional.of(testPlatform));
        when(strategyFactory.getStrategy(testPlatform.getLabel())).thenReturn(mockOAuthStrategy);
        when(tokenManagementService.decryptToken(testRefreshToken.getRefreshToken()))
            .thenReturn("decrypted-refresh-token");
        when(mockOAuthStrategy.refreshTokens("decrypted-refresh-token"))
            .thenThrow(new TokenExpiredException("facebook", "EXPIRED_REFRESH_TOKEN", "Refresh token expired"));

        // Act
        boolean result = tokenRotationService.rotateTokenForPage(pageId);

        // Assert
        assertFalse(result);
        verify(mockOAuthStrategy).refreshTokens("decrypted-refresh-token");
        verify(tokenManagementService, never()).updateTokens(any(), any());
        assertFalse(testPage.isActive()); // Page should be marked as requiring reauth
    }

    @Test
    @DisplayName("Should handle platform API errors gracefully")
    void testRotateTokenForPage_PlatformError() {
        // Arrange
        Long pageId = 1L;

        when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
        when(refreshTokenRepository.findValidRefreshTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(testRefreshToken));
        when(platformRepository.findById(testPage.getPlatformId())).thenReturn(Optional.of(testPlatform));
        when(strategyFactory.getStrategy(testPlatform.getLabel())).thenReturn(mockOAuthStrategy);
        when(tokenManagementService.decryptToken(testRefreshToken.getRefreshToken()))
            .thenReturn("decrypted-refresh-token");
        when(mockOAuthStrategy.refreshTokens("decrypted-refresh-token"))
            .thenThrow(new RuntimeException("Platform API error"));

        // Act
        boolean result = tokenRotationService.rotateTokenForPage(pageId);

        // Assert
        assertFalse(result);
        verify(mockOAuthStrategy).refreshTokens("decrypted-refresh-token");
        verify(tokenManagementService, never()).updateTokens(any(), any());
    }

    @Nested
    @DisplayName("Token Rotation Monitoring Tests")
    class TokenRotationMonitoringTests {

        @Test
        @DisplayName("Should check if page needs token rotation")
        void testNeedsTokenRotation() {
            // Arrange
            Long pageId = 1L;
            int hoursThreshold = 24;

            when(tokenManagementService.areAccessTokensNearExpiration(pageId, hoursThreshold * 60))
                .thenReturn(true);

            // Act
            boolean result = tokenRotationService.needsTokenRotation(pageId, hoursThreshold);

            // Assert
            assertTrue(result);
            verify(tokenManagementService).areAccessTokensNearExpiration(pageId, hoursThreshold * 60);
        }

        @Test
        @DisplayName("Should check if page needs re-authentication")
        void testNeedsReAuthentication() {
            // Arrange
            Long pageId = 1L;
            int daysThreshold = 7;

            when(tokenManagementService.areRefreshTokensNearExpiration(pageId, daysThreshold))
                .thenReturn(true);

            // Act
            boolean result = tokenRotationService.needsReAuthentication(pageId, daysThreshold);

            // Assert
            assertTrue(result);
            verify(tokenManagementService).areRefreshTokensNearExpiration(pageId, daysThreshold);
        }

        @Test
        @DisplayName("Should get pages needing rotation")
        void testGetPagesNeedingRotation() {
            // Arrange
            int hoursThreshold = 24;
            LocalDateTime threshold = LocalDateTime.now().plusHours(hoursThreshold);
            List<AccessToken> expiringTokens = Arrays.asList(testAccessToken);

            when(accessTokenRepository.findTokensExpiringBefore(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expiringTokens);

            // Act
            List<Long> result = tokenRotationService.getPagesNeedingRotation(hoursThreshold);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0));
        }

        @Test
        @DisplayName("Should get pages needing re-authentication")
        void testGetPagesNeedingReAuthentication() {
            // Arrange
            int daysThreshold = 7;
            List<Long> pageIds = Arrays.asList(1L, 2L);

            when(refreshTokenRepository.findPageIdsWithExpiringRefreshTokens(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(pageIds);

            // Act
            List<Long> result = tokenRotationService.getPagesNeedingReAuthentication(daysThreshold);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.contains(1L));
            assertTrue(result.contains(2L));
        }

        @Test
        @DisplayName("Should generate rotation statistics")
        void testGetRotationStatistics() {
            // Arrange
            List<ManagedPage> activePages = Arrays.asList(testPage);
            when(pageRepository.findAll()).thenReturn(activePages);
            when(tokenManagementService.hasValidAccessTokens(1L)).thenReturn(true);
            when(tokenManagementService.hasValidRefreshTokens(1L)).thenReturn(true);

            // Act
            TokenRotationService.TokenRotationStatistics result = tokenRotationService.getRotationStatistics();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalActivePages());
            assertEquals(1, result.getPagesWithHealthyTokens());
            assertEquals(100.0, result.getHealthyTokenPercentage(), 0.01);
        }
    }

    @Nested
    @DisplayName("Bulk Operations and Error Handling Tests")
    class BulkOperationsTests {

        @Test
        @DisplayName("Should handle multiple pages token rotation")
        void testMultiplePageTokenRotation() {
            // Arrange
            AccessToken token1 = new AccessToken();
            token1.setManagedPageId(1L);
            AccessToken token2 = new AccessToken();
            token2.setManagedPageId(2L);

            List<AccessToken> expiringTokens = Arrays.asList(token1, token2);

            when(accessTokenRepository.findTokensExpiringBefore(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expiringTokens);

            // Setup mocks for first page
            when(pageRepository.findById(1L)).thenReturn(Optional.of(testPage));
            when(refreshTokenRepository.findValidRefreshTokenByPageId(1L, any(LocalDateTime.class)))
                .thenReturn(Optional.of(testRefreshToken));
            when(platformRepository.findById(1L)).thenReturn(Optional.of(testPlatform));

            // Setup mocks for second page
            ManagedPage testPage2 = new ManagedPage();
            testPage2.setId(2L);
            testPage2.setPlatformId(1L);
            testPage2.setActive();

            RefreshToken testRefreshToken2 = new RefreshToken();
            testRefreshToken2.setRefreshToken("encrypted-refresh-token-2");

            when(pageRepository.findById(2L)).thenReturn(Optional.of(testPage2));
            when(refreshTokenRepository.findValidRefreshTokenByPageId(2L, any(LocalDateTime.class)))
                .thenReturn(Optional.of(testRefreshToken2));

            when(strategyFactory.getStrategy("facebook")).thenReturn(mockOAuthStrategy);
            when(tokenManagementService.decryptToken(anyString())).thenReturn("decrypted-token");
            when(mockOAuthStrategy.refreshTokens(anyString())).thenReturn(newTokens);

            // Act
            tokenRotationService.rotateExpiringTokens();

            // Assert
            verify(mockOAuthStrategy, times(2)).refreshTokens(anyString());
            verify(tokenManagementService, times(2)).updateTokens(any(), any());
        }

        @Test
        @DisplayName("Should handle partial failures in bulk rotation")
        void testBulkRotation_PartialFailures() {
            // Arrange
            AccessToken token1 = new AccessToken();
            token1.setManagedPageId(1L);
            AccessToken token2 = new AccessToken();
            token2.setManagedPageId(2L);
            AccessToken token3 = new AccessToken();
            token3.setManagedPageId(3L);

            List<AccessToken> expiringTokens = Arrays.asList(token1, token2, token3);

            when(accessTokenRepository.findTokensExpiringBefore(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expiringTokens);

            // Setup success for page 1
            when(pageRepository.findById(1L)).thenReturn(Optional.of(testPage));
            when(refreshTokenRepository.findValidRefreshTokenByPageId(1L, any(LocalDateTime.class)))
                .thenReturn(Optional.of(testRefreshToken));
            when(platformRepository.findById(1L)).thenReturn(Optional.of(testPlatform));

            // Setup failure for page 2 (no refresh token)
            ManagedPage testPage2 = new ManagedPage();
            testPage2.setId(2L);
            testPage2.setPlatformId(1L);
            testPage2.setActive();

            when(pageRepository.findById(2L)).thenReturn(Optional.of(testPage2));
            when(refreshTokenRepository.findValidRefreshTokenByPageId(2L, any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

            // Setup failure for page 3 (refresh token expired)
            ManagedPage testPage3 = new ManagedPage();
            testPage3.setId(3L);
            testPage3.setPlatformId(1L);
            testPage3.setActive();

            RefreshToken testRefreshToken3 = new RefreshToken();
            testRefreshToken3.setRefreshToken("encrypted-refresh-token-3");

            when(pageRepository.findById(3L)).thenReturn(Optional.of(testPage3));
            when(refreshTokenRepository.findValidRefreshTokenByPageId(3L, any(LocalDateTime.class)))
                .thenReturn(Optional.of(testRefreshToken3));

            when(strategyFactory.getStrategy("facebook")).thenReturn(mockOAuthStrategy);
            when(tokenManagementService.decryptToken("encrypted-refresh-token")).thenReturn("decrypted-token-1");
            when(tokenManagementService.decryptToken("encrypted-refresh-token-3")).thenReturn("decrypted-token-3");
            when(mockOAuthStrategy.refreshTokens("decrypted-token-1")).thenReturn(newTokens);
            when(mockOAuthStrategy.refreshTokens("decrypted-token-3"))
                .thenThrow(new TokenExpiredException("facebook", "EXPIRED", "Token expired"));

            // Act
            tokenRotationService.rotateExpiringTokens();

            // Assert
            // Page 1 should succeed
            verify(tokenManagementService, times(1)).updateTokens(1L, newTokens);

            // Page 2 should be marked as inactive (no refresh token)
            assertFalse(testPage2.isActive());

            // Page 3 should be marked as inactive (refresh token expired)
            assertFalse(testPage3.isActive());
        }

        @Test
        @DisplayName("Should handle graceful failure during rotation")
        void testRotationFailureHandling() {
            // Arrange
            List<AccessToken> expiringTokens = Arrays.asList(testAccessToken);

            when(accessTokenRepository.findTokensExpiringBefore(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expiringTokens);
            when(pageRepository.findById(1L)).thenReturn(Optional.of(testPage));
            when(refreshTokenRepository.findValidRefreshTokenByPageId(1L, any(LocalDateTime.class)))
                .thenReturn(Optional.of(testRefreshToken));
            when(platformRepository.findById(1L)).thenReturn(Optional.of(testPlatform));
            when(strategyFactory.getStrategy("facebook")).thenReturn(mockOAuthStrategy);
            when(tokenManagementService.decryptToken("encrypted-refresh-token"))
                .thenReturn("decrypted-refresh-token");
            when(mockOAuthStrategy.refreshTokens("decrypted-refresh-token"))
                .thenThrow(new RuntimeException("Network error"));

            // Act - Should not throw exception, should handle gracefully
            assertDoesNotThrow(() -> {
                tokenRotationService.rotateExpiringTokens();
            });

            // Assert
            verify(mockOAuthStrategy).refreshTokens("decrypted-refresh-token");
            verify(tokenManagementService, never()).updateTokens(any(), any());
        }

        @Test
        @DisplayName("Should handle database connection failures")
        void testDatabaseConnectionFailure() {
            // Arrange
            when(accessTokenRepository.findTokensExpiringBefore(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

            // Act & Assert
            assertDoesNotThrow(() -> {
                tokenRotationService.rotateExpiringTokens();
            });
        }

        @Test
        @DisplayName("Should handle force rotation for specific page")
        void testForceRotateTokenForPage() {
            // Arrange
            Long pageId = 1L;

            when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
            when(refreshTokenRepository.findValidRefreshTokenByPageId(pageId, any(LocalDateTime.class)))
                .thenReturn(Optional.of(testRefreshToken));
            when(platformRepository.findById(testPage.getPlatformId())).thenReturn(Optional.of(testPlatform));
            when(strategyFactory.getStrategy(testPlatform.getLabel())).thenReturn(mockOAuthStrategy);
            when(tokenManagementService.decryptToken(testRefreshToken.getRefreshToken()))
                .thenReturn("decrypted-refresh-token");
            when(mockOAuthStrategy.refreshTokens("decrypted-refresh-token")).thenReturn(newTokens);

            // Act
            boolean result = tokenRotationService.forceRotateTokenForPage(pageId);

            // Assert
            assertTrue(result);
            verify(mockOAuthStrategy).refreshTokens("decrypted-refresh-token");
            verify(tokenManagementService).updateTokens(pageId, newTokens);
        }
    }
}