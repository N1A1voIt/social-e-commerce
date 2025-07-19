package com.itu.socialcom.demo.socialmedia.exception;

/**
 * Exception thrown when API rate limits are exceeded during OAuth operations.
 */
public class RateLimitExceededException extends OAuthException {
    
    private final Long retryAfterSeconds;
    
    public RateLimitExceededException(String platform, String message, Long retryAfterSeconds) {
        super(platform, "RATE_LIMIT_EXCEEDED", message);
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    public RateLimitExceededException(String platform, String message, Long retryAfterSeconds, Throwable cause) {
        super(platform, "RATE_LIMIT_EXCEEDED", message, cause);
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    public Long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}