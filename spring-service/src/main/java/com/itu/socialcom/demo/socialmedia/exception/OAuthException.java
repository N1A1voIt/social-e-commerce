package com.itu.socialcom.demo.socialmedia.exception;

/**
 * Base exception for OAuth operations across all social media platforms.
 * Provides common error handling structure for OAuth-related failures.
 */
public class OAuthException extends RuntimeException {
    
    private final String platform;
    private final String errorCode;
    
    public OAuthException(String platform, String errorCode, String message) {
        super(message);
        this.platform = platform;
        this.errorCode = errorCode;
    }
    
    public OAuthException(String platform, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.platform = platform;
        this.errorCode = errorCode;
    }
    
    public String getPlatform() {
        return platform;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    @Override
    public String toString() {
        return "OAuthException{" +
                "platform='" + platform + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}