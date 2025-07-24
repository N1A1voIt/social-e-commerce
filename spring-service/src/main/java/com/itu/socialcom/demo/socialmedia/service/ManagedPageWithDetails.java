package com.itu.socialcom.demo.socialmedia.service;

import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import com.itu.socialcom.demo.socialmedia.entity.SupportedPlatform;

/**
 * DTO containing managed page information with platform details and token status.
 * Used for displaying comprehensive page information to users.
 */
public class ManagedPageWithDetails {
    
    private ManagedPage page;
    private SupportedPlatform platform;
    private boolean hasValidAccessToken;
    private boolean hasValidRefreshToken;
    private PageConnectionStatus connectionStatus;
    
    public ManagedPageWithDetails() {}
    
    public ManagedPageWithDetails(ManagedPage page, SupportedPlatform platform, 
                                 boolean hasValidAccessToken, boolean hasValidRefreshToken,
                                 PageConnectionStatus connectionStatus) {
        this.page = page;
        this.platform = platform;
        this.hasValidAccessToken = hasValidAccessToken;
        this.hasValidRefreshToken = hasValidRefreshToken;
        this.connectionStatus = connectionStatus;
    }
    
    public ManagedPage getPage() {
        return page;
    }
    
    public void setPage(ManagedPage page) {
        this.page = page;
    }
    
    public SupportedPlatform getPlatform() {
        return platform;
    }
    
    public void setPlatform(SupportedPlatform platform) {
        this.platform = platform;
    }
    
    public boolean isHasValidAccessToken() {
        return hasValidAccessToken;
    }
    
    public void setHasValidAccessToken(boolean hasValidAccessToken) {
        this.hasValidAccessToken = hasValidAccessToken;
    }
    
    public boolean isHasValidRefreshToken() {
        return hasValidRefreshToken;
    }
    
    public void setHasValidRefreshToken(boolean hasValidRefreshToken) {
        this.hasValidRefreshToken = hasValidRefreshToken;
    }
    
    public PageConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }
    
    public void setConnectionStatus(PageConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
    }
    
    /**
     * Check if the page connection is healthy (has valid tokens and is active)
     * @return true if connection is healthy, false otherwise
     */
    public boolean isConnectionHealthy() {
        return page != null && page.isActive() && hasValidAccessToken && 
               connectionStatus == PageConnectionStatus.CONNECTED;
    }
    
    /**
     * Get the platform display name
     * @return Platform display name or "Unknown" if platform is null
     */
    public String getPlatformDisplayName() {
        return platform != null ? platform.getPlatformType().getDisplayName() : "Unknown";
    }
    
    /**
     * Get the platform identifier
     * @return Platform identifier or "unknown" if platform is null
     */
    public String getPlatformIdentifier() {
        return platform != null ? platform.getLabel() : "unknown";
    }
    
    @Override
    public String toString() {
        return "ManagedPageWithDetails{" +
                "page=" + (page != null ? page.getId() : null) +
                ", platform=" + (platform != null ? platform.getLabel() : null) +
                ", hasValidAccessToken=" + hasValidAccessToken +
                ", hasValidRefreshToken=" + hasValidRefreshToken +
                ", connectionStatus=" + connectionStatus +
                '}';
    }
    
    /**
     * Builder pattern for creating ManagedPageWithDetails instances
     */
    public static class Builder {
        private ManagedPage page;
        private SupportedPlatform platform;
        private boolean hasValidAccessToken;
        private boolean hasValidRefreshToken;
        private PageConnectionStatus connectionStatus;
        
        public Builder page(ManagedPage page) {
            this.page = page;
            return this;
        }
        
        public Builder platform(SupportedPlatform platform) {
            this.platform = platform;
            return this;
        }
        
        public Builder hasValidAccessToken(boolean hasValidAccessToken) {
            this.hasValidAccessToken = hasValidAccessToken;
            return this;
        }
        
        public Builder hasValidRefreshToken(boolean hasValidRefreshToken) {
            this.hasValidRefreshToken = hasValidRefreshToken;
            return this;
        }
        
        public Builder connectionStatus(PageConnectionStatus connectionStatus) {
            this.connectionStatus = connectionStatus;
            return this;
        }
        
        public ManagedPageWithDetails build() {
            return new ManagedPageWithDetails(page, platform, hasValidAccessToken, 
                                            hasValidRefreshToken, connectionStatus);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}