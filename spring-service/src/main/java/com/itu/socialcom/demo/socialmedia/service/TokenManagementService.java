package com.itu.socialcom.demo.socialmedia.service;

import com.itu.socialcom.demo.socialmedia.dto.OAuthTokenResponse;

import java.util.Optional;

/**
 * Service interface for managing OAuth tokens with encryption and secure storage.
 * Handles token storage, validation, refresh, and cleanup operations.
 */
public interface TokenManagementService {
    
    /**
     * Store OAuth tokens securely for a managed page
     * @param pageId The managed page ID
     * @param tokens The OAuth token response containing access and refresh tokens
     */
    void storeTokens(Long pageId, OAuthTokenResponse tokens);
    
    /**
     * Store OAuth tokens with platform information
     * @param pageId The managed page ID
     * @param tokens The OAuth token response
     * @param platformId The platform ID
     * @param platform The platform identifier
     */
    void storeTokens(Long pageId, OAuthTokenResponse tokens, Long platformId, String platform);
    
    /**
     * Retrieve valid (non-expired) tokens for a managed page
     * @param pageId The managed page ID
     * @return Optional containing valid tokens if available
     */
    Optional<OAuthTokenResponse> getValidTokens(Long pageId);
    
    /**
     * Check if a page has valid access tokens
     * @param pageId The managed page ID
     * @return true if valid access tokens exist, false otherwise
     */
    boolean hasValidAccessTokens(Long pageId);
    
    /**
     * Check if a page has valid refresh tokens
     * @param pageId The managed page ID
     * @return true if valid refresh tokens exist, false otherwise
     */
    boolean hasValidRefreshTokens(Long pageId);
    
    /**
     * Refresh tokens if they are expired or near expiration
     * @param pageId The managed page ID
     * @return true if tokens were successfully refreshed, false otherwise
     */
    boolean refreshTokensIfNeeded(Long pageId);
    
    /**
     * Force refresh tokens for a page
     * @param pageId The managed page ID
     * @return Optional containing new tokens if refresh was successful
     */
    Optional<OAuthTokenResponse> forceRefreshTokens(Long pageId);
    
    /**
     * Revoke and delete all tokens for a managed page
     * @param pageId The managed page ID
     */
    void revokeTokens(Long pageId);
    
    /**
     * Revoke tokens on the platform and clean up locally
     * @param pageId The managed page ID
     * @param platform The platform identifier
     */
    void revokeTokensOnPlatform(Long pageId, String platform);
    
    /**
     * Clean up expired tokens from the database
     * @return Number of tokens cleaned up
     */
    int cleanupExpiredTokens();
    
    /**
     * Validate token integrity and format
     * @param pageId The managed page ID
     * @return true if tokens are valid and properly formatted, false otherwise
     */
    boolean validateTokenIntegrity(Long pageId);
    
    /**
     * Get the expiration time of access tokens for a page
     * @param pageId The managed page ID
     * @return Optional containing expiration time if tokens exist
     */
    Optional<java.time.LocalDateTime> getAccessTokenExpiration(Long pageId);
    
    /**
     * Get the expiration time of refresh tokens for a page
     * @param pageId The managed page ID
     * @return Optional containing expiration time if tokens exist
     */
    Optional<java.time.LocalDateTime> getRefreshTokenExpiration(Long pageId);
    
    /**
     * Check if access tokens are near expiration (within specified minutes)
     * @param pageId The managed page ID
     * @param minutesThreshold Minutes before expiration to consider "near"
     * @return true if tokens expire within the threshold, false otherwise
     */
    boolean areAccessTokensNearExpiration(Long pageId, int minutesThreshold);
    
    /**
     * Check if refresh tokens are near expiration (within specified days)
     * @param pageId The managed page ID
     * @param daysThreshold Days before expiration to consider "near"
     * @return true if tokens expire within the threshold, false otherwise
     */
    boolean areRefreshTokensNearExpiration(Long pageId, int daysThreshold);
    
    /**
     * Encrypt a token string for secure storage
     * @param token The plain text token
     * @return Encrypted token string
     */
    String encryptToken(String token);
    
    /**
     * Decrypt a token string from storage
     * @param encryptedToken The encrypted token
     * @return Plain text token
     */
    String decryptToken(String encryptedToken);
    
    /**
     * Update tokens with new values (used during token rotation)
     * @param pageId The managed page ID
     * @param newTokens The new OAuth token response
     */
    void updateTokens(Long pageId, OAuthTokenResponse newTokens);
    
    /**
     * Mark old tokens as expired (for audit trail)
     * @param pageId The managed page ID
     */
    void markOldTokensAsExpired(Long pageId);
    
    /**
     * Get token statistics for monitoring
     * @return TokenStatistics object containing various metrics
     */
    TokenStatistics getTokenStatistics();
}