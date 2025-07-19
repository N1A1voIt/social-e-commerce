package com.itu.socialcom.demo.socialmedia.exception;

/**
 * Exception thrown when OAuth tokens have expired and need to be refreshed.
 */
public class TokenExpiredException extends OAuthException {
    
    public TokenExpiredException(String platform, String message) {
        super(platform, "TOKEN_EXPIRED", message);
    }
    
    public TokenExpiredException(String platform, String errorCode, String message) {
        super(platform, errorCode, message);
    }
    
    public TokenExpiredException(String platform, String message, Throwable cause) {
        super(platform, "TOKEN_EXPIRED", message, cause);
    }
    
    public TokenExpiredException(String platform, String errorCode, String message, Throwable cause) {
        super(platform, errorCode, message, cause);
    }
}