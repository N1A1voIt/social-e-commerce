package com.itu.socialcom.demo.socialmedia.oauth;

import com.itu.socialcom.demo.socialmedia.dto.OAuthTokenResponse;
import com.itu.socialcom.demo.socialmedia.dto.SocialMediaPage;

import java.util.List;

/**
 * OAuth strategy interface for social media platform integrations.
 * Implements the Strategy pattern to handle different OAuth flows across platforms.
 */
public interface OAuthStrategy {
    
    /**
     * Generate the OAuth authorization URL for the platform
     * @param state CSRF protection state parameter
     * @return Authorization URL for user redirection
     */
    String getAuthorizationUrl(String state);
    
    /**
     * Exchange authorization code for access and refresh tokens
     * @param code Authorization code from OAuth callback
     * @param state State parameter for validation
     * @return OAuth token response containing access and refresh tokens
     */
    OAuthTokenResponse exchangeCodeForTokens(String code, String state);
    
    /**
     * Refresh expired access tokens using refresh token
     * @param refreshToken Valid refresh token
     * @return New OAuth token response
     */
    OAuthTokenResponse refreshTokens(String refreshToken);
    
    /**
     * Retrieve user's pages/accounts from the platform
     * @param accessToken Valid access token
     * @return List of available social media pages
     */
    List<SocialMediaPage> getUserPages(String accessToken);
    
    /**
     * Revoke access tokens and disconnect from platform
     * @param accessToken Access token to revoke
     */
    void revokeAccess(String accessToken);
    
    /**
     * Get the platform identifier for this strategy
     * @return Platform name (e.g., "facebook", "instagram", "x")
     */
    String getPlatformName();
}