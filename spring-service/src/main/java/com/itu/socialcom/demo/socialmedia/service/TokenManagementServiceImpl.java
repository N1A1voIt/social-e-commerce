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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of TokenManagementService for secure token storage and management.
 * Handles token encryption, validation, refresh, and cleanup operations.
 */
@Service
@Transactional
public class TokenManagementServiceImpl implements TokenManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenManagementServiceImpl.class);
    
    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ManagedPageRepository pageRepository;
    private final SupportedPlatformRepository platformRepository;
    private final OAuthStrategyFactory strategyFactory;
    
    @Value("${token.encryption.key}")
    private String encryptionKey;
    
    @Value("${token.encryption.algorithm:AES}")
    private String encryptionAlgorithm;
    
    @Value("${token.encryption.transformation:AES/ECB/PKCS5Padding}")
    private String encryptionTransformation;
    
    public TokenManagementServiceImpl(AccessTokenRepository accessTokenRepository,
                                    RefreshTokenRepository refreshTokenRepository,
                                    ManagedPageRepository pageRepository,
                                    SupportedPlatformRepository platformRepository,
                                    OAuthStrategyFactory strategyFactory) {
        this.accessTokenRepository = accessTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.pageRepository = pageRepository;
        this.platformRepository = platformRepository;
        this.strategyFactory = strategyFactory;
    }
    
    @Override
    public void storeTokens(Long pageId, OAuthTokenResponse tokens) {
        logger.debug("Storing tokens for page: {}", pageId);
        
        // Get page information to determine platform
        ManagedPage page = pageRepository.findById(pageId)
            .orElseThrow(() -> new IllegalArgumentException("Page not found: " + pageId));
        
        SupportedPlatform platform = platformRepository.findById(page.getPlatformId())
            .orElseThrow(() -> new IllegalArgumentException("Platform not found for page: " + pageId));
        
        storeTokens(pageId, tokens, platform.getId(), platform.getLabel());
    }
    
    @Override
    public void storeTokens(Long pageId, OAuthTokenResponse tokens, Long platformId, String platform) {
        logger.debug("Storing tokens for page: {}, platform: {}", pageId, platform);
        
        try {
            // Store access token
            if (tokens.getAccessToken() != null) {
                AccessToken accessToken = new AccessToken();
                accessToken.setManagedPageId(pageId);
                accessToken.setPlatformId(platformId);
                accessToken.setPlatform(platform);
                accessToken.setAccessToken(encryptToken(tokens.getAccessToken()));
                accessToken.setExpirationDate(calculateExpirationDate(tokens.getExpiresIn()));
                accessTokenRepository.save(accessToken);
                
                logger.debug("Stored access token for page: {}", pageId);
            }
            
            // Store refresh token if available
            if (tokens.getRefreshToken() != null) {
                RefreshToken refreshToken = new RefreshToken();
                refreshToken.setManagedPageId(pageId);
                refreshToken.setPlatformId(platformId);
                refreshToken.setPlatform(platform);
                refreshToken.setRefreshToken(encryptToken(tokens.getRefreshToken()));
                refreshToken.setExpirationDate(calculateRefreshTokenExpirationDate(platform));
                refreshTokenRepository.save(refreshToken);
                
                logger.debug("Stored refresh token for page: {}", pageId);
            }
            
        } catch (Exception e) {
            logger.error("Failed to store tokens for page: {}", pageId, e);
            throw new RuntimeException("Failed to store tokens: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<OAuthTokenResponse> getValidTokens(Long pageId) {
        logger.debug("Retrieving valid tokens for page: {}", pageId);
        
        LocalDateTime now = LocalDateTime.now();
        
        Optional<AccessToken> accessToken = accessTokenRepository.findValidTokenByPageId(pageId, now);
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findValidRefreshTokenByPageId(pageId, now);
        
        if (accessToken.isEmpty()) {
            logger.debug("No valid access token found for page: {}", pageId);
            return Optional.empty();
        }
        
        try {
            String decryptedAccessToken = decryptToken(accessToken.get().getAccessToken());
            String decryptedRefreshToken = refreshToken.map(rt -> decryptToken(rt.getRefreshToken())).orElse(null);
            
            long expiresIn = java.time.Duration.between(now, accessToken.get().getExpirationDate()).getSeconds();
            
            OAuthTokenResponse response = new OAuthTokenResponse(
                decryptedAccessToken,
                decryptedRefreshToken,
                Math.max(0, expiresIn),
                "Bearer",
                null
            );
            
            return Optional.of(response);
            
        } catch (Exception e) {
            logger.error("Failed to decrypt tokens for page: {}", pageId, e);
            return Optional.empty();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasValidAccessTokens(Long pageId) {
        return accessTokenRepository.hasValidTokens(pageId, LocalDateTime.now());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasValidRefreshTokens(Long pageId) {
        return refreshTokenRepository.hasValidRefreshTokens(pageId, LocalDateTime.now());
    }
    
    @Override
    public boolean refreshTokensIfNeeded(Long pageId) {
        logger.debug("Checking if tokens need refresh for page: {}", pageId);
        
        // Check if access tokens are near expiration (within 30 minutes)
        if (!areAccessTokensNearExpiration(pageId, 30)) {
            logger.debug("Access tokens are not near expiration for page: {}", pageId);
            return true; // Tokens are still valid
        }
        
        return forceRefreshTokens(pageId).isPresent();
    }
    
    @Override
    public Optional<OAuthTokenResponse> forceRefreshTokens(Long pageId) {
        logger.info("Force refreshing tokens for page: {}", pageId);
        
        try {
            // Get page and platform information
            ManagedPage page = pageRepository.findById(pageId)
                .orElseThrow(() -> new IllegalArgumentException("Page not found: " + pageId));
            
            SupportedPlatform platform = platformRepository.findById(page.getPlatformId())
                .orElseThrow(() -> new IllegalArgumentException("Platform not found for page: " + pageId));
            
            // Get valid refresh token
            Optional<RefreshToken> refreshToken = refreshTokenRepository.findValidRefreshTokenByPageId(pageId, LocalDateTime.now());
            
            if (refreshToken.isEmpty()) {
                logger.warn("No valid refresh token found for page: {}", pageId);
                return Optional.empty();
            }
            
            // Get OAuth strategy and refresh tokens
            OAuthStrategy strategy = strategyFactory.getStrategy(platform.getLabel());
            String decryptedRefreshToken = decryptToken(refreshToken.get().getRefreshToken());
            
            OAuthTokenResponse newTokens = strategy.refreshTokens(decryptedRefreshToken);
            
            // Mark old tokens as expired
            markOldTokensAsExpired(pageId);
            
            // Store new tokens
            storeTokens(pageId, newTokens, platform.getId(), platform.getLabel());
            
            logger.info("Successfully refreshed tokens for page: {}", pageId);
            return Optional.of(newTokens);
            
        } catch (TokenExpiredException e) {
            logger.warn("Refresh token expired for page: {}", pageId);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Failed to refresh tokens for page: {}", pageId, e);
            return Optional.empty();
        }
    }
    
    @Override
    public void revokeTokens(Long pageId) {
        logger.info("Revoking tokens for page: {}", pageId);
        
        try {
            // Delete all tokens for the page
            accessTokenRepository.deleteByManagedPageId(pageId);
            refreshTokenRepository.deleteByManagedPageId(pageId);
            
            logger.info("Successfully revoked tokens for page: {}", pageId);
            
        } catch (Exception e) {
            logger.error("Failed to revoke tokens for page: {}", pageId, e);
            throw new RuntimeException("Failed to revoke tokens: " + e.getMessage());
        }
    }
    
    @Override
    public void revokeTokensOnPlatform(Long pageId, String platform) {
        logger.info("Revoking tokens on platform: {} for page: {}", platform, pageId);
        
        try {
            // Get valid access token
            Optional<AccessToken> accessToken = accessTokenRepository.findValidTokenByPageId(pageId, LocalDateTime.now());
            
            if (accessToken.isPresent()) {
                // Revoke on platform
                OAuthStrategy strategy = strategyFactory.getStrategy(platform);
                String decryptedToken = decryptToken(accessToken.get().getAccessToken());
                strategy.revokeAccess(decryptedToken);
            }
            
            // Clean up locally
            revokeTokens(pageId);
            
        } catch (Exception e) {
            logger.error("Failed to revoke tokens on platform: {} for page: {}", platform, pageId, e);
            // Still clean up locally even if platform revocation fails
            revokeTokens(pageId);
        }
    }
    
    @Override
    public int cleanupExpiredTokens() {
        logger.info("Cleaning up expired tokens");
        
        LocalDateTime now = LocalDateTime.now();
        int cleanedUp = 0;
        
        try {
            // Clean up expired access tokens
            List<AccessToken> expiredAccessTokens = accessTokenRepository.findExpiredTokens(now);
            accessTokenRepository.deleteExpiredTokens(now);
            cleanedUp += expiredAccessTokens.size();
            
            // Clean up expired refresh tokens
            List<RefreshToken> expiredRefreshTokens = refreshTokenRepository.findExpiredTokens(now);
            refreshTokenRepository.deleteExpiredTokens(now);
            cleanedUp += expiredRefreshTokens.size();
            
            logger.info("Cleaned up {} expired tokens", cleanedUp);
            
        } catch (Exception e) {
            logger.error("Failed to cleanup expired tokens", e);
        }
        
        return cleanedUp;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean validateTokenIntegrity(Long pageId) {
        logger.debug("Validating token integrity for page: {}", pageId);
        
        try {
            Optional<AccessToken> accessToken = accessTokenRepository.findLatestTokenByPageId(pageId);
            Optional<RefreshToken> refreshToken = refreshTokenRepository.findLatestRefreshTokenByPageId(pageId);
            
            // Try to decrypt tokens to validate integrity
            if (accessToken.isPresent()) {
                decryptToken(accessToken.get().getAccessToken());
            }
            
            if (refreshToken.isPresent()) {
                decryptToken(refreshToken.get().getRefreshToken());
            }
            
            return true;
            
        } catch (Exception e) {
            logger.warn("Token integrity validation failed for page: {}", pageId, e);
            return false;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<LocalDateTime> getAccessTokenExpiration(Long pageId) {
        return accessTokenRepository.findValidTokenByPageId(pageId, LocalDateTime.now())
            .map(AccessToken::getExpirationDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<LocalDateTime> getRefreshTokenExpiration(Long pageId) {
        return refreshTokenRepository.findValidRefreshTokenByPageId(pageId, LocalDateTime.now())
            .map(RefreshToken::getExpirationDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean areAccessTokensNearExpiration(Long pageId, int minutesThreshold) {
        LocalDateTime threshold = LocalDateTime.now().plusMinutes(minutesThreshold);
        
        return accessTokenRepository.findValidTokenByPageId(pageId, LocalDateTime.now())
            .map(token -> token.getExpirationDate().isBefore(threshold))
            .orElse(false);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean areRefreshTokensNearExpiration(Long pageId, int daysThreshold) {
        LocalDateTime threshold = LocalDateTime.now().plusDays(daysThreshold);
        
        return refreshTokenRepository.findValidRefreshTokenByPageId(pageId, LocalDateTime.now())
            .map(token -> token.getExpirationDate().isBefore(threshold))
            .orElse(false);
    }
    
    @Override
    public String encryptToken(String token) {
        if (token == null || token.isEmpty()) {
            return token;
        }
        
        try {
            SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), encryptionAlgorithm);
            Cipher cipher = Cipher.getInstance(encryptionTransformation);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
            
        } catch (Exception e) {
            logger.error("Failed to encrypt token", e);
            throw new RuntimeException("Token encryption failed: " + e.getMessage());
        }
    }
    
    @Override
    public String decryptToken(String encryptedToken) {
        if (encryptedToken == null || encryptedToken.isEmpty()) {
            return encryptedToken;
        }
        
        try {
            SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), encryptionAlgorithm);
            Cipher cipher = Cipher.getInstance(encryptionTransformation);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedToken));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            logger.error("Failed to decrypt token", e);
            throw new RuntimeException("Token decryption failed: " + e.getMessage());
        }
    }
    
    @Override
    public void updateTokens(Long pageId, OAuthTokenResponse newTokens) {
        logger.debug("Updating tokens for page: {}", pageId);
        
        // Mark old tokens as expired first
        markOldTokensAsExpired(pageId);
        
        // Store new tokens
        storeTokens(pageId, newTokens);
    }
    
    @Override
    public void markOldTokensAsExpired(Long pageId) {
        logger.debug("Marking old tokens as expired for page: {}", pageId);
        
        LocalDateTime now = LocalDateTime.now();
        
        try {
            accessTokenRepository.markTokensAsExpired(pageId, now, now);
            refreshTokenRepository.markTokensAsExpired(pageId, now, now);
            
        } catch (Exception e) {
            logger.error("Failed to mark old tokens as expired for page: {}", pageId, e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public TokenStatistics getTokenStatistics() {
        logger.debug("Generating token statistics");
        
        LocalDateTime now = LocalDateTime.now();
        
        try {
            // Count total tokens
            long totalAccessTokens = accessTokenRepository.count();
            long totalRefreshTokens = refreshTokenRepository.count();
            
            // Count valid tokens
            long validAccessTokens = accessTokenRepository.findAll().stream()
                .mapToLong(token -> token.getExpirationDate().isAfter(now) ? 1 : 0)
                .sum();
            
            long validRefreshTokens = refreshTokenRepository.findAll().stream()
                .mapToLong(token -> token.getExpirationDate().isAfter(now) ? 1 : 0)
                .sum();
            
            // Count expired tokens
            long expiredAccessTokens = totalAccessTokens - validAccessTokens;
            long expiredRefreshTokens = totalRefreshTokens - validRefreshTokens;
            
            // Count tokens near expiration
            LocalDateTime nearExpirationThreshold = now.plusHours(24);
            long tokensNearExpiration = accessTokenRepository.findTokensExpiringBefore(nearExpirationThreshold, now).size();
            
            LocalDateTime refreshNearExpirationThreshold = now.plusDays(7);
            long refreshTokensNearExpiration = refreshTokenRepository.findTokensExpiringBefore(refreshNearExpirationThreshold, now).size();
            
            // Count tokens by platform
            Map<String, Long> tokensByPlatform = accessTokenRepository.findAll().stream()
                .collect(Collectors.groupingBy(AccessToken::getPlatform, Collectors.counting()));
            
            Map<String, Long> validTokensByPlatform = accessTokenRepository.findAll().stream()
                .filter(token -> token.getExpirationDate().isAfter(now))
                .collect(Collectors.groupingBy(AccessToken::getPlatform, Collectors.counting()));
            
            // Count pages with valid tokens
            Set<Long> pagesWithValidTokens = accessTokenRepository.findAll().stream()
                .filter(token -> token.getExpirationDate().isAfter(now))
                .map(AccessToken::getManagedPageId)
                .collect(Collectors.toSet());
            
            long totalPagesWithValidTokens = pagesWithValidTokens.size();
            
            // Count pages requiring reauth (active pages without valid tokens)
            long totalActivePages = pageRepository.findAll().stream()
                .mapToLong(page -> page.isActive() ? 1 : 0)
                .sum();
            
            long totalPagesRequiringReauth = totalActivePages - totalPagesWithValidTokens;
            
            return new TokenStatistics(
                totalAccessTokens, totalRefreshTokens,
                validAccessTokens, validRefreshTokens,
                expiredAccessTokens, expiredRefreshTokens,
                tokensNearExpiration, refreshTokensNearExpiration,
                tokensByPlatform, validTokensByPlatform,
                now, totalPagesWithValidTokens, totalPagesRequiringReauth
            );
            
        } catch (Exception e) {
            logger.error("Failed to generate token statistics", e);
            return new TokenStatistics();
        }
    }
    
    /**
     * Calculate expiration date for access tokens
     */
    private LocalDateTime calculateExpirationDate(Long expiresIn) {
        if (expiresIn == null || expiresIn <= 0) {
            return LocalDateTime.now().plusHours(1); // Default 1 hour
        }
        return LocalDateTime.now().plusSeconds(expiresIn);
    }
    
    /**
     * Calculate expiration date for refresh tokens based on platform
     */
    private LocalDateTime calculateRefreshTokenExpirationDate(String platform) {
        return switch (platform.toLowerCase()) {
            case "facebook" -> LocalDateTime.now().plusDays(60); // Facebook refresh tokens last 60 days
            case "instagram" -> LocalDateTime.now().plusDays(60); // Instagram refresh tokens last 60 days
            case "x" -> LocalDateTime.now().plusDays(90); // X refresh tokens last 90 days
            default -> LocalDateTime.now().plusDays(60); // Default 60 days
        };
    }
}