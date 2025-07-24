package com.itu.socialcom.demo.socialmedia.service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Statistics class for token management monitoring and reporting.
 * Provides insights into token health, expiration patterns, and system performance.
 */
public class TokenStatistics {
    
    private long totalAccessTokens;
    private long totalRefreshTokens;
    private long validAccessTokens;
    private long validRefreshTokens;
    private long expiredAccessTokens;
    private long expiredRefreshTokens;
    private long tokensNearExpiration;
    private long refreshTokensNearExpiration;
    private Map<String, Long> tokensByPlatform;
    private Map<String, Long> validTokensByPlatform;
    private LocalDateTime lastCleanupTime;
    private long totalPagesWithValidTokens;
    private long totalPagesRequiringReauth;
    
    public TokenStatistics() {}
    
    public TokenStatistics(long totalAccessTokens, long totalRefreshTokens, 
                          long validAccessTokens, long validRefreshTokens,
                          long expiredAccessTokens, long expiredRefreshTokens,
                          long tokensNearExpiration, long refreshTokensNearExpiration,
                          Map<String, Long> tokensByPlatform, Map<String, Long> validTokensByPlatform,
                          LocalDateTime lastCleanupTime, long totalPagesWithValidTokens,
                          long totalPagesRequiringReauth) {
        this.totalAccessTokens = totalAccessTokens;
        this.totalRefreshTokens = totalRefreshTokens;
        this.validAccessTokens = validAccessTokens;
        this.validRefreshTokens = validRefreshTokens;
        this.expiredAccessTokens = expiredAccessTokens;
        this.expiredRefreshTokens = expiredRefreshTokens;
        this.tokensNearExpiration = tokensNearExpiration;
        this.refreshTokensNearExpiration = refreshTokensNearExpiration;
        this.tokensByPlatform = tokensByPlatform;
        this.validTokensByPlatform = validTokensByPlatform;
        this.lastCleanupTime = lastCleanupTime;
        this.totalPagesWithValidTokens = totalPagesWithValidTokens;
        this.totalPagesRequiringReauth = totalPagesRequiringReauth;
    }
    
    // Getters and setters
    public long getTotalAccessTokens() {
        return totalAccessTokens;
    }
    
    public void setTotalAccessTokens(long totalAccessTokens) {
        this.totalAccessTokens = totalAccessTokens;
    }
    
    public long getTotalRefreshTokens() {
        return totalRefreshTokens;
    }
    
    public void setTotalRefreshTokens(long totalRefreshTokens) {
        this.totalRefreshTokens = totalRefreshTokens;
    }
    
    public long getValidAccessTokens() {
        return validAccessTokens;
    }
    
    public void setValidAccessTokens(long validAccessTokens) {
        this.validAccessTokens = validAccessTokens;
    }
    
    public long getValidRefreshTokens() {
        return validRefreshTokens;
    }
    
    public void setValidRefreshTokens(long validRefreshTokens) {
        this.validRefreshTokens = validRefreshTokens;
    }
    
    public long getExpiredAccessTokens() {
        return expiredAccessTokens;
    }
    
    public void setExpiredAccessTokens(long expiredAccessTokens) {
        this.expiredAccessTokens = expiredAccessTokens;
    }
    
    public long getExpiredRefreshTokens() {
        return expiredRefreshTokens;
    }
    
    public void setExpiredRefreshTokens(long expiredRefreshTokens) {
        this.expiredRefreshTokens = expiredRefreshTokens;
    }
    
    public long getTokensNearExpiration() {
        return tokensNearExpiration;
    }
    
    public void setTokensNearExpiration(long tokensNearExpiration) {
        this.tokensNearExpiration = tokensNearExpiration;
    }
    
    public long getRefreshTokensNearExpiration() {
        return refreshTokensNearExpiration;
    }
    
    public void setRefreshTokensNearExpiration(long refreshTokensNearExpiration) {
        this.refreshTokensNearExpiration = refreshTokensNearExpiration;
    }
    
    public Map<String, Long> getTokensByPlatform() {
        return tokensByPlatform;
    }
    
    public void setTokensByPlatform(Map<String, Long> tokensByPlatform) {
        this.tokensByPlatform = tokensByPlatform;
    }
    
    public Map<String, Long> getValidTokensByPlatform() {
        return validTokensByPlatform;
    }
    
    public void setValidTokensByPlatform(Map<String, Long> validTokensByPlatform) {
        this.validTokensByPlatform = validTokensByPlatform;
    }
    
    public LocalDateTime getLastCleanupTime() {
        return lastCleanupTime;
    }
    
    public void setLastCleanupTime(LocalDateTime lastCleanupTime) {
        this.lastCleanupTime = lastCleanupTime;
    }
    
    public long getTotalPagesWithValidTokens() {
        return totalPagesWithValidTokens;
    }
    
    public void setTotalPagesWithValidTokens(long totalPagesWithValidTokens) {
        this.totalPagesWithValidTokens = totalPagesWithValidTokens;
    }
    
    public long getTotalPagesRequiringReauth() {
        return totalPagesRequiringReauth;
    }
    
    public void setTotalPagesRequiringReauth(long totalPagesRequiringReauth) {
        this.totalPagesRequiringReauth = totalPagesRequiringReauth;
    }
    
    /**
     * Calculate the percentage of valid access tokens
     * @return Percentage of valid access tokens (0-100)
     */
    public double getValidAccessTokenPercentage() {
        return totalAccessTokens > 0 ? (double) validAccessTokens / totalAccessTokens * 100 : 0;
    }
    
    /**
     * Calculate the percentage of valid refresh tokens
     * @return Percentage of valid refresh tokens (0-100)
     */
    public double getValidRefreshTokenPercentage() {
        return totalRefreshTokens > 0 ? (double) validRefreshTokens / totalRefreshTokens * 100 : 0;
    }
    
    /**
     * Calculate the percentage of pages with healthy connections
     * @return Percentage of pages with valid tokens (0-100)
     */
    public double getHealthyConnectionPercentage() {
        long totalPages = totalPagesWithValidTokens + totalPagesRequiringReauth;
        return totalPages > 0 ? (double) totalPagesWithValidTokens / totalPages * 100 : 0;
    }
    
    /**
     * Check if the token system is healthy (>80% valid tokens)
     * @return true if system is healthy, false otherwise
     */
    public boolean isSystemHealthy() {
        return getValidAccessTokenPercentage() > 80 && getValidRefreshTokenPercentage() > 80;
    }
    
    @Override
    public String toString() {
        return "TokenStatistics{" +
                "totalAccessTokens=" + totalAccessTokens +
                ", totalRefreshTokens=" + totalRefreshTokens +
                ", validAccessTokens=" + validAccessTokens +
                ", validRefreshTokens=" + validRefreshTokens +
                ", expiredAccessTokens=" + expiredAccessTokens +
                ", expiredRefreshTokens=" + expiredRefreshTokens +
                ", tokensNearExpiration=" + tokensNearExpiration +
                ", refreshTokensNearExpiration=" + refreshTokensNearExpiration +
                ", totalPagesWithValidTokens=" + totalPagesWithValidTokens +
                ", totalPagesRequiringReauth=" + totalPagesRequiringReauth +
                ", validAccessTokenPercentage=" + String.format("%.2f", getValidAccessTokenPercentage()) + "%" +
                ", validRefreshTokenPercentage=" + String.format("%.2f", getValidRefreshTokenPercentage()) + "%" +
                ", healthyConnectionPercentage=" + String.format("%.2f", getHealthyConnectionPercentage()) + "%" +
                ", systemHealthy=" + isSystemHealthy() +
                '}';
    }
}