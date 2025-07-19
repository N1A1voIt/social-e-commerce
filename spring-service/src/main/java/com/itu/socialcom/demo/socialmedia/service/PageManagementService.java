package com.itu.socialcom.demo.socialmedia.service;

import com.itu.socialcom.demo.socialmedia.dto.SocialMediaPage;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing social media page connections.
 * Handles connecting, disconnecting, and retrieving page information.
 */
public interface PageManagementService {
    
    /**
     * Get all connected pages for a seller
     * @param sellerId The seller's ID
     * @return List of managed pages with their details
     */
    List<ManagedPageWithDetails> getConnectedPages(Long sellerId);
    
    /**
     * Connect a social media page for a seller
     * @param sellerId The seller's ID
     * @param platform The platform identifier (e.g., "facebook", "instagram")
     * @param authCode The OAuth authorization code
     * @param state The OAuth state parameter for validation
     * @return The connected managed page
     */
    ManagedPage connectPage(Long sellerId, String platform, String authCode, String state);
    
    /**
     * Disconnect a social media page
     * @param sellerId The seller's ID
     * @param pageId The managed page ID to disconnect
     */
    void disconnectPage(Long sellerId, Long pageId);
    
    /**
     * Refresh tokens for a specific page
     * @param pageId The managed page ID
     */
    void refreshPageTokens(Long pageId);
    
    /**
     * Get the connection status of a page
     * @param pageId The managed page ID
     * @return The page connection status
     */
    PageConnectionStatus getPageStatus(Long pageId);
    
    /**
     * Get available pages from a platform for connection
     * @param sellerId The seller's ID
     * @param platform The platform identifier
     * @param accessToken The access token to use for API calls
     * @return List of available social media pages
     */
    List<SocialMediaPage> getAvailablePages(Long sellerId, String platform, String accessToken);
    
    /**
     * Connect a specific page from the available pages
     * @param sellerId The seller's ID
     * @param platform The platform identifier
     * @param pageId The platform-specific page ID
     * @param accessToken The access token for the page
     * @return The connected managed page
     */
    ManagedPage connectSpecificPage(Long sellerId, String platform, String pageId, String accessToken);
    
    /**
     * Get a managed page by ID if it belongs to the seller
     * @param sellerId The seller's ID
     * @param pageId The managed page ID
     * @return Optional containing the managed page if found and owned by seller
     */
    Optional<ManagedPage> getManagedPage(Long sellerId, Long pageId);
    
    /**
     * Check if a page is already connected for a seller
     * @param sellerId The seller's ID
     * @param platform The platform identifier
     * @param platformPageId The platform-specific page ID
     * @return true if page is already connected, false otherwise
     */
    boolean isPageAlreadyConnected(Long sellerId, String platform, String platformPageId);
}