package com.itu.socialcom.demo.socialmedia.service;

import com.itu.socialcom.demo.socialmedia.exception.RateLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * Service for implementing rate limiting on OAuth endpoints.
 * Provides protection against abuse and ensures compliance with platform rate limits.
 */
@Service
public class OAuthRateLimitService {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuthRateLimitService.class);
    
    // Rate limit configurations
    private static final int OAUTH_REQUESTS_PER_MINUTE = 10;
    private static final int OAUTH_REQUESTS_PER_HOUR = 100;
    private static final int TOKEN_REFRESH_PER_MINUTE = 5;
    private static final int TOKEN_REFRESH_PER_HOUR = 50;
    private static final int FAILED_ATTEMPTS_THRESHOLD = 5;
    private static final int FAILED_ATTEMPTS_WINDOW_MINUTES = 15;
    
    // Redis key prefixes
    private static final String OAUTH_MINUTE_KEY = "oauth_rate:minute:";
    private static final String OAUTH_HOUR_KEY = "oauth_rate:hour:";
    private static final String TOKEN_MINUTE_KEY = "token_rate:minute:";
    private static final String TOKEN_HOUR_KEY = "token_rate:hour:";
    private static final String FAILED_ATTEMPTS_KEY = "oauth_failed:";
    
    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private OAuthAuditService auditService;
    
    /**
     * Check if OAuth request is allowed for the given identifier.
     * 
     * @param identifier Unique identifier (IP address, user ID, etc.)
     * @param platform The platform being accessed
     * @return true if request is allowed, false otherwise
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    public boolean checkOAuthRateLimit(String identifier, String platform) throws RateLimitExceededException {
        if (redisTemplate == null) {
            logger.warn("Redis not available, skipping rate limiting");
            return true;
        }
        
        String currentMinute = getCurrentMinuteKey();
        String currentHour = getCurrentHourKey();
        
        String minuteKey = OAUTH_MINUTE_KEY + identifier + ":" + currentMinute;
        String hourKey = OAUTH_HOUR_KEY + identifier + ":" + currentHour;
        
        // Check minute limit
        Long minuteCount = redisTemplate.opsForValue().increment(minuteKey);
        if (minuteCount == 1) {
            redisTemplate.expire(minuteKey, 1, TimeUnit.MINUTES);
        }
        
        if (minuteCount > OAUTH_REQUESTS_PER_MINUTE) {
            auditService.logRateLimitEvent(platform, null, "oauth_request", 60L, identifier);
            throw new RateLimitExceededException(platform, 
                "OAuth rate limit exceeded: " + minuteCount + " requests in current minute", 60L);
        }
        
        // Check hour limit
        Long hourCount = redisTemplate.opsForValue().increment(hourKey);
        if (hourCount == 1) {
            redisTemplate.expire(hourKey, 1, TimeUnit.HOURS);
        }
        
        if (hourCount > OAUTH_REQUESTS_PER_HOUR) {
            long secondsUntilNextHour = 3600 - (System.currentTimeMillis() / 1000) % 3600;
            auditService.logRateLimitEvent(platform, null, "oauth_request", secondsUntilNextHour, identifier);
            throw new RateLimitExceededException(platform, 
                "OAuth rate limit exceeded: " + hourCount + " requests in current hour", secondsUntilNextHour);
        }
        
        logger.debug("OAuth rate limit check passed for {}: {}/min, {}/hour", 
                    identifier, minuteCount, hourCount);
        
        return true;
    }
    
    /**
     * Check if token refresh request is allowed.
     */
    public boolean checkTokenRefreshRateLimit(String identifier, String platform) throws RateLimitExceededException {
        if (redisTemplate == null) {
            logger.warn("Redis not available, skipping rate limiting");
            return true;
        }
        
        String currentMinute = getCurrentMinuteKey();
        String currentHour = getCurrentHourKey();
        
        String minuteKey = TOKEN_MINUTE_KEY + identifier + ":" + currentMinute;
        String hourKey = TOKEN_HOUR_KEY + identifier + ":" + currentHour;
        
        // Check minute limit
        Long minuteCount = redisTemplate.opsForValue().increment(minuteKey);
        if (minuteCount == 1) {
            redisTemplate.expire(minuteKey, 1, TimeUnit.MINUTES);
        }
        
        if (minuteCount > TOKEN_REFRESH_PER_MINUTE) {
            auditService.logRateLimitEvent(platform, null, "token_refresh", 60L, identifier);
            throw new RateLimitExceededException(platform, 
                "Token refresh rate limit exceeded: " + minuteCount + " requests in current minute", 60L);
        }
        
        // Check hour limit
        Long hourCount = redisTemplate.opsForValue().increment(hourKey);
        if (hourCount == 1) {
            redisTemplate.expire(hourKey, 1, TimeUnit.HOURS);
        }
        
        if (hourCount > TOKEN_REFRESH_PER_HOUR) {
            long secondsUntilNextHour = 3600 - (System.currentTimeMillis() / 1000) % 3600;
            auditService.logRateLimitEvent(platform, null, "token_refresh", secondsUntilNextHour, identifier);
            throw new RateLimitExceededException(platform, 
                "Token refresh rate limit exceeded: " + hourCount + " requests in current hour", secondsUntilNextHour);
        }
        
        logger.debug("Token refresh rate limit check passed for {}: {}/min, {}/hour", 
                    identifier, minuteCount, hourCount);
        
        return true;
    }
    
    /**
     * Record a failed OAuth attempt and check if threshold is exceeded.
     */
    public void recordFailedAttempt(String identifier, String platform, String reason) {
        if (redisTemplate == null) {
            logger.warn("Redis not available, skipping failed attempt tracking");
            return;
        }
        
        String failedKey = FAILED_ATTEMPTS_KEY + identifier;
        Long failedCount = redisTemplate.opsForValue().increment(failedKey);
        
        if (failedCount == 1) {
            redisTemplate.expire(failedKey, FAILED_ATTEMPTS_WINDOW_MINUTES, TimeUnit.MINUTES);
        }
        
        logger.warn("Failed OAuth attempt recorded for {}: {} failures in window (reason: {})", 
                   identifier, failedCount, reason);
        
        if (failedCount >= FAILED_ATTEMPTS_THRESHOLD) {
            auditService.logSuspiciousActivity("EXCESSIVE_FAILED_ATTEMPTS", platform, null, 
                String.format("%d failed attempts in %d minutes: %s", 
                             failedCount, FAILED_ATTEMPTS_WINDOW_MINUTES, reason), identifier);
        }
    }
    
    /**
     * Check if identifier has exceeded failed attempt threshold.
     */
    public boolean isBlocked(String identifier) {
        if (redisTemplate == null) {
            return false;
        }
        
        String failedKey = FAILED_ATTEMPTS_KEY + identifier;
        String failedCountStr = redisTemplate.opsForValue().get(failedKey);
        
        if (failedCountStr != null) {
            long failedCount = Long.parseLong(failedCountStr);
            return failedCount >= FAILED_ATTEMPTS_THRESHOLD;
        }
        
        return false;
    }
    
    /**
     * Clear failed attempts for an identifier (after successful authentication).
     */
    public void clearFailedAttempts(String identifier) {
        if (redisTemplate == null) {
            return;
        }
        
        String failedKey = FAILED_ATTEMPTS_KEY + identifier;
        redisTemplate.delete(failedKey);
        
        logger.debug("Cleared failed attempts for identifier: {}", identifier);
    }
    
    /**
     * Get current rate limit status for an identifier.
     */
    public RateLimitStatus getRateLimitStatus(String identifier) {
        if (redisTemplate == null) {
            return new RateLimitStatus(0, 0, 0, 0, false);
        }
        
        String currentMinute = getCurrentMinuteKey();
        String currentHour = getCurrentHourKey();
        
        String oauthMinuteKey = OAUTH_MINUTE_KEY + identifier + ":" + currentMinute;
        String oauthHourKey = OAUTH_HOUR_KEY + identifier + ":" + currentHour;
        String tokenMinuteKey = TOKEN_MINUTE_KEY + identifier + ":" + currentMinute;
        String tokenHourKey = TOKEN_HOUR_KEY + identifier + ":" + currentHour;
        String failedKey = FAILED_ATTEMPTS_KEY + identifier;
        
        int oauthMinuteCount = getCountFromRedis(oauthMinuteKey);
        int oauthHourCount = getCountFromRedis(oauthHourKey);
        int tokenMinuteCount = getCountFromRedis(tokenMinuteKey);
        int tokenHourCount = getCountFromRedis(tokenHourKey);
        int failedCount = getCountFromRedis(failedKey);
        
        boolean isBlocked = failedCount >= FAILED_ATTEMPTS_THRESHOLD;
        
        return new RateLimitStatus(oauthMinuteCount, oauthHourCount, tokenMinuteCount, tokenHourCount, isBlocked);
    }
    
    /**
     * Get count from Redis, returning 0 if key doesn't exist.
     */
    private int getCountFromRedis(String key) {
        String countStr = redisTemplate.opsForValue().get(key);
        return countStr != null ? Integer.parseInt(countStr) : 0;
    }
    
    /**
     * Get current minute key for rate limiting.
     */
    private String getCurrentMinuteKey() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        return now.toString();
    }
    
    /**
     * Get current hour key for rate limiting.
     */
    private String getCurrentHourKey() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        return now.toString();
    }
    
    /**
     * Rate limit status data class.
     */
    public static class RateLimitStatus {
        private final int oauthRequestsThisMinute;
        private final int oauthRequestsThisHour;
        private final int tokenRefreshThisMinute;
        private final int tokenRefreshThisHour;
        private final boolean isBlocked;
        
        public RateLimitStatus(int oauthRequestsThisMinute, int oauthRequestsThisHour, 
                              int tokenRefreshThisMinute, int tokenRefreshThisHour, boolean isBlocked) {
            this.oauthRequestsThisMinute = oauthRequestsThisMinute;
            this.oauthRequestsThisHour = oauthRequestsThisHour;
            this.tokenRefreshThisMinute = tokenRefreshThisMinute;
            this.tokenRefreshThisHour = tokenRefreshThisHour;
            this.isBlocked = isBlocked;
        }
        
        // Getters
        public int getOauthRequestsThisMinute() { return oauthRequestsThisMinute; }
        public int getOauthRequestsThisHour() { return oauthRequestsThisHour; }
        public int getTokenRefreshThisMinute() { return tokenRefreshThisMinute; }
        public int getTokenRefreshThisHour() { return tokenRefreshThisHour; }
        public boolean isBlocked() { return isBlocked; }
        
        public boolean isOAuthMinuteLimitReached() { return oauthRequestsThisMinute >= OAUTH_REQUESTS_PER_MINUTE; }
        public boolean isOAuthHourLimitReached() { return oauthRequestsThisHour >= OAUTH_REQUESTS_PER_HOUR; }
        public boolean isTokenRefreshMinuteLimitReached() { return tokenRefreshThisMinute >= TOKEN_REFRESH_PER_MINUTE; }
        public boolean isTokenRefreshHourLimitReached() { return tokenRefreshThisHour >= TOKEN_REFRESH_PER_HOUR; }
    }
}