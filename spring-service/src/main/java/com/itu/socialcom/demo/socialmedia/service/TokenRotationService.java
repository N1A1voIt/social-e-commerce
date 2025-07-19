package com.itu.socialcom.demo.socialmedia.service;

import com.itu.socialcom.demo.socialmedia.dto.OAuthTokenResponse;
import com.itu.socialcom.demo.socialmedia.entity.AccessToken;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import com.itu.socialcom.demo.socialmedia.entity.RefreshToken;
import com.itu.socialcom.demo.socialmedia.entity.SupportedPlatform;
import com.itu.socialcom.demo.socialmedia.exception.TokenExpiredException;
import com.itu.socialcom.demo.socialmedia.oauth.OAuthStrategy;
import com.itu.socialcom.demo.socialmedia.oauth.OAuthStrategyFactory;
import com.itu.socialcom.demo.socialmedia.repository.AccessTokenRepository;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageRepository;
import com.itu.socialcom.demo.socialmedia.repository.RefreshTokenRepository;
import com.itu.socialcom.demo.socialmedia.repository.SupportedPlatformRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for automatic token rotation and expiration monitoring.
 * Handles proactive token refresh and re-authentication requirements.
 */
@Service
@Transactional
public class TokenRotationService {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenRotationService.class);
    
    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ManagedPageRepository pageRepository;
    private final SupportedPlatformRepository platformRepository;
    private final OAuthStrategyFactory strategyFactory;
    private final TokenManagementService tokenManagementService;
    
    public TokenRotationService(AccessTokenRepository accessTokenRepository,
                              RefreshTokenRepository refreshTokenRepository,
                              ManagedPageRepository pageRepository,
                              SupportedPlatformRepository platformRepository,
                              OAuthStrategyFactory strategyFactory,
                              TokenManagementService tokenManagementService) {
        this.accessTokenRepository = accessTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.pageRepository = pageRepository;
        this.platformRepository = platformRepository;
        this.strategyFactory = strategyFactory;
        this.tokenManagementService = tokenManagementService;
    }
    
    /**
     * Scheduled method to rotate expiring tokens
     * Runs every hour to check for tokens that need rotation
     */
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void rotateExpiringTokens() {
        logger.info("Starting scheduled token rotation check");
        
        LocalDateTime rotationThreshold = LocalDateTime.now().plusHours(24);
        
        // Find tokens expiring within 24 hours
        List<AccessToken> expiringTokens = accessTokenRepository.findTokensExpiringBefore(rotationThreshold, LocalDateTime.now());
        
        logger.info("Found {} tokens expiring within 24 hours", expiringTokens.size());
        
        int successfulRotations = 0;
        int failedRotations = 0;
        
        for (AccessToken token : expiringTokens) {
            try {
                boolean rotated = rotateTokenForPage(token.getManagedPageId());
                if (rotated) {
                    successfulRotations++;
                } else {
                    failedRotations++;
                }
            } catch (Exception e) {
                logger.error("Failed to rotate token for page {}: {}", token.getManagedPageId(), e.getMessage());
                failedRotations++;
            }
        }
        
        logger.info("Token rotation completed. Successful: {}, Failed: {}", successfulRotations, failedRotations);
    }
    
    /**
     * Scheduled method to monitor refresh token expiration
     * Runs daily to check for refresh tokens that will expire soon
     */
    @Scheduled(fixedRate = 86400000) // Run every 24 hours
    public void monitorRefreshTokenExpiration() {
        logger.info("Starting refresh token expiration monitoring");
        
        LocalDateTime warningThreshold = LocalDateTime.now().plusDays(7);
        
        // Find refresh tokens expiring within 7 days
        List<RefreshToken> expiringRefreshTokens = refreshTokenRepository.findTokensExpiringBefore(warningThreshold, LocalDateTime.now());
        
        logger.info("Found {} refresh tokens expiring within 7 days", expiringRefreshTokens.size());
        
        for (RefreshToken token : expiringRefreshTokens) {
            try {
                handleExpiringRefreshToken(token);
            } catch (Exception e) {
                logger.error("Failed to handle expiring refresh token for page {}: {}", token.getManagedPageId(), e.getMessage());
            }
        }
        
        logger.info("Refresh token expiration monitoring completed");
    }
    
    /**
     * Rotate tokens for a specific page
     * @param pageId The managed page ID
     * @return true if rotation was successful, false otherwise
     */
    public boolean rotateTokenForPage(Long pageId) {
        logger.debug("Attempting to rotate tokens for page: {}", pageId);
        
        try {
            // Check if page exists and is active
            Optional<ManagedPage> pageOpt = pageRepository.findById(pageId);
            if (pageOpt.isEmpty()) {
                logger.warn("Page not found: {}", pageId);
                return false;
            }
            
            ManagedPage page = pageOpt.get();
            if (!page.isActive()) {
                logger.debug("Page is not active, skipping rotation: {}", pageId);
                return false;
            }
            
            // Get valid refresh token
            Optional<RefreshToken> refreshToken = refreshTokenRepository.findValidRefreshTokenByPageId(pageId, LocalDateTime.now());
            
            if (refreshToken.isEmpty()) {
                logger.warn("No valid refresh token found for page: {}", pageId);
                markPageAsRequiringReauth(pageId);
                return false;
            }
            
            // Get platform information
            SupportedPlatform platform = platformRepository.findById(page.getPlatformId()).orElse(null);
            if (platform == null) {
                logger.error("Platform not found for page: {}", pageId);
                return false;
            }
            
            // Perform token rotation
            OAuthStrategy strategy = strategyFactory.getStrategy(platform.getLabel());
            String decryptedRefreshToken = tokenManagementService.decryptToken(refreshToken.get().getRefreshToken());
            
            OAuthTokenResponse newTokens = strategy.refreshTokens(decryptedRefreshToken);
            
            // Update tokens using TokenManagementService
            tokenManagementService.updateTokens(pageId, newTokens);
            
            logger.info("Successfully rotated tokens for page: {}", pageId);
            return true;
            
        } catch (TokenExpiredException e) {
            logger.warn("Refresh token expired for page: {}", pageId);
            markPageAsRequiringReauth(pageId);
            return false;
        } catch (Exception e) {
            logger.error("Failed to rotate tokens for page: {}", pageId, e);
            handleRotationFailure(pageId, e);
            return false;
        }
    }
    
    /**
     * Force rotate tokens for a page (manual trigger)
     * @param pageId The managed page ID
     * @return true if rotation was successful, false otherwise
     */
    public boolean forceRotateTokenForPage(Long pageId) {
        logger.info("Force rotating tokens for page: {}", pageId);
        return rotateTokenForPage(pageId);
    }
    
    /**
     * Check if a page needs token rotation
     * @param pageId The managed page ID
     * @param hoursThreshold Hours before expiration to consider rotation needed
     * @return true if rotation is needed, false otherwise
     */
    public boolean needsTokenRotation(Long pageId, int hoursThreshold) {
        return tokenManagementService.areAccessTokensNearExpiration(pageId, hoursThreshold * 60);
    }
    
    /**
     * Check if a page needs re-authentication
     * @param pageId The managed page ID
     * @param daysThreshold Days before refresh token expiration to consider re-auth needed
     * @return true if re-authentication is needed, false otherwise
     */
    public boolean needsReAuthentication(Long pageId, int daysThreshold) {
        return tokenManagementService.areRefreshTokensNearExpiration(pageId, daysThreshold);
    }
    
    /**
     * Get pages that need token rotation
     * @param hoursThreshold Hours before expiration to consider rotation needed
     * @return List of page IDs that need rotation
     */
    public List<Long> getPagesNeedingRotation(int hoursThreshold) {
        LocalDateTime threshold = LocalDateTime.now().plusHours(hoursThreshold);
        
        return accessTokenRepository.findTokensExpiringBefore(threshold, LocalDateTime.now())
            .stream()
            .map(AccessToken::getManagedPageId)
            .distinct()
            .toList();
    }
    
    /**
     * Get pages that need re-authentication
     * @param daysThreshold Days before refresh token expiration to consider re-auth needed
     * @return List of page IDs that need re-authentication
     */
    public List<Long> getPagesNeedingReAuthentication(int daysThreshold) {
        LocalDateTime threshold = LocalDateTime.now().plusDays(daysThreshold);
        
        return refreshTokenRepository.findPageIdsWithExpiringRefreshTokens(LocalDateTime.now(), threshold);
    }
    
    /**
     * Handle expiring refresh token
     * @param refreshToken The expiring refresh token
     */
    private void handleExpiringRefreshToken(RefreshToken refreshToken) {
        logger.info("Handling expiring refresh token for page: {}", refreshToken.getManagedPageId());
        
        // Mark page as requiring re-authentication
        markPageAsRequiringReauth(refreshToken.getManagedPageId());
        
        // TODO: Send notification to user about required re-authentication
        // This could be implemented as an event or notification service call
        logger.info("Page {} requires re-authentication due to expiring refresh token", refreshToken.getManagedPageId());
    }
    
    /**
     * Mark a page as requiring re-authentication
     * @param pageId The managed page ID
     */
    private void markPageAsRequiringReauth(Long pageId) {
        logger.info("Marking page as requiring re-authentication: {}", pageId);
        
        try {
            Optional<ManagedPage> pageOpt = pageRepository.findById(pageId);
            if (pageOpt.isPresent()) {
                ManagedPage page = pageOpt.get();
                page.setInactive(); // Mark as inactive to indicate re-auth needed
                pageRepository.save(page);
                
                logger.info("Page {} marked as requiring re-authentication", pageId);
            }
        } catch (Exception e) {
            logger.error("Failed to mark page as requiring re-auth: {}", pageId, e);
        }
    }
    
    /**
     * Handle token rotation failure
     * @param pageId The managed page ID
     * @param exception The exception that caused the failure
     */
    private void handleRotationFailure(Long pageId, Exception exception) {
        logger.error("Handling rotation failure for page: {}", pageId, exception);
        
        // Determine if this is a recoverable error
        if (exception instanceof TokenExpiredException) {
            markPageAsRequiringReauth(pageId);
        } else {
            // For other errors, we might want to retry later
            logger.warn("Token rotation failed for page {}, will retry in next cycle", pageId);
        }
        
        // TODO: Implement notification or alerting for rotation failures
        // This could include metrics, alerts, or user notifications
    }
    
    /**
     * Cleanup old expired tokens (maintenance task)
     * Runs weekly to clean up old expired tokens
     */
    @Scheduled(fixedRate = 604800000) // Run every 7 days
    public void cleanupExpiredTokens() {
        logger.info("Starting expired token cleanup");
        
        try {
            int cleanedUp = tokenManagementService.cleanupExpiredTokens();
            logger.info("Cleaned up {} expired tokens", cleanedUp);
        } catch (Exception e) {
            logger.error("Failed to cleanup expired tokens", e);
        }
    }
    
    /**
     * Get token rotation statistics
     * @return Statistics about token rotation health
     */
    public TokenRotationStatistics getRotationStatistics() {
        logger.debug("Generating token rotation statistics");
        
        LocalDateTime now = LocalDateTime.now();
        
        // Count pages needing rotation (within 24 hours)
        List<Long> pagesNeedingRotation = getPagesNeedingRotation(24);
        
        // Count pages needing re-authentication (within 7 days)
        List<Long> pagesNeedingReauth = getPagesNeedingReAuthentication(7);
        
        // Count active pages
        long activePagesCount = pageRepository.findAll().stream()
            .mapToLong(page -> page.isActive() ? 1 : 0)
            .sum();
        
        // Count pages with healthy tokens
        long pagesWithHealthyTokens = pageRepository.findAll().stream()
            .filter(ManagedPage::isActive)
            .mapToLong(page -> {
                boolean hasValidAccess = tokenManagementService.hasValidAccessTokens(page.getId());
                boolean hasValidRefresh = tokenManagementService.hasValidRefreshTokens(page.getId());
                return (hasValidAccess && hasValidRefresh) ? 1 : 0;
            })
            .sum();
        
        return new TokenRotationStatistics(
            activePagesCount,
            pagesWithHealthyTokens,
            pagesNeedingRotation.size(),
            pagesNeedingReauth.size(),
            now
        );
    }
    
    /**
     * Statistics class for token rotation monitoring
     */
    public static class TokenRotationStatistics {
        private final long totalActivePages;
        private final long pagesWithHealthyTokens;
        private final long pagesNeedingRotation;
        private final long pagesNeedingReAuthentication;
        private final LocalDateTime generatedAt;
        
        public TokenRotationStatistics(long totalActivePages, long pagesWithHealthyTokens,
                                     long pagesNeedingRotation, long pagesNeedingReAuthentication,
                                     LocalDateTime generatedAt) {
            this.totalActivePages = totalActivePages;
            this.pagesWithHealthyTokens = pagesWithHealthyTokens;
            this.pagesNeedingRotation = pagesNeedingRotation;
            this.pagesNeedingReAuthentication = pagesNeedingReAuthentication;
            this.generatedAt = generatedAt;
        }
        
        public long getTotalActivePages() { return totalActivePages; }
        public long getPagesWithHealthyTokens() { return pagesWithHealthyTokens; }
        public long getPagesNeedingRotation() { return pagesNeedingRotation; }
        public long getPagesNeedingReAuthentication() { return pagesNeedingReAuthentication; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        
        public double getHealthyTokenPercentage() {
            return totalActivePages > 0 ? (double) pagesWithHealthyTokens / totalActivePages * 100 : 0;
        }
        
        @Override
        public String toString() {
            return "TokenRotationStatistics{" +
                    "totalActivePages=" + totalActivePages +
                    ", pagesWithHealthyTokens=" + pagesWithHealthyTokens +
                    ", pagesNeedingRotation=" + pagesNeedingRotation +
                    ", pagesNeedingReAuthentication=" + pagesNeedingReAuthentication +
                    ", healthyTokenPercentage=" + String.format("%.2f", getHealthyTokenPercentage()) + "%" +
                    ", generatedAt=" + generatedAt +
                    '}';
        }
    }
}