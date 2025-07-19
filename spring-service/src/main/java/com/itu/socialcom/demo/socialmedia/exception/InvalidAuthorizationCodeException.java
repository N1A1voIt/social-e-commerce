package com.itu.socialcom.demo.socialmedia.exception;

/**
 * Exception thrown when an invalid authorization code is provided during OAuth flow.
 */
public class InvalidAuthorizationCodeException extends OAuthException {
    
    public InvalidAuthorizationCodeException(String platform, String message) {
        super(platform, "INVALID_AUTH_CODE", message);
    }
    
    public InvalidAuthorizationCodeException(String platform, String errorCode, String message) {
        super(platform, errorCode, message);
    }
    
    public InvalidAuthorizationCodeException(String platform, String message, Throwable cause) {
        super(platform, "INVALID_AUTH_CODE", message, cause);
    }
    
    public InvalidAuthorizationCodeException(String platform, String errorCode, String message, Throwable cause) {
        super(platform, errorCode, message, cause);
    }
}