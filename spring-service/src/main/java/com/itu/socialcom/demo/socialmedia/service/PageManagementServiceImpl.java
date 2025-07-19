package com.itu.socialcom.demo.socialmedia.service;

import com.itu.socialcom.demo.socialmedia.dto.OAuthTokenResponse;
import com.itu.socialcom.demo.socialmedia.dto.SocialMediaPage;
import com.itu.socialcom.demo.socialmedia.entity.AccessToken;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import com.itu.socialcom.demo.socialmedia.entity.RefreshToken;
import com.itu.socialcom.demo.socialmedia.entity.SupportedPlatform;
import com.itu.socialcom.demo.socialmedia.exception.OAuthException;
import com.itu.socialcom.demo.socialmedia.exception.UnsupportedPlatformException;
import com.itu.socialcom.demo.socialmedia.oauth.OAuthStrategy;
import com.itu.socialcom.demo.socialmedia.oauth.OAuthStrategyFactory;
import com.itu.socialcom.demo.socialmedia.repository.AccessTokenRepository;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageRepository;
import com.itu.socialcom.demo.socialmedia.repository.RefreshTokenRepository;
import com.itu.socialcom.demo.socialmedia.repository.SupportedPlatformRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of PageManagementService for managing social media page connections.
 * Handles connecting, disconnecting, and retrieving page information with manual relationship handling.
 */
