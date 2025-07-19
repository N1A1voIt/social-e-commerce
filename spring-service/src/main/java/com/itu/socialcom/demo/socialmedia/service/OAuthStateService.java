package com.itu.socialcom.demo.socialmedia.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Service for generating and validating OAuth state parameters.
 * Provides CSRF protection and session binding for OAuth flows.
 */
@Service
public class OAuthStateService {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuthStateService.class);
    
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int STATE_LENGTH = 32;
    private static final int STATE_EXPIRY_MINUTES = 10;
    private static final String STATE_PREFIX = "oauth_state:";
    
    private final SecureRandom secureRandom = new SecureRandom();
    private final String secretKey;
    
    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private OAuthAuditService auditService;
    
    public OAuthStateService() {
        // Generate a secure key for HMAC signing
        this.secretKey = generateSecureKey();
    }
    
    /**
     * Generate a secure state parameter for OAuth flow.
     * 
     * @param sellerId The seller ID initiating the OAuth flow
     * @param platform The platform being connected
     * @param sessionId The user's session ID
     * @param ipAddress The client's IP address
     * @return A secure state parameter
     */
    public String generateState(Long sellerId, String platform, String sessionId, String ipAddress) {
        try {
            // Generate random bytes for the state
            byte[] randomBytes = new byte[STATE_LENGTH];
            secureRandom.nextBytes(randomBytes);
            String randomState = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
            
            // Create state data with metadata
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String stateData = String.format("%s:%s:%s:%s:%s", 
                                           randomState, sellerId, platform, sessionId, timestamp);
            
            // Sign the state data with HMAC
            String signature = generateHmacSignature(stateData);
            String signedState = randomState + "." + signature;
            
            // Store state in Redis with expiration (if available)
            if (redisTemplate != null) {
                String stateKey = STATE_PREFIX + randomState;
                String stateValue = String.format("%s:%s:%s:%s", sellerId, platform, sessionId, ipAddress);
                redisTemplate.opsForValue().set(stateKey, stateValue, STATE_EXPIRY_MINUTES, TimeUnit.MINUTES);
            }
            
            logger.debug("Generated OAuth state for seller {} on platform {}: {}", 
                        sellerId, platform, randomState);
            
            return signedState;
            
        } catch (Exception e) {
            logger.error("Failed to generate OAuth state for seller {} on platform {}: {}", 
                        sellerId, platform, e.getMessage(), e);
            throw new RuntimeException("Failed to generate secure state parameter", e);
        }
    }
    
    /**
     * Validate an OAuth state parameter.
     * 
     * @param state The state parameter to validate
     * @param sellerId The expected seller ID
     * @param platform The expected platform
     * @param sessionId The expected session ID
     * @param ipAddress The client's IP address
     * @return true if the state is valid, false otherwise
     */
    public boolean validateState(String state, Long sellerId, String platform, String sessionId, String ipAddress) {
        try {
            if (state == null || state.isEmpty()) {
                auditService.logStateValidation(platform, sellerId, false, "null_or_empty", ipAddress);
                return false;
            }
            
            // Split state into random part and signature
            String[] parts = state.split("\\.");
            if (parts.length != 2) {
                auditService.logStateValidation(platform, sellerId, false, "invalid_format", ipAddress);
                return false;
            }
            
            String randomState = parts[0];
            String providedSignature = parts[1];
            
            // Verify state exists in Redis (if available)
            if (redisTemplate != null) {
                String stateKey = STATE_PREFIX + randomState;
                String storedStateValue = redisTemplate.opsForValue().get(stateKey);
                
                if (storedStateValue == null) {
                    auditService.logStateValidation(platform, sellerId, false, "expired_or_not_found", ipAddress);
                    return false;
                }
                
                // Parse stored state value
                String[] storedParts = storedStateValue.split(":");
                if (storedParts.length != 4) {
                    auditService.logStateValidation(platform, sellerId, false, "corrupted_stored_state", ipAddress);
                    return false;
                }
                
                Long storedSellerId = Long.parseLong(storedParts[0]);
                String storedPlatform = storedParts[1];
                String storedSessionId = storedParts[2];
                String storedIpAddress = storedParts[3];
                
                // Validate stored values match expected values
                if (!storedSellerId.equals(sellerId) || 
                    !storedPlatform.equals(platform) || 
                    !storedSessionId.equals(sessionId)) {
                    
                    auditService.logSuspiciousActivity("STATE_MISMATCH", platform, sellerId, 
                                                     String.format("Expected: %s/%s/%s, Got: %s/%s/%s", 
                                                                  sellerId, platform, sessionId,
                                                                  storedSellerId, storedPlatform, storedSessionId), 
                                                     ipAddress);
                    return false;
                }
                
                // Check IP address consistency (warn but don't fail for mobile users)
                if (!storedIpAddress.equals(ipAddress)) {
                    logger.warn("IP address changed during OAuth flow for seller {} on platform {}: {} -> {}", 
                              sellerId, platform, storedIpAddress, ipAddress);
                }
                
                // Remove used state to prevent replay attacks
                redisTemplate.delete(stateKey);
            }
            
            // Verify HMAC signature
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String expectedStateData = String.format("%s:%s:%s:%s:%s", 
                                                   randomState, sellerId, platform, sessionId, timestamp);
            String expectedSignature = generateHmacSignature(expectedStateData);
            
            // Use time-constant comparison to prevent timing attacks
            boolean signatureValid = constantTimeEquals(providedSignature, expectedSignature);
            
            auditService.logStateValidation(platform, sellerId, signatureValid, randomState, ipAddress);
            
            return signatureValid;
            
        } catch (Exception e) {
            logger.error("Failed to validate OAuth state for seller {} on platform {}: {}", 
                        sellerId, platform, e.getMessage(), e);
            auditService.logStateValidation(platform, sellerId, false, "validation_error", ipAddress);
            return false;
        }
    }
    
    /**
     * Generate HMAC signature for state data.
     */
    private String generateHmacSignature(String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        mac.init(secretKeySpec);
        
        byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
    }
    
    /**
     * Generate a secure key for HMAC signing.
     */
    private String generateSecureKey() {
        byte[] keyBytes = new byte[32]; // 256 bits
        secureRandom.nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
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
     * Clean up expired states (for systems without Redis).
     */
    public void cleanupExpiredStates() {
        // This would be implemented if using database storage instead of Redis
        logger.debug("Cleanup expired states called - using Redis TTL for automatic cleanup");
    }
}