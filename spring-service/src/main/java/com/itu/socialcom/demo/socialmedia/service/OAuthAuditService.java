package com.itu.socialcom.demo.socialmedia.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for comprehensive OAuth operation logging and auditing.
 * Provides structured logging for debugging OAuth issues and tracking user activities.
 */
@Service
public class OAuthAuditService {
    
    private static final Logger auditLogger = LoggerFactory.getLogger("OAUTH_AUDIT");
    private static final Logger securityLogger = LoggerFactory.getLogger("OAUTH_SECURITY");
    
    /**
     * Log the start of an OAuth operation.
     */
    public String logOAuthOperationStart(String operation, String platform, Long sellerId, String userAgent, String ipAddress) {
        String operationId = UUID.randomUUID().toString();
        
        // Set MDC for structured logging
        MDC.put("operationId", operationId);
        MDC.put("operation", operation);
        MDC.put("platform", platform);
        MDC.put("sellerId", sellerId != null ? sellerId.toString() : "unknown");
        MDC.put("userAgent", userAgent);
        MDC.put("ipAddress", ipAddress);
        
        auditLogger.info("OAuth operation started: {} for platform {} by seller {} from IP {} - Operation ID: {}", 
                        operation, platform, sellerId, ipAddress, operationId);
        
        return operationId;
    }
    
    /**
     * Log successful completion of an OAuth operation.
     */
    public void logOAuthOperationSuccess(String operationId, String operation, String platform, Long sellerId, 
                                       String additionalInfo) {
        MDC.put("operationId", operationId);
        MDC.put("status", "SUCCESS");
        
        auditLogger.info("OAuth operation completed successfully: {} for platform {} by seller {} - Operation ID: {} - Info: {}", 
                        operation, platform, sellerId, operationId, additionalInfo);
        
        clearMDC();
    }
    
    /**
     * Log failed OAuth operation with error details.
     */
    public void logOAuthOperationFailure(String operationId, String operation, String platform, Long sellerId, 
                                       String errorCode, String errorMessage, Exception exception) {
        MDC.put("operationId", operationId);
        MDC.put("status", "FAILURE");
        MDC.put("errorCode", errorCode);
        
        auditLogger.error("OAuth operation failed: {} for platform {} by seller {} - Operation ID: {} - Error: {} - Message: {}", 
                         operation, platform, sellerId, operationId, errorCode, errorMessage, exception);
        
        clearMDC();
    }
    
    /**
     * Log security-related events during OAuth operations.
     */
    public void logSecurityEvent(String event, String platform, Long sellerId, String details, String ipAddress) {
        securityLogger.warn("OAuth security event: {} for platform {} by seller {} from IP {} - Details: {}", 
                          event, platform, sellerId, ipAddress, details);
    }
    
    /**
     * Log token-related operations for audit trail.
     */
    public void logTokenOperation(String operation, String platform, Long sellerId, Long pageId, 
                                String tokenType, LocalDateTime expirationDate) {
        auditLogger.info("Token operation: {} - Platform: {} - Seller: {} - Page: {} - Type: {} - Expires: {}", 
                        operation, platform, sellerId, pageId, tokenType, expirationDate);
    }
    
    /**
     * Log rate limiting events.
     */
    public void logRateLimitEvent(String platform, Long sellerId, String operation, Long retryAfterSeconds, String ipAddress) {
        auditLogger.warn("Rate limit exceeded: Platform: {} - Seller: {} - Operation: {} - Retry after: {}s - IP: {}", 
                        platform, sellerId, operation, retryAfterSeconds, ipAddress);
    }
    
    /**
     * Log suspicious OAuth activities.
     */
    public void logSuspiciousActivity(String activity, String platform, Long sellerId, String details, String ipAddress) {
        securityLogger.error("Suspicious OAuth activity detected: {} - Platform: {} - Seller: {} - IP: {} - Details: {}", 
                           activity, platform, sellerId, ipAddress, details);
    }
    
    /**
     * Log OAuth state parameter validation events.
     */
    public void logStateValidation(String platform, Long sellerId, boolean isValid, String stateValue, String ipAddress) {
        if (isValid) {
            auditLogger.debug("OAuth state validation successful: Platform: {} - Seller: {} - IP: {}", 
                            platform, sellerId, ipAddress);
        } else {
            securityLogger.warn("OAuth state validation failed: Platform: {} - Seller: {} - State: {} - IP: {}", 
                              platform, sellerId, stateValue, ipAddress);
        }
    }
    
    /**
     * Log token refresh operations.
     */
    public void logTokenRefresh(String platform, Long sellerId, Long pageId, boolean successful, String reason) {
        if (successful) {
            auditLogger.info("Token refresh successful: Platform: {} - Seller: {} - Page: {} - Reason: {}", 
                           platform, sellerId, pageId, reason);
        } else {
            auditLogger.warn("Token refresh failed: Platform: {} - Seller: {} - Page: {} - Reason: {}", 
                           platform, sellerId, pageId, reason);
        }
    }
    
    /**
     * Log page connection/disconnection events.
     */
    public void logPageConnectionEvent(String event, String platform, Long sellerId, Long pageId, 
                                     String pageTitle, String platformIdentifier) {
        auditLogger.info("Page connection event: {} - Platform: {} - Seller: {} - Page: {} - Title: {} - Platform ID: {}", 
                        event, platform, sellerId, pageId, pageTitle, platformIdentifier);
    }
    
    /**
     * Clear MDC context to prevent memory leaks.
     */
    private void clearMDC() {
        MDC.clear();
    }
    
    /**
     * Create a structured log entry for external API calls.
     */
    public void logExternalApiCall(String platform, String endpoint, String method, int responseCode, 
                                 long responseTimeMs, String operationId) {
        auditLogger.debug("External API call: Platform: {} - Endpoint: {} - Method: {} - Response: {} - Time: {}ms - Operation: {}", 
                         platform, endpoint, method, responseCode, responseTimeMs, operationId);
    }
    
    /**
     * Log configuration-related events.
     */
    public void logConfigurationEvent(String event, String platform, String details) {
        auditLogger.info("OAuth configuration event: {} - Platform: {} - Details: {}", event, platform, details);
    }
}