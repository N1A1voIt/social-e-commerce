package com.itu.socialcom.demo.socialmedia.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Service for CSRF protection in OAuth flows.
 * Provides additional security layer beyond OAuth state parameter.
 */
@Service
public class OAuthCsrfProtectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuthCsrfProtectionService.class);
    
    private static final String CSRF_TOKEN_ATTRIBUTE = "oauth_csrf_token";
    private static final String CSRF_TOKEN_HEADER = "X-CSRF-Token";
    private static final String CSRF_TOKEN_PARAMETER = "_csrf";
    private static final int CSRF_TOKEN_LENGTH = 32;
    private static final int CSRF_TOKEN_EXPIRY_MINUTES = 30;
    private static final String CSRF_REDIS_PREFIX = "oauth_csrf:";
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private OAuthAuditService auditService;
    
    /**
     * Generate a CSRF token for OAuth flow protection.
     * 
     * @param request The HTTP request
     * @param sellerId The seller ID
     * @param platform The platform being accessed
     * @return The generated CSRF token
     */
    public String generateCsrfToken(HttpServletRequest request, Long sellerId, String platform) {
        // Generate random CSRF token
        byte[] tokenBytes = new byte[CSRF_TOKEN_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        String csrfToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        
        // Store in session
        HttpSession session = request.getSession(true);
        session.setAttribute(CSRF_TOKEN_ATTRIBUTE, csrfToken);
        
        // Also store in Redis with additional metadata (if available)
        if (redisTemplate != null) {
            String redisKey = CSRF_REDIS_PREFIX + csrfToken;
            String tokenData = String.format("%s:%s:%s:%s", 
                                           sellerId, platform, session.getId(), getClientIpAddress(request));
            redisTemplate.opsForValue().set(redisKey, tokenData, CSRF_TOKEN_EXPIRY_MINUTES, TimeUnit.MINUTES);
        }
        
        logger.debug("Generated CSRF token for seller {} on platform {}: {}", 
                    sellerId, platform, csrfToken.substring(0, 8) + "...");
        
        return csrfToken;
    }
    
    /**
     * Validate a CSRF token from the request.
     * 
     * @param request The HTTP request
     * @param sellerId The expected seller ID
     * @param platform The expected platform
     * @return true if CSRF token is valid, false otherwise
     */
    public boolean validateCsrfToken(HttpServletRequest request, Long sellerId, String platform) {
        String providedToken = getCsrfTokenFromRequest(request);
        
        if (providedToken == null || providedToken.isEmpty()) {
            auditService.logSuspiciousActivity("MISSING_CSRF_TOKEN", platform, sellerId, 
                                             "CSRF token missing from request", getClientIpAddress(request));
            return false;
        }
        
        // Check session token
        HttpSession session = request.getSession(false);
        if (session == null) {
            auditService.logSuspiciousActivity("NO_SESSION_FOR_CSRF", platform, sellerId, 
                                             "No session found for CSRF validation", getClientIpAddress(request));
            return false;
        }
        
        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_ATTRIBUTE);
        if (sessionToken == null || !constantTimeEquals(sessionToken, providedToken)) {
            auditService.logSuspiciousActivity("INVALID_CSRF_TOKEN", platform, sellerId, 
                                             "CSRF token mismatch", getClientIpAddress(request));
            return false;
        }
        
        // Additional validation with Redis (if available)
        if (redisTemplate != null) {
            String redisKey = CSRF_REDIS_PREFIX + providedToken;
            String storedTokenData = redisTemplate.opsForValue().get(redisKey);
            
            if (storedTokenData == null) {
                auditService.logSuspiciousActivity("EXPIRED_CSRF_TOKEN", platform, sellerId, 
                                                 "CSRF token not found in Redis (expired?)", getClientIpAddress(request));
                return false;
            }
            
            // Parse stored token data
            String[] parts = storedTokenData.split(":");
            if (parts.length != 4) {
                auditService.logSuspiciousActivity("CORRUPTED_CSRF_DATA", platform, sellerId, 
                                                 "Corrupted CSRF token data in Redis", getClientIpAddress(request));
                return false;
            }
            
            Long storedSellerId = Long.parseLong(parts[0]);
            String storedPlatform = parts[1];
            String storedSessionId = parts[2];
            String storedIpAddress = parts[3];
            
            // Validate stored data matches current request
            if (!storedSellerId.equals(sellerId) || !storedPlatform.equals(platform)) {
                auditService.logSuspiciousActivity("CSRF_DATA_MISMATCH", platform, sellerId, 
                    String.format("CSRF data mismatch - Expected: %s/%s, Stored: %s/%s", 
                                 sellerId, platform, storedSellerId, storedPlatform), 
                    getClientIpAddress(request));
                return false;
            }
            
            if (!storedSessionId.equals(session.getId())) {
                auditService.logSuspiciousActivity("CSRF_SESSION_MISMATCH", platform, sellerId, 
                                                 "CSRF token session ID mismatch", getClientIpAddress(request));
                return false;
            }
            
            // Check IP consistency (warn but don't fail for mobile users)
            String currentIp = getClientIpAddress(request);
            if (!storedIpAddress.equals(currentIp)) {
                logger.warn("IP address changed during OAuth flow for seller {} on platform {}: {} -> {}", 
                          sellerId, platform, storedIpAddress, currentIp);
            }
            
            // Remove used token to prevent replay
            redisTemplate.delete(redisKey);
        }
        
        // Remove token from session after successful validation
        session.removeAttribute(CSRF_TOKEN_ATTRIBUTE);
        
        logger.debug("CSRF token validation successful for seller {} on platform {}", sellerId, platform);
        return true;
    }
    
    /**
     * Get CSRF token from request (header or parameter).
     */
    private String getCsrfTokenFromRequest(HttpServletRequest request) {
        // First check header
        String token = request.getHeader(CSRF_TOKEN_HEADER);
        
        // If not in header, check parameter
        if (token == null || token.isEmpty()) {
            token = request.getParameter(CSRF_TOKEN_PARAMETER);
        }
        
        return token;
    }
    
    /**
     * Extract client IP address from request.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Constant-time string comparison to prevent timing attacks.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        
        return result == 0;
    }
    
    /**
     * Clean up expired CSRF tokens from session.
     */
    public void cleanupExpiredTokens(HttpSession session) {
        if (session != null) {
            session.removeAttribute(CSRF_TOKEN_ATTRIBUTE);
        }
    }
    
    /**
     * Check if request has valid CSRF protection setup.
     */
    public boolean hasValidCsrfSetup(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        
        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_ATTRIBUTE);
        return sessionToken != null && !sessionToken.isEmpty();
    }
    
    /**
     * Generate CSRF token for inclusion in forms/AJAX requests.
     */
    public String getCsrfTokenForResponse(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (String) session.getAttribute(CSRF_TOKEN_ATTRIBUTE);
        }
        return null;
    }
}