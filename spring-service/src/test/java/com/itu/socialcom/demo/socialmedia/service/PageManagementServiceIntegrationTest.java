package com.itu.socialcom.demo.socialmedia.service;

import com.itu.socialcom.demo.socialmedia.dto.SocialMediaPage;
import com.itu.socialcom.demo.socialmedia.entity.AccessToken;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import com.itu.socialcom.demo.socialmedia.entity.RefreshToken;
import com.itu.socialcom.demo.socialmedia.entity.SupportedPlatform;
import com.itu.socialcom.demo.socialmedia.exception.OAuthException;
import com.itu.socialcom.demo.socialmedia.exception.TokenExpiredException;
import com.itu.socialcom.demo.socialmedia.exception.UnsupportedPlatformException;
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
 * Integration tests for PageManagementService.
 * Tests complete OAuth flows with mock external services, page connection and disconnection workflows,
 * and error handling and recovery mechanisms.
 * 
 * Requirements covered:
 * - 2.1: Page connection and management workflows
 * - 3.1: Page disconnection workflows  
 * - 4.1: Token management and refresh scenarios
 * - 6.1: Error handling and recovery mechanisms
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class PageManagementServiceIntegrationTest {

    @Mock
    private ManagedPageRepository pageRepository;

    @Mock
    private AccessTokenRepository accessTokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private SupportedPlatformRepository platformRepository;

    @Mock
    private OAuthStrategyFactory strategyFactory;

    @Mock
    private TokenManagementService tokenManagementService;

    @Mock
    private OAuthStrategy mockOAuthStrategy;

    private PageManagementServiceImpl pageManagementService;

    private SupportedPlatform testPlatform;
    private ManagedPage testPage;
    private OAuthTokenResponse testTokens;
    private SocialMediaPage testSocialMediaPage;

    @BeforeEach
    void setUp() {
        pageManagementService = new PageManagementServiceImpl(
            pageRepository, accessTokenRepository, refreshTokenRepository,
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

        testTokens = new OAuthTokenResponse(
            "test-access-token",
            "test-refresh-token",
            3600L,
            "Bearer",
            Arrays.asList("pages_manage_posts", "pages_read_engagement")
        );

        testSocialMediaPage = new SocialMediaPage();
        testSocialMediaPage.setId("test-page-id");
        testSocialMediaPage.setName("Test Page");
        testSocialMediaPage.setPlatform("facebook");
        testSocialMediaPage.setAccessToken("test-page-access-token");
        testSocialMediaPage.setCategory("Business");
    }

    @Test
    void testCompleteOAuthFlow_Success() {
        // Arrange
        Long sellerId = 100L;
        String platform = "facebook";
        String authCode = "test-auth-code";
        String state = "test-state";

        when(platformRepository.findByLabelIgnoreCase(platform)).thenReturn(Optional.of(testPlatform));
        when(strategyFactory.getStrategy(platform)).thenReturn(mockOAuthStrategy);
        when(mockOAuthStrategy.exchangeCodeForTokens(authCode, state)).thenReturn(testTokens);
        when(mockOAuthStrategy.getUserPages(testTokens.getAccessToken()))
            .thenReturn(Arrays.asList(testSocialMediaPage));
        when(pageRepository.existsBySellerAndPlatformIdentifier(sellerId, "test-page-id", 1L))
            .thenReturn(false);
        when(pageRepository.save(any(ManagedPage.class))).thenReturn(testPage);

        // Act
        ManagedPage result = pageManagementService.connectPage(sellerId, platform, authCode, state);

        // Assert
        assertNotNull(result);
        assertEquals(testPage.getId(), result.getId());
        verify(strategyFactory).getStrategy(platform);
        verify(mockOAuthStrategy).exchangeCodeForTokens(authCode, state);
        verify(mockOAuthStrategy).getUserPages(testTokens.getAccessToken());
        verify(tokenManagementService).storeTokens(eq(testPage.getId()), any(OAuthTokenResponse.class), eq(1L), eq(platform));
        verify(pageRepository).save(any(ManagedPage.class));
    }

    @Test
    void testCompleteOAuthFlow_UnsupportedPlatform() {
        // Arrange
        Long sellerId = 100L;
        String platform = "unsupported";
        String authCode = "test-auth-code";
        String state = "test-state";

        when(platformRepository.findByLabelIgnoreCase(platform)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnsupportedPlatformException.class, () -> {
            pageManagementService.connectPage(sellerId, platform, authCode, state);
        });

        verify(strategyFactory, never()).getStrategy(any());
        verify(tokenManagementService, never()).storeTokens(any(), any(), any(), any());
    }

    @Test
    void testCompleteOAuthFlow_TokenExchangeFailure() {
        // Arrange
        Long sellerId = 100L;
        String platform = "facebook";
        String authCode = "invalid-code";
        String state = "test-state";

        when(platformRepository.findByLabelIgnoreCase(platform)).thenReturn(Optional.of(testPlatform));
        when(strategyFactory.getStrategy(platform)).thenReturn(mockOAuthStrategy);
        when(mockOAuthStrategy.exchangeCodeForTokens(authCode, state))
            .thenThrow(new OAuthException(platform, "INVALID_CODE", "Invalid authorization code"));

        // Act & Assert
        OAuthException exception = assertThrows(OAuthException.class, () -> {
            pageManagementService.connectPage(sellerId, platform, authCode, state);
        });

        assertEquals("CONNECTION_FAILED", exception.getErrorCode());
        verify(mockOAuthStrategy).exchangeCodeForTokens(authCode, state);
        verify(mockOAuthStrategy, never()).getUserPages(any());
        verify(tokenManagementService, never()).storeTokens(any(), any(), any(), any());
    }

    @Test
    void testCompleteOAuthFlow_NoPagesAvailable() {
        // Arrange
        Long sellerId = 100L;
        String platform = "facebook";
        String authCode = "test-auth-code";
        String state = "test-state";

        when(platformRepository.findByLabelIgnoreCase(platform)).thenReturn(Optional.of(testPlatform));
        when(strategyFactory.getStrategy(platform)).thenReturn(mockOAuthStrategy);
        when(mockOAuthStrategy.exchangeCodeForTokens(authCode, state)).thenReturn(testTokens);
        when(mockOAuthStrategy.getUserPages(testTokens.getAccessToken())).thenReturn(Arrays.asList());

        // Act & Assert
        OAuthException exception = assertThrows(OAuthException.class, () -> {
            pageManagementService.connectPage(sellerId, platform, authCode, state);
        });

        assertEquals("NO_PAGES", exception.getErrorCode());
        verify(mockOAuthStrategy).getUserPages(testTokens.getAccessToken());
        verify(tokenManagementService, never()).storeTokens(any(), any(), any(), any());
    }

    @Test
    void testPageDisconnection_Success() {
        // Arrange
        Long sellerId = 100L;
        Long pageId = 1L;

        AccessToken accessToken = new AccessToken();
        accessToken.setAccessToken("encrypted-token");

        when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
        when(platformRepository.findById(testPage.getPlatformId())).thenReturn(Optional.of(testPlatform));
        when(strategyFactory.getStrategy(testPlatform.getLabel())).thenReturn(mockOAuthStrategy);
        when(accessTokenRepository.findValidTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(accessToken));

        // Act
        pageManagementService.disconnectPage(sellerId, pageId);

        // Assert
        verify(mockOAuthStrategy).revokeAccess(accessToken.getAccessToken());
        verify(pageRepository).save(testPage);
        verify(accessTokenRepository).deleteByManagedPageId(pageId);
        verify(refreshTokenRepository).deleteByManagedPageId(pageId);
        assertFalse(testPage.isActive());
    }

    @Test
    void testPageDisconnection_PageNotFound() {
        // Arrange
        Long sellerId = 100L;
        Long pageId = 999L;

        when(pageRepository.findById(pageId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            pageManagementService.disconnectPage(sellerId, pageId);
        });

        verify(mockOAuthStrategy, never()).revokeAccess(any());
        verify(accessTokenRepository, never()).deleteByManagedPageId(any());
    }

    @Test
    void testPageDisconnection_WrongSeller() {
        // Arrange
        Long sellerId = 200L; // Different seller
        Long pageId = 1L;

        when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            pageManagementService.disconnectPage(sellerId, pageId);
        });

        verify(mockOAuthStrategy, never()).revokeAccess(any());
        verify(accessTokenRepository, never()).deleteByManagedPageId(any());
    }

    @Test
    void testPageDisconnection_PlatformRevocationFailure() {
        // Arrange
        Long sellerId = 100L;
        Long pageId = 1L;

        AccessToken accessToken = new AccessToken();
        accessToken.setAccessToken("encrypted-token");

        when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
        when(platformRepository.findById(testPage.getPlatformId())).thenReturn(Optional.of(testPlatform));
        when(strategyFactory.getStrategy(testPlatform.getLabel())).thenReturn(mockOAuthStrategy);
        when(accessTokenRepository.findValidTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(accessToken));
        doThrow(new RuntimeException("Platform API error")).when(mockOAuthStrategy).revokeAccess(any());

        // Act - Should not throw exception, should continue with local cleanup
        assertDoesNotThrow(() -> {
            pageManagementService.disconnectPage(sellerId, pageId);
        });

        // Assert - Local cleanup should still happen
        verify(pageRepository).save(testPage);
        verify(accessTokenRepository).deleteByManagedPageId(pageId);
        verify(refreshTokenRepository).deleteByManagedPageId(pageId);
        assertFalse(testPage.isActive());
    }

    @Test
    void testTokenRefresh_Success() {
        // Arrange
        Long pageId = 1L;
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshToken("encrypted-refresh-token");

        when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
        when(platformRepository.findById(testPage.getPlatformId())).thenReturn(Optional.of(testPlatform));
        when(refreshTokenRepository.findValidRefreshTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(refreshToken));
        when(strategyFactory.getStrategy(testPlatform.getLabel())).thenReturn(mockOAuthStrategy);
        when(mockOAuthStrategy.refreshTokens(refreshToken.getRefreshToken())).thenReturn(testTokens);

        // Act
        pageManagementService.refreshPageTokens(pageId);

        // Assert
        verify(mockOAuthStrategy).refreshTokens(refreshToken.getRefreshToken());
        verify(tokenManagementService).updateTokens(pageId, testTokens);
        verify(pageRepository).save(testPage);
        assertTrue(testPage.isActive());
    }

    @Test
    void testTokenRefresh_NoRefreshToken() {
        // Arrange
        Long pageId = 1L;

        when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
        when(platformRepository.findById(testPage.getPlatformId())).thenReturn(Optional.of(testPlatform));
        when(refreshTokenRepository.findValidRefreshTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.empty());

        // Act
        pageManagementService.refreshPageTokens(pageId);

        // Assert
        verify(mockOAuthStrategy, never()).refreshTokens(any());
        verify(tokenManagementService, never()).updateTokens(any(), any());
        verify(pageRepository).save(testPage);
        assertFalse(testPage.isActive());
    }

    @Test
    void testTokenRefresh_RefreshTokenExpired() {
        // Arrange
        Long pageId = 1L;
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshToken("encrypted-refresh-token");

        when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
        when(platformRepository.findById(testPage.getPlatformId())).thenReturn(Optional.of(testPlatform));
        when(refreshTokenRepository.findValidRefreshTokenByPageId(pageId, any(LocalDateTime.class)))
            .thenReturn(Optional.of(refreshToken));
        when(strategyFactory.getStrategy(testPlatform.getLabel())).thenReturn(mockOAuthStrategy);
        when(mockOAuthStrategy.refreshTokens(refreshToken.getRefreshToken()))
            .thenThrow(new TokenExpiredException("facebook", "EXPIRED_REFRESH_TOKEN", "Refresh token expired"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pageManagementService.refreshPageTokens(pageId);
        });

        assertTrue(exception.getMessage().contains("Failed to refresh tokens"));
        verify(pageRepository, times(2)).save(testPage); // Once for marking inactive, once in exception handling
        assertFalse(testPage.isActive());
    }

    @Test
    void testGetConnectedPages_Success() {
        // Arrange
        Long sellerId = 100L;
        LocalDateTime now = LocalDateTime.now();

        when(pageRepository.findBySellerIdCustom(sellerId)).thenReturn(Arrays.asList(testPage));
        when(platformRepository.findById(testPage.getPlatformId())).thenReturn(Optional.of(testPlatform));
        when(accessTokenRepository.hasValidTokens(testPage.getId(), any(LocalDateTime.class))).thenReturn(true);
        when(refreshTokenRepository.hasValidRefreshTokens(testPage.getId(), any(LocalDateTime.class))).thenReturn(true);

        // Act
        List<ManagedPageWithDetails> result = pageManagementService.getConnectedPages(sellerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ManagedPageWithDetails pageDetails = result.get(0);
        assertEquals(testPage.getId(), pageDetails.getPage().getId());
        assertEquals(testPlatform.getLabel(), pageDetails.getPlatform().getLabel());
        assertTrue(pageDetails.isHasValidAccessToken());
        assertTrue(pageDetails.isHasValidRefreshToken());
        assertEquals(PageConnectionStatus.CONNECTED, pageDetails.getConnectionStatus());
    }

    @Test
    void testGetConnectedPages_ExpiredTokens() {
        // Arrange
        Long sellerId = 100L;

        when(pageRepository.findBySellerIdCustom(sellerId)).thenReturn(Arrays.asList(testPage));
        when(platformRepository.findById(testPage.getPlatformId())).thenReturn(Optional.of(testPlatform));
        when(accessTokenRepository.hasValidTokens(testPage.getId(), any(LocalDateTime.class))).thenReturn(false);
        when(refreshTokenRepository.hasValidRefreshTokens(testPage.getId(), any(LocalDateTime.class))).thenReturn(true);

        // Act
        List<ManagedPageWithDetails> result = pageManagementService.getConnectedPages(sellerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ManagedPageWithDetails pageDetails = result.get(0);
        assertFalse(pageDetails.isHasValidAccessToken());
        assertTrue(pageDetails.isHasValidRefreshToken());
        assertEquals(PageConnectionStatus.TOKEN_EXPIRED, pageDetails.getConnectionStatus());
    }

    @Test
    void testGetConnectedPages_RequiresReauth() {
        // Arrange
        Long sellerId = 100L;

        when(pageRepository.findBySellerIdCustom(sellerId)).thenReturn(Arrays.asList(testPage));
        when(platformRepository.findById(testPage.getPlatformId())).thenReturn(Optional.of(testPlatform));
        when(accessTokenRepository.hasValidTokens(testPage.getId(), any(LocalDateTime.class))).thenReturn(false);
        when(refreshTokenRepository.hasValidRefreshTokens(testPage.getId(), any(LocalDateTime.class))).thenReturn(false);

        // Act
        List<ManagedPageWithDetails> result = pageManagementService.getConnectedPages(sellerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ManagedPageWithDetails pageDetails = result.get(0);
        assertFalse(pageDetails.isHasValidAccessToken());
        assertFalse(pageDetails.isHasValidRefreshToken());
        assertEquals(PageConnectionStatus.AUTHENTICATION_REQUIRED, pageDetails.getConnectionStatus());
    }

    @Test
    void testConnectSpecificPage_Success() {
        // Arrange
        Long sellerId = 100L;
        String platform = "facebook";
        String pageId = "test-page-id";
        String accessToken = "test-access-token";

        when(platformRepository.findByLabelIgnoreCase(platform)).thenReturn(Optional.of(testPlatform));
        when(pageRepository.existsBySellerAndPlatformIdentifier(sellerId, pageId, testPlatform.getId()))
            .thenReturn(false);
        when(strategyFactory.getStrategy(platform)).thenReturn(mockOAuthStrategy);
        when(mockOAuthStrategy.getUserPages(accessToken)).thenReturn(Arrays.asList(testSocialMediaPage));
        when(pageRepository.save(any(ManagedPage.class))).thenReturn(testPage);

        // Act
        ManagedPage result = pageManagementService.connectSpecificPage(sellerId, platform, pageId, accessToken);

        // Assert
        assertNotNull(result);
        assertEquals(testPage.getId(), result.getId());
        verify(mockOAuthStrategy).getUserPages(accessToken);
        verify(tokenManagementService).storeTokens(eq(testPage.getId()), any(OAuthTokenResponse.class), eq(testPlatform.getId()), eq(platform));
        verify(pageRepository).save(any(ManagedPage.class));
    }

    @Test
    void testConnectSpecificPage_AlreadyConnected() {
        // Arrange
        Long sellerId = 100L;
        String platform = "facebook";
        String pageId = "test-page-id";
        String accessToken = "test-access-token";

        when(platformRepository.findByLabelIgnoreCase(platform)).thenReturn(Optional.of(testPlatform));
        when(pageRepository.existsBySellerAndPlatformIdentifier(sellerId, pageId, testPlatform.getId()))
            .thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            pageManagementService.connectSpecificPage(sellerId, platform, pageId, accessToken);
        });

        verify(mockOAuthStrategy, never()).getUserPages(any());
        verify(tokenManagementService, never()).storeTokens(any(), any(), any(), any());
    }

    @Test
    void testConnectSpecificPage_PageNotAccessible() {
        // Arrange
        Long sellerId = 100L;
        String platform = "facebook";
        String pageId = "inaccessible-page-id";
        String accessToken = "test-access-token";

        when(platformRepository.findByLabelIgnoreCase(platform)).thenReturn(Optional.of(testPlatform));
        when(pageRepository.existsBySellerAndPlatformIdentifier(sellerId, pageId, testPlatform.getId()))
            .thenReturn(false);
        when(strategyFactory.getStrategy(platform)).thenReturn(mockOAuthStrategy);
        when(mockOAuthStrategy.getUserPages(accessToken)).thenReturn(Arrays.asList(testSocialMediaPage));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            pageManagementService.connectSpecificPage(sellerId, platform, pageId, accessToken);
        });

        verify(mockOAuthStrategy).getUserPages(accessToken);
        verify(tokenManagementService, never()).storeTokens(any(), any(), any(), any());
    }

    @Test
    void testGetPageStatus_Active() {
        // Arrange
        Long pageId = 1L;

        when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
        when(accessTokenRepository.hasValidTokens(pageId, any(LocalDateTime.class))).thenReturn(true);
        when(refreshTokenRepository.hasValidRefreshTokens(pageId, any(LocalDateTime.class))).thenReturn(true);

        // Act
        PageConnectionStatus result = pageManagementService.getPageStatus(pageId);

        // Assert
        assertEquals(PageConnectionStatus.CONNECTED, result);
    }

    @Test
    void testGetPageStatus_RequiresRefresh() {
        // Arrange
        Long pageId = 1L;

        when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
        when(accessTokenRepository.hasValidTokens(pageId, any(LocalDateTime.class))).thenReturn(false);
        when(refreshTokenRepository.hasValidRefreshTokens(pageId, any(LocalDateTime.class))).thenReturn(true);

        // Act
        PageConnectionStatus result = pageManagementService.getPageStatus(pageId);

        // Assert
        assertEquals(PageConnectionStatus.TOKEN_EXPIRED, result);
    }

    @Test
    void testGetPageStatus_RequiresReauth() {
        // Arrange
        Long pageId = 1L;

        when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
        when(accessTokenRepository.hasValidTokens(pageId, any(LocalDateTime.class))).thenReturn(false);
        when(refreshTokenRepository.hasValidRefreshTokens(pageId, any(LocalDateTime.class))).thenReturn(false);

        // Act
        PageConnectionStatus result = pageManagementService.getPageStatus(pageId);

        // Assert
        assertEquals(PageConnectionStatus.AUTHENTICATION_REQUIRED, result);
    }

    @Test
    void testGetPageStatus_Unknown() {
        // Arrange
        Long pageId = 999L;

        when(pageRepository.findById(pageId)).thenReturn(Optional.empty());

        // Act
        PageConnectionStatus result = pageManagementService.getPageStatus(pageId);

        // Assert
        assertEquals(PageConnectionStatus.UNKNOWN, result);
    }

    @Test
    void testGetAvailablePages_Success() {
        // Arrange
        Long sellerId = 100L;
        String platform = "facebook";
        String accessToken = "test-access-token";

        when(strategyFactory.getStrategy(platform)).thenReturn(mockOAuthStrategy);
        when(mockOAuthStrategy.getUserPages(accessToken)).thenReturn(Arrays.asList(testSocialMediaPage));

        // Act
        List<SocialMediaPage> result = pageManagementService.getAvailablePages(sellerId, platform, accessToken);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSocialMediaPage.getId(), result.get(0).getId());
        verify(mockOAuthStrategy).getUserPages(accessToken);
    }

    @Test
    void testGetAvailablePages_PlatformError() {
        // Arrange
        Long sellerId = 100L;
        String platform = "facebook";
        String accessToken = "invalid-token";

        when(strategyFactory.getStrategy(platform)).thenReturn(mockOAuthStrategy);
        when(mockOAuthStrategy.getUserPages(accessToken))
            .thenThrow(new RuntimeException("Platform API error"));

        // Act & Assert
        OAuthException exception = assertThrows(OAuthException.class, () -> {
            pageManagementService.getAvailablePages(sellerId, platform, accessToken);
        });

        assertEquals("PAGES_FETCH_FAILED", exception.getErrorCode());
        verify(mockOAuthStrategy).getUserPages(accessToken);
    }

    @Nested
    @DisplayName("Complete OAuth Flow Integration Tests")
    class CompleteOAuthFlowTests {

        @Test
        @DisplayName("Should handle complete OAuth flow with multiple pages")
        void testCompleteOAuthFlow_MultiplePagesAvailable() {
            // Arrange
            Long sellerId = 100L;
            String platform = "facebook";
            String authCode = "test-auth-code";
            String state = "test-state";

            SocialMediaPage page1 = new SocialMediaPage();
            page1.setId("page-1");
            page1.setName("Page 1");
            page1.setPlatform("facebook");

            SocialMediaPage page2 = new SocialMediaPage();
            page2.setId("page-2");
            page2.setName("Page 2");
            page2.setPlatform("facebook");

            when(platformRepository.findByLabelIgnoreCase(platform)).thenReturn(Optional.of(testPlatform));
            when(strategyFactory.getStrategy(platform)).thenReturn(mockOAuthStrategy);
            when(mockOAuthStrategy.exchangeCodeForTokens(authCode, state)).thenReturn(testTokens);
            when(mockOAuthStrategy.getUserPages(testTokens.getAccessToken()))
                .thenReturn(Arrays.asList(page1, page2));
            when(pageRepository.existsBySellerAndPlatformIdentifier(sellerId, "page-1", 1L))
                .thenReturn(false);
            when(pageRepository.save(any(ManagedPage.class))).thenReturn(testPage);

            // Act
            ManagedPage result = pageManagementService.connectPage(sellerId, platform, authCode, state);

            // Assert
            assertNotNull(result);
            verify(mockOAuthStrategy).getUserPages(testTokens.getAccessToken());
            verify(tokenManagementService).storeTokens(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should handle OAuth flow with network timeout")
        void testCompleteOAuthFlow_NetworkTimeout() {
            // Arrange
            Long sellerId = 100L;
            String platform = "facebook";
            String authCode = "test-auth-code";
            String state = "test-state";

            when(platformRepository.findByLabelIgnoreCase(platform)).thenReturn(Optional.of(testPlatform));
            when(strategyFactory.getStrategy(platform)).thenReturn(mockOAuthStrategy);
            when(mockOAuthStrategy.exchangeCodeForTokens(authCode, state))
                .thenThrow(new RuntimeException("Network timeout"));

            // Act & Assert
            OAuthException exception = assertThrows(OAuthException.class, () -> {
                pageManagementService.connectPage(sellerId, platform, authCode, state);
            });

            assertEquals("CONNECTION_FAILED", exception.getErrorCode());
            assertTrue(exception.getMessage().contains("Network timeout"));
        }

        @Test
        @DisplayName("Should handle OAuth flow with invalid state parameter")
        void testCompleteOAuthFlow_InvalidState() {
            // Arrange
            Long sellerId = 100L;
            String platform = "facebook";
            String authCode = "test-auth-code";
            String state = "invalid-state";

            when(platformRepository.findByLabelIgnoreCase(platform)).thenReturn(Optional.of(testPlatform));
            when(strategyFactory.getStrategy(platform)).thenReturn(mockOAuthStrategy);
            when(mockOAuthStrategy.exchangeCodeForTokens(authCode, state))
                .thenThrow(new OAuthException(platform, "INVALID_STATE", "State parameter mismatch"));

            // Act & Assert
            OAuthException exception = assertThrows(OAuthException.class, () -> {
                pageManagementService.connectPage(sellerId, platform, authCode, state);
            });

            assertEquals("CONNECTION_FAILED", exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("Page Connection and Disconnection Workflow Tests")
    class PageConnectionWorkflowTests {

        @Test
        @DisplayName("Should handle page connection with existing tokens")
        void testPageConnection_WithExistingTokens() {
            // Arrange
            Long sellerId = 100L;
            String platform = "facebook";
            String pageId = "existing-page-id";
            String accessToken = "existing-access-token";

            when(platformRepository.findByLabelIgnoreCase(platform)).thenReturn(Optional.of(testPlatform));
            when(pageRepository.existsBySellerAndPlatformIdentifier(sellerId, pageId, testPlatform.getId()))
                .thenReturn(false);
            when(strategyFactory.getStrategy(platform)).thenReturn(mockOAuthStrategy);
            when(mockOAuthStrategy.getUserPages(accessToken)).thenReturn(Arrays.asList(testSocialMediaPage));
            when(pageRepository.save(any(ManagedPage.class))).thenReturn(testPage);

            // Act
            ManagedPage result = pageManagementService.connectSpecificPage(sellerId, platform, pageId, accessToken);

            // Assert
            assertNotNull(result);
            verify(tokenManagementService).storeTokens(any(), any(), any(), any());
            assertTrue(result.isActive());
        }

        @Test
        @DisplayName("Should handle bulk page disconnection")
        void testBulkPageDisconnection() {
            // Arrange
            Long sellerId = 100L;
            List<Long> pageIds = Arrays.asList(1L, 2L, 3L);

            ManagedPage page1 = createTestPage(1L, sellerId);
            ManagedPage page2 = createTestPage(2L, sellerId);
            ManagedPage page3 = createTestPage(3L, sellerId);

            when(pageRepository.findById(1L)).thenReturn(Optional.of(page1));
            when(pageRepository.findById(2L)).thenReturn(Optional.of(page2));
            when(pageRepository.findById(3L)).thenReturn(Optional.of(page3));
            when(platformRepository.findById(any())).thenReturn(Optional.of(testPlatform));
            when(strategyFactory.getStrategy(any())).thenReturn(mockOAuthStrategy);
            when(accessTokenRepository.findValidTokenByPageId(any(), any())).thenReturn(Optional.empty());

            // Act
            for (Long pageId : pageIds) {
                pageManagementService.disconnectPage(sellerId, pageId);
            }

            // Assert
            verify(pageRepository, times(3)).save(any(ManagedPage.class));
            verify(accessTokenRepository, times(3)).deleteByManagedPageId(any());
            verify(refreshTokenRepository, times(3)).deleteByManagedPageId(any());
        }

        @Test
        @DisplayName("Should handle page disconnection with concurrent access")
        void testPageDisconnection_ConcurrentAccess() {
            // Arrange
            Long sellerId = 100L;
            Long pageId = 1L;

            when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
            when(platformRepository.findById(testPage.getPlatformId())).thenReturn(Optional.of(testPlatform));
            when(strategyFactory.getStrategy(testPlatform.getLabel())).thenReturn(mockOAuthStrategy);
            when(accessTokenRepository.findValidTokenByPageId(pageId, any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

            // Simulate concurrent modification
            doThrow(new RuntimeException("Concurrent modification")).when(pageRepository).save(any());

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                pageManagementService.disconnectPage(sellerId, pageId);
            });
        }

        private ManagedPage createTestPage(Long id, Long sellerId) {
            ManagedPage page = new ManagedPage();
            page.setId(id);
            page.setSellerId(sellerId);
            page.setPlatformId(1L);
            page.setPlatformIdentifier("test-page-" + id);
            page.setPageTitle("Test Page " + id);
            page.setActive();
            return page;
        }
    }

    @Nested
    @DisplayName("Token Management and Rotation Tests")
    class TokenManagementTests {

        @Test
        @DisplayName("Should handle token refresh with expired access token")
        void testTokenRefresh_ExpiredAccessToken() {
            // Arrange
            Long pageId = 1L;
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setRefreshToken("encrypted-refresh-token");

            OAuthTokenResponse newTokens = new OAuthTokenResponse(
                "new-access-token", "new-refresh-token", 3600L, "Bearer", null
            );

            when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
            when(platformRepository.findById(testPage.getPlatformId())).thenReturn(Optional.of(testPlatform));
            when(refreshTokenRepository.findValidRefreshTokenByPageId(pageId, any(LocalDateTime.class)))
                .thenReturn(Optional.of(refreshToken));
            when(strategyFactory.getStrategy(testPlatform.getLabel())).thenReturn(mockOAuthStrategy);
            when(mockOAuthStrategy.refreshTokens(refreshToken.getRefreshToken())).thenReturn(newTokens);

            // Act
            pageManagementService.refreshPageTokens(pageId);

            // Assert
            verify(mockOAuthStrategy).refreshTokens(refreshToken.getRefreshToken());
            verify(tokenManagementService).updateTokens(pageId, newTokens);
            assertTrue(testPage.isActive());
        }

        @Test
        @DisplayName("Should handle token refresh failure with retry")
        void testTokenRefresh_FailureWithRetry() {
            // Arrange
            Long pageId = 1L;
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setRefreshToken("encrypted-refresh-token");

            when(pageRepository.findById(pageId)).thenReturn(Optional.of(testPage));
            when(platformRepository.findById(testPage.getPlatformId())).thenReturn(Optional.of(testPlatform));
            when(refreshTokenRepository.findValidRefreshTokenByPageId(pageId, any(LocalDateTime.class)))
                .thenReturn(Optional.of(refreshToken));
            when(strategyFactory.getStrategy(testPlatform.getLabel())).thenReturn(mockOAuthStrategy);
            when(mockOAuthStrategy.refreshTokens(refreshToken.getRefreshToken()))
                .thenThrow(new RuntimeException("Temporary network error"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                pageManagementService.refreshPageTokens(pageId);
            });

            assertTrue(exception.getMessage().contains("Failed to refresh tokens"));
            assertFalse(testPage.isActive());
        }

        @Test
        @DisplayName("Should handle multiple platform token refresh")
        void testTokenRefresh_MultiplePlatforms() {
            // Arrange
            List<ManagedPage> pages = Arrays.asList(
                createTestPageForPlatform(1L, 100L, "facebook"),
                createTestPageForPlatform(2L, 100L, "instagram"),
                createTestPageForPlatform(3L, 100L, "x")
            );

            for (ManagedPage page : pages) {
                RefreshToken refreshToken = new RefreshToken();
                refreshToken.setRefreshToken("encrypted-refresh-token-" + page.getId());

                when(pageRepository.findById(page.getId())).thenReturn(Optional.of(page));
                when(platformRepository.findById(page.getPlatformId())).thenReturn(Optional.of(testPlatform));
                when(refreshTokenRepository.findValidRefreshTokenByPageId(page.getId(), any(LocalDateTime.class)))
                    .thenReturn(Optional.of(refreshToken));
                when(strategyFactory.getStrategy(any())).thenReturn(mockOAuthStrategy);
                when(mockOAuthStrategy.refreshTokens(any())).thenReturn(testTokens);
            }

            // Act
            for (ManagedPage page : pages) {
                pageManagementService.refreshPageTokens(page.getId());
            }

            // Assert
            verify(mockOAuthStrategy, times(3)).refreshTokens(any());
            verify(tokenManagementService, times(3)).updateTokens(any(), any());
        }

        private ManagedPage createTestPageForPlatform(Long id, Long sellerId, String platform) {
            ManagedPage page = new ManagedPage();
            page.setId(id);
            page.setSellerId(sellerId);
            page.setPlatformId(1L);
            page.setPlatformIdentifier("test-page-" + platform + "-" + id);
            page.setPageTitle("Test " + platform + " Page");
            page.setActive();
            return page;
        }
    }

    @Nested
    @DisplayName("Error Handling and Recovery Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle database connection failure gracefully")
        void testDatabaseConnectionFailure() {
            // Arrange
            Long sellerId = 100L;

            when(pageRepository.findBySellerIdCustom(sellerId))
                .thenThrow(new RuntimeException("Database connection failed"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                pageManagementService.getConnectedPages(sellerId);
            });
        }

        @Test
        @DisplayName("Should handle platform API rate limiting")
        void testPlatformApiRateLimiting() {
            // Arrange
            Long sellerId = 100L;
            String platform = "facebook";
            String authCode = "test-auth-code";
            String state = "test-state";

            when(platformRepository.findByLabelIgnoreCase(platform)).thenReturn(Optional.of(testPlatform));
            when(strategyFactory.getStrategy(platform)).thenReturn(mockOAuthStrategy);
            when(mockOAuthStrategy.exchangeCodeForTokens(authCode, state))
                .thenThrow(new OAuthException(platform, "RATE_LIMITED", "Rate limit exceeded"));

            // Act & Assert
            OAuthException exception = assertThrows(OAuthException.class, () -> {
                pageManagementService.connectPage(sellerId, platform, authCode, state);
            });

            assertEquals("CONNECTION_FAILED", exception.getErrorCode());
        }

        @Test
        @DisplayName("Should handle partial failure in batch operations")
        void testPartialFailureInBatchOperations() {
            // Arrange
            Long sellerId = 100L;
            List<ManagedPage> pages = Arrays.asList(
                createTestPage(1L, sellerId),
                createTestPage(2L, sellerId),
                createTestPage(3L, sellerId)
            );

            when(pageRepository.findBySellerIdCustom(sellerId)).thenReturn(pages);
            when(platformRepository.findById(any())).thenReturn(Optional.of(testPlatform));
            when(accessTokenRepository.hasValidTokens(1L, any())).thenReturn(true);
            when(accessTokenRepository.hasValidTokens(2L, any())).thenThrow(new RuntimeException("DB error"));
            when(accessTokenRepository.hasValidTokens(3L, any())).thenReturn(false);
            when(refreshTokenRepository.hasValidRefreshTokens(any(), any())).thenReturn(true);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                pageManagementService.getConnectedPages(sellerId);
            });
        }

        @Test
        @DisplayName("Should handle invalid platform configuration")
        void testInvalidPlatformConfiguration() {
            // Arrange
            Long sellerId = 100L;
            String platform = "facebook";
            String authCode = "test-auth-code";
            String state = "test-state";

            SupportedPlatform invalidPlatform = new SupportedPlatform();
            invalidPlatform.setId(1L);
            invalidPlatform.setLabel("invalid-platform");

            when(platformRepository.findByLabelIgnoreCase(platform)).thenReturn(Optional.of(invalidPlatform));
            when(strategyFactory.getStrategy(platform))
                .thenThrow(new UnsupportedPlatformException("Platform strategy not found"));

            // Act & Assert
            assertThrows(UnsupportedPlatformException.class, () -> {
                pageManagementService.connectPage(sellerId, platform, authCode, state);
            });
        }

        @Test
        @DisplayName("Should handle token storage failure")
        void testTokenStorageFailure() {
            // Arrange
            Long sellerId = 100L;
            String platform = "facebook";
            String pageId = "test-page-id";
            String accessToken = "test-access-token";

            when(platformRepository.findByLabelIgnoreCase(platform)).thenReturn(Optional.of(testPlatform));
            when(pageRepository.existsBySellerAndPlatformIdentifier(sellerId, pageId, testPlatform.getId()))
                .thenReturn(false);
            when(strategyFactory.getStrategy(platform)).thenReturn(mockOAuthStrategy);
            when(mockOAuthStrategy.getUserPages(accessToken)).thenReturn(Arrays.asList(testSocialMediaPage));
            when(pageRepository.save(any(ManagedPage.class))).thenReturn(testPage);
            doThrow(new RuntimeException("Token storage failed"))
                .when(tokenManagementService).storeTokens(any(), any(), any(), any());

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                pageManagementService.connectSpecificPage(sellerId, platform, pageId, accessToken);
            });
        }

        private ManagedPage createTestPage(Long id, Long sellerId) {
            ManagedPage page = new ManagedPage();
            page.setId(id);
            page.setSellerId(sellerId);
            page.setPlatformId(1L);
            page.setPlatformIdentifier("test-page-" + id);
            page.setPageTitle("Test Page " + id);
            page.setActive();
            return page;
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty page list from platform")
        void testEmptyPageListFromPlatform() {
            // Arrange
            Long sellerId = 100L;
            String platform = "facebook";
            String accessToken = "test-access-token";

            when(strategyFactory.getStrategy(platform)).thenReturn(mockOAuthStrategy);
            when(mockOAuthStrategy.getUserPages(accessToken)).thenReturn(Collections.emptyList());

            // Act
            List<SocialMediaPage> result = pageManagementService.getAvailablePages(sellerId, platform, accessToken);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle null token response")
        void testNullTokenResponse() {
            // Arrange
            Long sellerId = 100L;
            String platform = "facebook";
            String authCode = "test-auth-code";
            String state = "test-state";

            when(platformRepository.findByLabelIgnoreCase(platform)).thenReturn(Optional.of(testPlatform));
            when(strategyFactory.getStrategy(platform)).thenReturn(mockOAuthStrategy);
            when(mockOAuthStrategy.exchangeCodeForTokens(authCode, state)).thenReturn(null);

            // Act & Assert
            assertThrows(OAuthException.class, () -> {
                pageManagementService.connectPage(sellerId, platform, authCode, state);
            });
        }

        @Test
        @DisplayName("Should handle very long platform identifiers")
        void testVeryLongPlatformIdentifiers() {
            // Arrange
            Long sellerId = 100L;
            String platform = "facebook";
            String veryLongPageId = "a".repeat(1000); // Very long page ID
            String accessToken = "test-access-token";

            SocialMediaPage longIdPage = new SocialMediaPage();
            longIdPage.setId(veryLongPageId);
            longIdPage.setName("Test Page");
            longIdPage.setPlatform("facebook");

            when(platformRepository.findByLabelIgnoreCase(platform)).thenReturn(Optional.of(testPlatform));
            when(pageRepository.existsBySellerAndPlatformIdentifier(sellerId, veryLongPageId, testPlatform.getId()))
                .thenReturn(false);
            when(strategyFactory.getStrategy(platform)).thenReturn(mockOAuthStrategy);
            when(mockOAuthStrategy.getUserPages(accessToken)).thenReturn(Arrays.asList(longIdPage));
            when(pageRepository.save(any(ManagedPage.class))).thenReturn(testPage);

            // Act
            ManagedPage result = pageManagementService.connectSpecificPage(sellerId, platform, veryLongPageId, accessToken);

            // Assert
            assertNotNull(result);
            verify(pageRepository).save(any(ManagedPage.class));
        }

        @Test
        @DisplayName("Should handle concurrent page connections")
        void testConcurrentPageConnections() {
            // Arrange
            Long sellerId = 100L;
            String platform = "facebook";
            String pageId = "test-page-id";
            String accessToken = "test-access-token";

            when(platformRepository.findByLabelIgnoreCase(platform)).thenReturn(Optional.of(testPlatform));
            when(pageRepository.existsBySellerAndPlatformIdentifier(sellerId, pageId, testPlatform.getId()))
                .thenReturn(false)
                .thenReturn(true); // Second call returns true (already connected)
            when(strategyFactory.getStrategy(platform)).thenReturn(mockOAuthStrategy);
            when(mockOAuthStrategy.getUserPages(accessToken)).thenReturn(Arrays.asList(testSocialMediaPage));

            // Act - First connection should succeed
            when(pageRepository.save(any(ManagedPage.class))).thenReturn(testPage);
            ManagedPage result1 = pageManagementService.connectSpecificPage(sellerId, platform, pageId, accessToken);

            // Act - Second connection should fail
            assertThrows(IllegalArgumentException.class, () -> {
                pageManagementService.connectSpecificPage(sellerId, platform, pageId, accessToken);
            });

            // Assert
            assertNotNull(result1);
            verify(pageRepository, times(1)).save(any(ManagedPage.class));
        }
    }
}