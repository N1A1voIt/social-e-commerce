package com.itu.socialcom.demo.socialmedia.service;

/**
 * Enum representing the connection status of a managed social media page.
 * Provides detailed status information beyond simple active/inactive.
 */
public enum PageConnectionStatus {
    
    /**
     * Page is connected and tokens are valid
     */
    CONNECTED("Connected", "Page is successfully connected with valid tokens"),
    
    /**
     * Page connection is inactive (manually disconnected)
     */
    DISCONNECTED("Disconnected", "Page has been manually disconnected"),
    
    /**
     * Access token has expired but refresh token is available
     */
    TOKEN_EXPIRED("Token Expired", "Access token expired, refresh available"),
    
    /**
     * Both access and refresh tokens have expired, re-authentication required
     */
    AUTHENTICATION_REQUIRED("Authentication Required", "Tokens expired, re-authentication needed"),
    
    /**
     * Token refresh failed, manual intervention required
     */
    REFRESH_FAILED("Refresh Failed", "Token refresh failed, manual re-authentication required"),
    
    /**
     * Connection is in an error state
     */
    ERROR("Error", "Connection is in an error state"),
    
    /**
     * Connection status is unknown or being determined
     */
    UNKNOWN("Unknown", "Connection status is being determined");
    
    private final String displayName;
    private final String description;
    
    PageConnectionStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if the status indicates a healthy connection
     * @return true if status is CONNECTED, false otherwise
     */
    public boolean isHealthy() {
        return this == CONNECTED;
    }
    
    /**
     * Check if the status indicates tokens need refresh
     * @return true if status is TOKEN_EXPIRED, false otherwise
     */
    public boolean needsTokenRefresh() {
        return this == TOKEN_EXPIRED;
    }
    
    /**
     * Check if the status indicates re-authentication is required
     * @return true if status requires re-authentication, false otherwise
     */
    public boolean requiresReAuthentication() {
        return this == AUTHENTICATION_REQUIRED || this == REFRESH_FAILED;
    }
    
    /**
     * Check if the status indicates an error condition
     * @return true if status indicates error, false otherwise
     */
    public boolean isError() {
        return this == ERROR || this == REFRESH_FAILED;
    }
    
    /**
     * Determine status based on page and token conditions
     * @param pageActive Whether the page is marked as active
     * @param hasValidAccessToken Whether there's a valid access token
     * @param hasValidRefreshToken Whether there's a valid refresh token
     * @return Appropriate PageConnectionStatus
     */
    public static PageConnectionStatus determineStatus(boolean pageActive, boolean hasValidAccessToken, boolean hasValidRefreshToken) {
        if (!pageActive) {
            return DISCONNECTED;
        }
        
        if (hasValidAccessToken) {
            return CONNECTED;
        }
        
        if (hasValidRefreshToken) {
            return TOKEN_EXPIRED;
        }
        
        return AUTHENTICATION_REQUIRED;
    }
}