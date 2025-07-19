package com.itu.socialcom.demo.socialmedia.controller;

import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import com.itu.socialcom.demo.socialmedia.exception.OAuthException;
import com.itu.socialcom.demo.socialmedia.service.ManagedPageWithDetails;
import com.itu.socialcom.demo.socialmedia.service.PageConnectionStatus;
import com.itu.socialcom.demo.socialmedia.service.PageManagementService;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing social media page connections.
 * Provides endpoints for listing, connecting, disconnecting, and managing page status.
 */
@RestController
@RequestMapping("/api/pages")
@CrossOrigin(origins = "*")
public class PageManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(PageManagementController.class);
    
    private final PageManagementService pageManagementService;
    
    public PageManagementController(PageManagementService pageManagementService) {
        this.pageManagementService = pageManagementService;
    }
    
    /**
     * Get all connected pages for a seller
     * @param sellerId The seller's ID
     * @return List of connected pages with their details
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getConnectedPages(@RequestParam Long sellerId) {
        logger.info("Retrieving connected pages for seller: {}", sellerId);
        
        try {
            if (sellerId == null) {
                throw new IllegalArgumentException("Seller ID is required");
            }
            
            List<ManagedPageWithDetails> connectedPages = pageManagementService.getConnectedPages(sellerId);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("pages", connectedPages);
            responseData.put("totalCount", connectedPages.size());
            responseData.put("sellerId", sellerId);
            
            ApiResponse response = new ApiResponse();
            response.setStatus(HttpStatus.OK.value());
            response.setData(responseData);
            
            logger.info("Retrieved {} connected pages for seller: {}", connectedPages.size(), sellerId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for getting connected pages: {}", e.getMessage());
            return handleError("INVALID_REQUEST", e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error retrieving connected pages for seller: {}", sellerId, e);
            return handleError("RETRIEVAL_ERROR", "Failed to retrieve connected pages", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get details of a specific managed page
     * @param sellerId The seller's ID
     * @param pageId The managed page ID
     * @return Page details including status and platform information
     */
    @GetMapping("/{pageId}")
    public ResponseEntity<ApiResponse> getPageDetails(
            @RequestParam Long sellerId,
            @PathVariable Long pageId) {
        
        logger.info("Retrieving page details for seller: {} and page: {}", sellerId, pageId);
        
        try {
            if (sellerId == null || pageId == null) {
                throw new IllegalArgumentException("Seller ID and Page ID are required");
            }
            
            Optional<ManagedPage> managedPage = pageManagementService.getManagedPage(sellerId, pageId);
            
            if (managedPage.isEmpty()) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "Page not found or not owned by seller");
                errorData.put("sellerId", sellerId);
                errorData.put("pageId", pageId);
                
                ApiResponse response = new ApiResponse();
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setData(errorData);
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Get page status
            PageConnectionStatus status = pageManagementService.getPageStatus(pageId);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("page", managedPage.get());
            responseData.put("status", status);
            responseData.put("sellerId", sellerId);
            
            ApiResponse response = new ApiResponse();
            response.setStatus(HttpStatus.OK.value());
            response.setData(responseData);
            
            logger.info("Retrieved page details for page: {}", pageId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for getting page details: {}", e.getMessage());
            return handleError("INVALID_REQUEST", e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error retrieving page details for page: {}", pageId, e);
            return handleError("RETRIEVAL_ERROR", "Failed to retrieve page details", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Disconnect a social media page
     * @param sellerId The seller's ID
     * @param pageId The managed page ID to disconnect
     * @return Confirmation of disconnection
     */
    @DeleteMapping("/{pageId}")
    public ResponseEntity<ApiResponse> disconnectPage(
            @RequestParam Long sellerId,
            @PathVariable Long pageId) {
        
        logger.info("Disconnecting page for seller: {} and page: {}", sellerId, pageId);
        
        try {
            if (sellerId == null || pageId == null) {
                throw new IllegalArgumentException("Seller ID and Page ID are required");
            }
            
            // Verify page exists and belongs to seller
            Optional<ManagedPage> managedPage = pageManagementService.getManagedPage(sellerId, pageId);
            if (managedPage.isEmpty()) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "Page not found or not owned by seller");
                errorData.put("sellerId", sellerId);
                errorData.put("pageId", pageId);
                
                ApiResponse response = new ApiResponse();
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setData(errorData);
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Disconnect the page
            pageManagementService.disconnectPage(sellerId, pageId);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "Page disconnected successfully");
            responseData.put("pageId", pageId);
            responseData.put("sellerId", sellerId);
            responseData.put("status", "disconnected");
            
            ApiResponse response = new ApiResponse();
            response.setStatus(HttpStatus.OK.value());
            response.setData(responseData);
            
            logger.info("Page disconnected successfully: {}", pageId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for disconnecting page: {}", e.getMessage());
            return handleError("INVALID_REQUEST", e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (OAuthException e) {
            logger.error("OAuth error disconnecting page: {}", pageId, e);
            return handleOAuthError(e, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error disconnecting page: {}", pageId, e);
            return handleError("DISCONNECTION_ERROR", "Failed to disconnect page", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Manually refresh tokens for a specific page
     * @param sellerId The seller's ID
     * @param pageId The managed page ID
     * @return Token refresh result
     */
    @PostMapping("/{pageId}/refresh")
    public ResponseEntity<ApiResponse> refreshPageTokens(
            @RequestParam Long sellerId,
            @PathVariable Long pageId) {
        
        logger.info("Refreshing tokens for seller: {} and page: {}", sellerId, pageId);
        
        try {
            if (sellerId == null || pageId == null) {
                throw new IllegalArgumentException("Seller ID and Page ID are required");
            }
            
            // Verify page exists and belongs to seller
            Optional<ManagedPage> managedPage = pageManagementService.getManagedPage(sellerId, pageId);
            if (managedPage.isEmpty()) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "Page not found or not owned by seller");
                errorData.put("sellerId", sellerId);
                errorData.put("pageId", pageId);
                
                ApiResponse response = new ApiResponse();
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setData(errorData);
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Refresh page tokens
            pageManagementService.refreshPageTokens(pageId);
            
            // Get updated status
            PageConnectionStatus status = pageManagementService.getPageStatus(pageId);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "Tokens refreshed successfully");
            responseData.put("pageId", pageId);
            responseData.put("sellerId", sellerId);
            responseData.put("status", status);
            
            ApiResponse response = new ApiResponse();
            response.setStatus(HttpStatus.OK.value());
            response.setData(responseData);
            
            logger.info("Tokens refreshed successfully for page: {}", pageId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for refreshing tokens: {}", e.getMessage());
            return handleError("INVALID_REQUEST", e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (OAuthException e) {
            logger.error("OAuth error refreshing tokens for page: {}", pageId, e);
            return handleOAuthError(e, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error refreshing tokens for page: {}", pageId, e);
            return handleError("REFRESH_ERROR", "Failed to refresh tokens", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get connection status for a specific page
     * @param sellerId The seller's ID
     * @param pageId The managed page ID
     * @return Page connection status details
     */
    @GetMapping("/{pageId}/status")
    public ResponseEntity<ApiResponse> getPageStatus(
            @RequestParam Long sellerId,
            @PathVariable Long pageId) {
        
        logger.info("Getting status for seller: {} and page: {}", sellerId, pageId);
        
        try {
            if (sellerId == null || pageId == null) {
                throw new IllegalArgumentException("Seller ID and Page ID are required");
            }
            
            // Verify page exists and belongs to seller
            Optional<ManagedPage> managedPage = pageManagementService.getManagedPage(sellerId, pageId);
            if (managedPage.isEmpty()) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "Page not found or not owned by seller");
                errorData.put("sellerId", sellerId);
                errorData.put("pageId", pageId);
                
                ApiResponse response = new ApiResponse();
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setData(errorData);
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Get page status
            PageConnectionStatus status = pageManagementService.getPageStatus(pageId);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("pageId", pageId);
            responseData.put("sellerId", sellerId);
            responseData.put("status", status);
            
            ApiResponse response = new ApiResponse();
            response.setStatus(HttpStatus.OK.value());
            response.setData(responseData);
            
            logger.info("Retrieved status for page: {}", pageId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for getting page status: {}", e.getMessage());
            return handleError("INVALID_REQUEST", e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error getting status for page: {}", pageId, e);
            return handleError("STATUS_ERROR", "Failed to get page status", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get pages filtered by platform
     * @param sellerId The seller's ID
     * @param platform The platform to filter by (optional)
     * @return List of pages filtered by platform
     */
    @GetMapping("/by-platform")
    public ResponseEntity<ApiResponse> getPagesByPlatform(
            @RequestParam Long sellerId,
            @RequestParam(required = false) String platform) {
        
        logger.info("Retrieving pages for seller: {} and platform: {}", sellerId, platform);
        
        try {
            if (sellerId == null) {
                throw new IllegalArgumentException("Seller ID is required");
            }
            
            List<ManagedPageWithDetails> allPages = pageManagementService.getConnectedPages(sellerId);
            
            // Filter by platform if specified
            List<ManagedPageWithDetails> filteredPages = allPages;
            if (platform != null && !platform.trim().isEmpty()) {
                filteredPages = allPages.stream()
                    .filter(page -> page.getPlatform() != null && 
                           platform.equalsIgnoreCase(page.getPlatform().getLabel()))
                    .toList();
            }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("pages", filteredPages);
            responseData.put("totalCount", filteredPages.size());
            responseData.put("sellerId", sellerId);
            responseData.put("platform", platform);
            
            ApiResponse response = new ApiResponse();
            response.setStatus(HttpStatus.OK.value());
            response.setData(responseData);
            
            logger.info("Retrieved {} pages for seller: {} and platform: {}", 
                       filteredPages.size(), sellerId, platform);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for getting pages by platform: {}", e.getMessage());
            return handleError("INVALID_REQUEST", e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error retrieving pages by platform for seller: {}", sellerId, e);
            return handleError("RETRIEVAL_ERROR", "Failed to retrieve pages by platform", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Handle OAuth-related errors and create appropriate response
     * @param exception The OAuth exception
     * @param status HTTP status code
     * @return Error response entity
     */
    private ResponseEntity<ApiResponse> handleOAuthError(OAuthException exception, HttpStatus status) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("platform", exception.getPlatform());
        errorData.put("errorCode", exception.getErrorCode());
        errorData.put("message", exception.getMessage());
        
        ApiResponse response = new ApiResponse();
        response.setStatus(status.value());
        response.setData(errorData);
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * Handle general errors and create appropriate response
     * @param errorCode The error code
     * @param message The error message
     * @param status HTTP status code
     * @return Error response entity
     */
    private ResponseEntity<ApiResponse> handleError(String errorCode, String message, HttpStatus status) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("errorCode", errorCode);
        errorData.put("message", message);
        
        ApiResponse response = new ApiResponse();
        response.setStatus(status.value());
        response.setData(errorData);
        
        return ResponseEntity.status(status).body(response);
    }
}