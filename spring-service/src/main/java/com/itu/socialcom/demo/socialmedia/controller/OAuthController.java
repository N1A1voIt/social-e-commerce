package com.itu.socialcom.demo.socialmedia.controller;

import com.itu.socialcom.demo.socialmedia.dto.OAuthTokenResponse;
import com.itu.socialcom.demo.socialmedia.dto.SocialMediaPage;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import com.itu.socialcom.demo.socialmedia.exception.*;
import com.itu.socialcom.demo.socialmedia.oauth.OAuthStrategy;
import com.itu.socialcom.demo.socialmedia.oauth.OAuthStrategyFactory;
import com.itu.socialcom.demo.socialmedia.service.PageManagementService;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for handling OAuth authorization flows across different social media platforms.
 * Provides endpoints for initiating OAuth flows and handling authorization callbacks.
 */
@RestController
@RequestMapping("/api/oauth")
@CrossOrigin(origins = "*")
public class OAuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuthController.class);
    private static final String STATE_SESSION_KEY = "oauth_state_";
    private static final int STATE_LENGTH = 32;
    
    private final OAuthStrategyFactory strategyFactory;
    private final PageManagementService pageManagementService;
    private final SecureRandom secureRandom;
    
    public OAuthController(OAuthStrategyFactory strategyFactory, PageManagementService pageManagementService) {
        this.strategyFactory = strategyFactory;
        this.pageManagementService = pageManagementService;
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * Initiate OAuth authorization flow for a specific platform
     * @param platform The social media platform (facebook, instagram, x)
     * @param sellerId The seller ID from request header or parameter
     * @param session HTTP session for state management
     * @return Authorization URL and state parameter
     */
    @GetMapping("/{platform}/authorize")
    public ResponseEntity<ApiResponse> initiateOAuth(
            @PathVariable String platform,
            @RequestParam Long sellerId,
            HttpSession session) {
        
        logger.info("Initiating OAuth flow for platform: {} and seller: {}", platform, sellerId);
        
        try {
            // Validate platform support
            if (!strategyFactory.isSupported(platform)) {
                throw new UnsupportedPlatformException("Platform not supported: " + platform);
            }
            
            // Generate secure state parameter
            String state = generateSecureState();
            
            // Store state in session with platform and seller context
            String sessionKey = STATE_SESSION_KEY + platform + "_" + sellerId;
            session.setAttribute(sessionKey, state);
            session.setAttribute(sessionKey + "_seller", sellerId);
            session.setAttribute(sessionKey + "_timestamp", System.currentTimeMillis());
            
            // Get OAuth strategy and generate authorization URL
            OAuthStrategy strategy = strategyFactory.getStrategy(platform);
            String authorizationUrl = strategy.getAuthorizationUrl(state);
            
            // Prepare response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("authorizationUrl", authorizationUrl);
            responseData.put("state", state);
            responseData.put("platform", platform);
            
            ApiResponse response = new ApiResponse();
            response.setStatus(HttpStatus.OK.value());
            response.setData(responseData);
            
            logger.info("OAuth authorization URL generated for platform: {}", platform);
            return ResponseEntity.ok(response);
            
        } catch (UnsupportedPlatformException e) {
            logger.error("Unsupported platform requested: {}", platform, e);
            return handleOAuthError(e, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error initiating OAuth flow for platform: {}", platform, e);
            return handleOAuthError(new OAuthException(platform, "INIT_ERROR", "Failed to initiate OAuth flow", e), 
                                  HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Handle OAuth callback with authorization code
     * @param platform The social media platform
     * @param code OAuth authorization code
     * @param state OAuth state parameter for validation
     * @param error OAuth error parameter (if any)
     * @param session HTTP session for state validation
     * @return Token exchange result and available pages
     */
    @GetMapping("/{platform}/callback")
    public ResponseEntity<ApiResponse> handleOAuthCallback(
            @PathVariable String platform,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String error_description,
            HttpSession session) {
        
        logger.info("Handling OAuth callback for platform: {} with state: {}", platform, state);
        
        try {
            // Handle OAuth error responses
            if (error != null) {
                logger.error("OAuth error received: {} - {}", error, error_description);
                throw new OAuthException(platform, error, error_description != null ? error_description : "OAuth authorization failed");
            }
            
            // Validate required parameters
            if (code == null || state == null) {
                throw new InvalidAuthorizationCodeException(platform, "MISSING_PARAMS", "Missing authorization code or state parameter");
            }
            
            // Validate state parameter
            Long sellerId = validateStateParameter(platform, state, session);
            
            // Get OAuth strategy and exchange code for tokens
            OAuthStrategy strategy = strategyFactory.getStrategy(platform);
            OAuthTokenResponse tokenResponse = strategy.exchangeCodeForTokens(code, state);
            
            // Get available pages using the access token
            List<SocialMediaPage> availablePages = strategy.getUserPages(tokenResponse.getAccessToken());
            
            // Prepare response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("platform", platform);
            responseData.put("tokenReceived", true);
            responseData.put("availablePages", availablePages);
            responseData.put("sellerId", sellerId);
            
            ApiResponse response = new ApiResponse();
            response.setStatus(HttpStatus.OK.value());
            response.setData(responseData);
            
            logger.info("OAuth callback processed successfully for platform: {} with {} available pages", 
                       platform, availablePages.size());
            return ResponseEntity.ok(response);
            
        } catch (OAuthException e) {
            logger.error("OAuth callback error for platform: {}", platform, e);
            return handleOAuthError(e, HttpStatus.BAD_REQUEST);
        } catch (UnsupportedPlatformException e) {
            logger.error("Unsupported platform in callback: {}", platform, e);
            return handleOAuthError(e, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error in OAuth callback for platform: {}", platform, e);
            return handleOAuthError(new OAuthException(platform, "CALLBACK_ERROR", "Failed to process OAuth callback", e), 
                                  HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Connect a specific page after OAuth authorization
     * @param platform The social media platform
     * @param request Connection request containing page details
     * @param session HTTP session for validation
     * @return Connected page information
     */
    @PostMapping("/{platform}/connect")
    public ResponseEntity<ApiResponse> connectPage(
            @PathVariable String platform,
            @RequestBody PageConnectionRequest request,
            HttpSession session) {
        
        logger.info("Connecting page for platform: {} and seller: {}", platform, request.getSellerId());
        
        try {
            // Validate request
            if (request.getSellerId() == null || request.getPageId() == null || request.getAccessToken() == null) {
                throw new IllegalArgumentException("Missing required parameters: sellerId, pageId, or accessToken");
            }
            
            // Check if page is already connected
            if (pageManagementService.isPageAlreadyConnected(request.getSellerId(), platform, request.getPageId())) {
                throw new OAuthException(platform, "PAGE_ALREADY_CONNECTED", "Page is already connected for this seller");
            }
            
            // Connect the specific page
            ManagedPage connectedPage = pageManagementService.connectSpecificPage(
                request.getSellerId(), 
                platform, 
                request.getPageId(), 
                request.getAccessToken()
            );
            
            // Prepare response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("connectedPage", connectedPage);
            responseData.put("platform", platform);
            responseData.put("status", "connected");
            
            ApiResponse response = new ApiResponse();
            response.setStatus(HttpStatus.CREATED.value());
            response.setData(responseData);
            
            logger.info("Page connected successfully: {} for platform: {}", connectedPage.getId(), platform);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (OAuthException e) {
            logger.error("Error connecting page for platform: {}", platform, e);
            return handleOAuthError(e, HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for connecting page: {}", e.getMessage());
            return handleOAuthError(new OAuthException(platform, "INVALID_REQUEST", e.getMessage()), 
                                  HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error connecting page for platform: {}", platform, e);
            return handleOAuthError(new OAuthException(platform, "CONNECTION_ERROR", "Failed to connect page", e), 
                                  HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get supported platforms
     * @return List of supported platform identifiers
     */
    @GetMapping("/platforms")
    public ResponseEntity<ApiResponse> getSupportedPlatforms() {
        try {
            ApiResponse response = new ApiResponse();
            response.setStatus(HttpStatus.OK.value());
            response.setData(strategyFactory.getSupportedPlatforms());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving supported platforms", e);
            return handleOAuthError(new OAuthException("system", "PLATFORM_ERROR", "Failed to retrieve supported platforms", e), 
                                  HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Generate cryptographically secure state parameter
     * @return Base64 encoded random state string
     */
    private String generateSecureState() {
        byte[] randomBytes = new byte[STATE_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
    
    /**
     * Validate OAuth state parameter against session
     * @param platform The platform identifier
     * @param state The state parameter to validate
     * @param session HTTP session containing stored state
     * @return Seller ID associated with the state
     * @throws OAuthException if state validation fails
     */
    private Long validateStateParameter(String platform, String state, HttpSession session) {
        String sessionKey = STATE_SESSION_KEY + platform + "_";
        
        // Find matching session state (we need to iterate since we don't know the seller ID)
        for (String attributeName : java.util.Collections.list(session.getAttributeNames())) {
            if (attributeName.startsWith(sessionKey) && !attributeName.endsWith("_seller") && !attributeName.endsWith("_timestamp")) {
                String storedState = (String) session.getAttribute(attributeName);
                if (state.equals(storedState)) {
                    // Extract seller ID from session
                    Long sellerId = (Long) session.getAttribute(attributeName + "_seller");
                    Long timestamp = (Long) session.getAttribute(attributeName + "_timestamp");
                    
                    // Check state expiration (10 minutes)
                    if (timestamp != null && (System.currentTimeMillis() - timestamp) > 600000) {
                        session.removeAttribute(attributeName);
                        session.removeAttribute(attributeName + "_seller");
                        session.removeAttribute(attributeName + "_timestamp");
                        throw new OAuthException(platform, "STATE_EXPIRED", "OAuth state parameter has expired");
                    }
                    
                    // Clean up session
                    session.removeAttribute(attributeName);
                    session.removeAttribute(attributeName + "_seller");
                    session.removeAttribute(attributeName + "_timestamp");
                    
                    return sellerId;
                }
            }
        }
        
        throw new OAuthException(platform, "INVALID_STATE", "Invalid or expired OAuth state parameter");
    }
    
    /**
     * Handle OAuth-related errors and create appropriate response
     * @param exception The OAuth exception
     * @param status HTTP status code
     * @return Error response entity
     */
    private ResponseEntity<ApiResponse> handleOAuthError(Exception exception, HttpStatus status) {
        Map<String, Object> errorData = new HashMap<>();
        
        if (exception instanceof OAuthException) {
            OAuthException oauthEx = (OAuthException) exception;
            errorData.put("platform", oauthEx.getPlatform());
            errorData.put("errorCode", oauthEx.getErrorCode());
            errorData.put("message", oauthEx.getMessage());
        } else {
            errorData.put("message", exception.getMessage());
            errorData.put("type", exception.getClass().getSimpleName());
        }
        
        ApiResponse response = new ApiResponse();
        response.setStatus(status.value());
        response.setData(errorData);
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * DTO for page connection requests
     */
    public static class PageConnectionRequest {
        private Long sellerId;
        private String pageId;
        private String accessToken;
        private String pageName;
        
        public PageConnectionRequest() {}
        
        public Long getSellerId() { return sellerId; }
        public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
        
        public String getPageId() { return pageId; }
        public void setPageId(String pageId) { this.pageId = pageId; }
        
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        
        public String getPageName() { return pageName; }
        public void setPageName(String pageName) { this.pageName = pageName; }
    }
}