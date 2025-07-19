package com.itu.socialcom.demo.socialmedia.exception;

import com.itu.socialcom.demo.socialmedia.service.OAuthAuditService;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for OAuth-related operations.
 * Provides centralized error handling with user-friendly messages and comprehensive logging.
 */
@ControllerAdvice
public class GlobalOAuthExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalOAuthExceptionHandler.class);
    
    @Autowired
    private OAuthAuditService auditService;
    
    /**
     * Handle token expiration exceptions with automatic refresh attempt guidance.
     */
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiResponse> handleTokenExpired(TokenExpiredException ex, WebRequest request) {
        String ipAddress = getClientIpAddress(request);
        
        logger.warn("Token expired for platform {}: {} - Request: {}", 
                   ex.getPlatform(), ex.getMessage(), request.getDescription(false));
        
        // Log security event for token expiration
        auditService.logSecurityEvent("TOKEN_EXPIRED", ex.getPlatform(), null, 
                                     "Access token expired: " + ex.getMessage(), ipAddress);
        
        ApiResponse response = createErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Your authentication has expired. Please reconnect your " + ex.getPlatform() + " account.",
            "TOKEN_EXPIRED",
            ex.getPlatform(),
            createRetryGuidance("Please click 'Reconnect' to re-authenticate your account.")
        );
        
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * Handle invalid authorization code exceptions.
     */
    @ExceptionHandler(InvalidAuthorizationCodeException.class)
    public ResponseEntity<ApiResponse> handleInvalidAuthCode(InvalidAuthorizationCodeException ex, WebRequest request) {
        String ipAddress = getClientIpAddress(request);
        
        logger.error("Invalid authorization code for platform {}: {} - Request: {}", 
                    ex.getPlatform(), ex.getMessage(), request.getDescription(false));
        
        // Log suspicious activity for invalid auth codes
        auditService.logSuspiciousActivity("INVALID_AUTH_CODE", ex.getPlatform(), null, 
                                          "Invalid authorization code provided: " + ex.getMessage(), ipAddress);
        
        ApiResponse response = createErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "The authorization code is invalid or has expired. Please try connecting your account again.",
            "INVALID_AUTH_CODE",
            ex.getPlatform(),
            createRetryGuidance("Please restart the connection process for your " + ex.getPlatform() + " account.")
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle rate limit exceeded exceptions with retry guidance.
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse> handleRateLimit(RateLimitExceededException ex, WebRequest request) {
        String ipAddress = getClientIpAddress(request);
        
        logger.warn("Rate limit exceeded for platform {}: {} - Retry after: {} seconds - Request: {}", 
                   ex.getPlatform(), ex.getMessage(), ex.getRetryAfterSeconds(), request.getDescription(false));
        
        // Log rate limit event
        auditService.logRateLimitEvent(ex.getPlatform(), null, "oauth_operation", 
                                     ex.getRetryAfterSeconds(), ipAddress);
        
        String userMessage = String.format(
            "Too many requests to %s. Please wait %d seconds before trying again.",
            ex.getPlatform(),
            ex.getRetryAfterSeconds() != null ? ex.getRetryAfterSeconds() : 60
        );
        
        Map<String, Object> additionalInfo = createRetryGuidance(
            "The system will automatically retry in a few moments. You can also try again manually later."
        );
        additionalInfo.put("retryAfterSeconds", ex.getRetryAfterSeconds());
        additionalInfo.put("retryAt", LocalDateTime.now().plusSeconds(ex.getRetryAfterSeconds() != null ? ex.getRetryAfterSeconds() : 60));
        
        ApiResponse response = createErrorResponse(
            HttpStatus.TOO_MANY_REQUESTS.value(),
            userMessage,
            "RATE_LIMIT_EXCEEDED",
            ex.getPlatform(),
            additionalInfo
        );
        
        // Add Retry-After header
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds() != null ? ex.getRetryAfterSeconds() : 60))
                .body(response);
    }
    
    /**
     * Handle unsupported platform exceptions.
     */
    @ExceptionHandler(UnsupportedPlatformException.class)
    public ResponseEntity<ApiResponse> handleUnsupportedPlatform(UnsupportedPlatformException ex, WebRequest request) {
        logger.error("Unsupported platform requested: {} - Request: {}", 
                    ex.getMessage(), request.getDescription(false));
        
        ApiResponse response = createErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "The requested social media platform is not supported. Please choose from Facebook, Instagram, or X (Twitter).",
            "UNSUPPORTED_PLATFORM",
            "unknown",
            createRetryGuidance("Please select a supported platform: Facebook, Instagram, or X (Twitter).")
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle general OAuth exceptions.
     */
    @ExceptionHandler(OAuthException.class)
    public ResponseEntity<ApiResponse> handleOAuthException(OAuthException ex, WebRequest request) {
        logger.error("OAuth error for platform {}: {} (Code: {}) - Request: {}", 
                    ex.getPlatform(), ex.getMessage(), ex.getErrorCode(), request.getDescription(false), ex);
        
        String userMessage = String.format(
            "There was a problem connecting to %s. %s",
            ex.getPlatform(),
            getUserFriendlyMessage(ex.getErrorCode(), ex.getMessage())
        );
        
        ApiResponse response = createErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            userMessage,
            ex.getErrorCode(),
            ex.getPlatform(),
            createRetryGuidance("Please try connecting your account again. If the problem persists, contact support.")
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle HTTP client errors from external API calls.
     */
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiResponse> handleHttpClientError(HttpClientErrorException ex, WebRequest request) {
        logger.error("HTTP client error during OAuth operation: {} {} - Request: {}", 
                    ex.getStatusCode(), ex.getResponseBodyAsString(), request.getDescription(false));
        
        String userMessage = "There was a problem communicating with the social media platform. Please try again.";
        
        if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            userMessage = "Authentication failed. Please check your credentials and try again.";
        } else if (ex.getStatusCode() == HttpStatus.FORBIDDEN) {
            userMessage = "Access denied. Please ensure you have the necessary permissions.";
        }
        
        ApiResponse response = createErrorResponse(
            ex.getStatusCode().value(),
            userMessage,
            "HTTP_CLIENT_ERROR",
            "external_api",
            createRetryGuidance("Please try the operation again. If the problem persists, contact support.")
        );
        
        return new ResponseEntity<>(response, ex.getStatusCode());
    }
    
    /**
     * Handle HTTP server errors from external API calls.
     */
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ApiResponse> handleHttpServerError(HttpServerErrorException ex, WebRequest request) {
        logger.error("HTTP server error during OAuth operation: {} {} - Request: {}", 
                    ex.getStatusCode(), ex.getResponseBodyAsString(), request.getDescription(false));
        
        ApiResponse response = createErrorResponse(
            HttpStatus.BAD_GATEWAY.value(),
            "The social media platform is currently experiencing issues. Please try again later.",
            "EXTERNAL_SERVICE_ERROR",
            "external_api",
            createRetryGuidance("The issue is on the social media platform's side. Please try again in a few minutes.")
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_GATEWAY);
    }
    
    /**
     * Handle network connectivity issues.
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiResponse> handleResourceAccess(ResourceAccessException ex, WebRequest request) {
        logger.error("Network error during OAuth operation: {} - Request: {}", 
                    ex.getMessage(), request.getDescription(false), ex);
        
        ApiResponse response = createErrorResponse(
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "Unable to connect to the social media platform. Please check your internet connection and try again.",
            "NETWORK_ERROR",
            "network",
            createRetryGuidance("Please check your internet connection and try again. The system will automatically retry failed operations.")
        );
        
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    /**
     * Handle unexpected exceptions during OAuth operations.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(Exception ex, WebRequest request) {
        logger.error("Unexpected error during OAuth operation: {} - Request: {}", 
                    ex.getMessage(), request.getDescription(false), ex);
        
        ApiResponse response = createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred. Please try again or contact support if the problem persists.",
            "INTERNAL_ERROR",
            "system",
            createRetryGuidance("Please try the operation again. If the problem persists, contact support with the error details.")
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Create a standardized error response.
     */
    private ApiResponse createErrorResponse(int status, String message, String errorCode, String platform, Map<String, Object> additionalInfo) {
        ApiResponse response = new ApiResponse();
        response.setStatus(status);
        
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("message", message);
        errorData.put("errorCode", errorCode);
        errorData.put("platform", platform);
        errorData.put("timestamp", LocalDateTime.now());
        
        if (additionalInfo != null) {
            errorData.putAll(additionalInfo);
        }
        
        response.setData(errorData);
        response.setErrors(Collections.emptyList());
        
        return response;
    }
    
    /**
     * Create retry guidance for users.
     */
    private Map<String, Object> createRetryGuidance(String guidance) {
        Map<String, Object> retryInfo = new HashMap<>();
        retryInfo.put("guidance", guidance);
        retryInfo.put("canRetry", true);
        return retryInfo;
    }
    
    /**
     * Convert technical error messages to user-friendly messages.
     */
    private String getUserFriendlyMessage(String errorCode, String technicalMessage) {
        switch (errorCode) {
            case "invalid_grant":
                return "The authorization has expired or been revoked. Please reconnect your account.";
            case "invalid_client":
                return "There's a configuration issue. Please contact support.";
            case "invalid_request":
                return "The request was malformed. Please try again.";
            case "access_denied":
                return "Access was denied. Please ensure you grant the necessary permissions.";
            case "server_error":
                return "The platform is experiencing issues. Please try again later.";
            case "temporarily_unavailable":
                return "The service is temporarily unavailable. Please try again in a few minutes.";
            default:
                return "Please try again or contact support if the issue persists.";
        }
    }
    
    /**
     * Extract client IP address from the request.
     */
    private String getClientIpAddress(WebRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        // Fallback to remote address if available
        return request.getRemoteAddress() != null ? request.getRemoteAddress().toString() : "unknown";
    }
}