@Service
@Transactional
public class PageManagementServiceImpl implements PageManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(PageManagementServiceImpl.class);
    
    private final ManagedPageRepository pageRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SupportedPlatformRepository platformRepository;
    private final OAuthStrategyFactory strategyFactory;
    private final TokenManagementService tokenManagementService;
    
    public PageManagementServiceImpl(ManagedPageRepository pageRepository,
                                   AccessTokenRepository accessTokenRepository,
                                   RefreshTokenRepository refreshTokenRepository,
                                   SupportedPlatformRepository platformRepository,
                                   OAuthStrategyFactory strategyFactory,
                                   TokenManagementService tokenManagementService) {
        this.pageRepository = pageRepository;
        this.accessTokenRepository = accessTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.platformRepository = platformRepository;
        this.strategyFactory = strategyFactory;
        this.tokenManagementService = tokenManagementService;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ManagedPageWithDetails> getConnectedPages(Long sellerId) {
        logger.debug("Retrieving connected pages for seller: {}", sellerId);
        
        List<ManagedPage> pages = pageRepository.findBySellerIdCustom(sellerId);
        
        return pages.stream().map(page -> {
            // Manually fetch platform details
            SupportedPlatform platform = platformRepository.findById(page.getPlatformId()).orElse(null);
            
            // Manually fetch current token status
            LocalDateTime now = LocalDateTime.now();
            boolean hasValidAccessToken = accessTokenRepository.hasValidTokens(page.getId(), now);
            boolean hasValidRefreshToken = refreshTokenRepository.hasValidRefreshTokens(page.getId(), now);
            
            // Determine connection status
            PageConnectionStatus status = PageConnectionStatus.determineStatus(
                page.isActive(), hasValidAccessToken, hasValidRefreshToken);
            
            return ManagedPageWithDetails.builder()
                .page(page)
                .platform(platform)
                .hasValidAccessToken(hasValidAccessToken)
                .hasValidRefreshToken(hasValidRefreshToken)
                .connectionStatus(status)
                .build();
        }).collect(Collectors.toList());
    }
    
    @Override
    public ManagedPage connectPage(Long sellerId, String platform, String authCode, String state) {
        logger.info("Connecting page for seller: {}, platform: {}", sellerId, platform);
        
        try {
            // Get platform information
            SupportedPlatform supportedPlatform = platformRepository.findByLabelIgnoreCase(platform)
                .orElseThrow(() -> new UnsupportedPlatformException("Platform not supported: " + platform));
            
            // Get OAuth strategy
            OAuthStrategy strategy = strategyFactory.getStrategy(platform);
            
            // Exchange code for tokens
            OAuthTokenResponse tokenResponse = strategy.exchangeCodeForTokens(authCode, state);
            
            // Get available pages from the platform
            List<SocialMediaPage> availablePages = strategy.getUserPages(tokenResponse.getAccessToken());
            
            if (availablePages.isEmpty()) {
                throw new OAuthException(platform, "NO_PAGES", "No pages available for connection");
            }
            
            // For now, connect the first available page (in a real implementation, user would choose)
            SocialMediaPage selectedPage = availablePages.get(0);
            
            return connectSpecificPage(sellerId, platform, selectedPage.getId(), 
                selectedPage.getAccessToken() != null ? selectedPage.getAccessToken() : tokenResponse.getAccessToken());
            
        } catch (Exception e) {
            logger.error("Failed to connect page for seller: {}, platform: {}", sellerId, platform, e);
            throw new OAuthException(platform, "CONNECTION_FAILED", "Failed to connect page: " + e.getMessage());
        }
    }
    
    @Override
    public void disconnectPage(Long sellerId, Long pageId) {
        logger.info("Disconnecting page: {} for seller: {}", pageId, sellerId);
        
        // Verify page belongs to seller
        ManagedPage page = getManagedPage(sellerId, pageId)
            .orElseThrow(() -> new IllegalArgumentException("Page not found or not owned by seller"));
        
        try {
            // Get platform information
            SupportedPlatform platform = platformRepository.findById(page.getPlatformId()).orElse(null);
            
            if (platform != null) {
                // Try to revoke tokens on the platform
                try {
                    OAuthStrategy strategy = strategyFactory.getStrategy(platform.getLabel());
                    Optional<AccessToken> accessToken = accessTokenRepository.findValidTokenByPageId(pageId, LocalDateTime.now());
                    
                    if (accessToken.isPresent()) {
                        strategy.revokeAccess(accessToken.get().getAccessToken());
                    }
                } catch (Exception e) {
                    logger.warn("Failed to revoke tokens on platform for page: {}", pageId, e);
                    // Continue with local cleanup even if platform revocation fails
                }
            }
            
            // Mark page as inactive
            page.setInactive();
            pageRepository.save(page);
            
            // Clean up tokens
            accessTokenRepository.deleteByManagedPageId(pageId);
            refreshTokenRepository.deleteByManagedPageId(pageId);
            
            logger.info("Successfully disconnected page: {}", pageId);
            
        } catch (Exception e) {
            logger.error("Failed to disconnect page: {}", pageId, e);
            throw new RuntimeException("Failed to disconnect page: " + e.getMessage());
        }
    }
    
    @Override
    public void refreshPageTokens(Long pageId) {
        logger.info("Refreshing tokens for page: {}", pageId);
        
        ManagedPage page = pageRepository.findById(pageId)
            .orElseThrow(() -> new IllegalArgumentException("Page not found: " + pageId));
        
        // Get platform information
        SupportedPlatform platform = platformRepository.findById(page.getPlatformId())
            .orElseThrow(() -> new IllegalArgumentException("Platform not found for page: " + pageId));
        
        try {
            // Get valid refresh token
            Optional<RefreshToken> refreshToken = refreshTokenRepository.findValidRefreshTokenByPageId(pageId, LocalDateTime.now());
            
            if (refreshToken.isEmpty()) {
                logger.warn("No valid refresh token found for page: {}", pageId);
                page.setInactive();
                pageRepository.save(page);
                return;
            }
            
            // Get OAuth strategy and refresh tokens
            OAuthStrategy strategy = strategyFactory.getStrategy(platform.getLabel());
            OAuthTokenResponse newTokens = strategy.refreshTokens(refreshToken.get().getRefreshToken());
            
            // Store new tokens using TokenManagementService
            tokenManagementService.updateTokens(pageId, newTokens);
            
            // Ensure page is active
            if (!page.isActive()) {
                page.setActive();
                pageRepository.save(page);
            }
            
            logger.info("Successfully refreshed tokens for page: {}", pageId);
            
        } catch (Exception e) {
            logger.error("Failed to refresh tokens for page: {}", pageId, e);
            // Mark page as inactive if refresh fails
            page.setInactive();
            pageRepository.save(page);
            throw new RuntimeException("Failed to refresh tokens: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageConnectionStatus getPageStatus(Long pageId) {
        logger.debug("Getting status for page: {}", pageId);
        
        ManagedPage page = pageRepository.findById(pageId).orElse(null);
        if (page == null) {
            return PageConnectionStatus.UNKNOWN;
        }
        
        LocalDateTime now = LocalDateTime.now();
        boolean hasValidAccessToken = accessTokenRepository.hasValidTokens(pageId, now);
        boolean hasValidRefreshToken = refreshTokenRepository.hasValidRefreshTokens(pageId, now);
        
        return PageConnectionStatus.determineStatus(page.isActive(), hasValidAccessToken, hasValidRefreshToken);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SocialMediaPage> getAvailablePages(Long sellerId, String platform, String accessToken) {
        logger.debug("Getting available pages for seller: {}, platform: {}", sellerId, platform);
        
        try {
            OAuthStrategy strategy = strategyFactory.getStrategy(platform);
            return strategy.getUserPages(accessToken);
        } catch (Exception e) {
            logger.error("Failed to get available pages for seller: {}, platform: {}", sellerId, platform, e);
            throw new OAuthException(platform, "PAGES_FETCH_FAILED", "Failed to fetch available pages: " + e.getMessage());
        }
    }
    
    @Override
    public ManagedPage connectSpecificPage(Long sellerId, String platform, String pageId, String accessToken) {
        logger.info("Connecting specific page: {} for seller: {}, platform: {}", pageId, sellerId, platform);
        
        // Get platform information
        SupportedPlatform supportedPlatform = platformRepository.findByLabelIgnoreCase(platform)
            .orElseThrow(() -> new UnsupportedPlatformException("Platform not supported: " + platform));
        
        // Check if page is already connected
        if (isPageAlreadyConnected(sellerId, platform, pageId)) {
            throw new IllegalArgumentException("Page is already connected");
        }
        
        try {
            // Get page details from platform
            OAuthStrategy strategy = strategyFactory.getStrategy(platform);
            List<SocialMediaPage> availablePages = strategy.getUserPages(accessToken);
            
            SocialMediaPage selectedPage = availablePages.stream()
                .filter(page -> page.getId().equals(pageId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Page not found or not accessible"));
            
            // Create managed page
            ManagedPage managedPage = new ManagedPage();
            managedPage.setSellerId(sellerId);
            managedPage.setPlatformId(supportedPlatform.getId());
            managedPage.setPlatformIdentifier(selectedPage.getId());
            managedPage.setPageTitle(selectedPage.getName());
            managedPage.setAssociatedMedia(selectedPage.getProfilePictureUrl());
            managedPage.setLinkToPlatform(generatePlatformLink(platform, selectedPage.getId()));
            managedPage.setActive();
            
            managedPage = pageRepository.save(managedPage);
            
            // Store tokens using TokenManagementService
            tokenManagementService.storeTokens(managedPage.getId(), 
                new OAuthTokenResponse(accessToken, null, 3600L, "Bearer", null),
                supportedPlatform.getId(), platform);
            
            logger.info("Successfully connected page: {} with ID: {}", selectedPage.getName(), managedPage.getId());
            return managedPage;
            
        } catch (Exception e) {
            logger.error("Failed to connect specific page: {} for seller: {}", pageId, sellerId, e);
            throw new OAuthException(platform, "CONNECTION_FAILED", "Failed to connect page: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ManagedPage> getManagedPage(Long sellerId, Long pageId) {
        return pageRepository.findById(pageId)
            .filter(page -> page.getSellerId().equals(sellerId));
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isPageAlreadyConnected(Long sellerId, String platform, String platformPageId) {
        SupportedPlatform supportedPlatform = platformRepository.findByLabelIgnoreCase(platform).orElse(null);
        if (supportedPlatform == null) {
            return false;
        }
        
        return pageRepository.existsBySellerAndPlatformIdentifier(sellerId, platformPageId, supportedPlatform.getId());
    }
    

    
    /**
     * Generate platform-specific link to the page
     */
    private String generatePlatformLink(String platform, String pageId) {
        return switch (platform.toLowerCase()) {
            case "facebook" -> "https://facebook.com/" + pageId;
            case "instagram" -> "https://instagram.com/" + pageId;
            case "x" -> "https://x.com/" + pageId;
            default -> "https://" + platform + ".com/" + pageId;
        };
    }
}