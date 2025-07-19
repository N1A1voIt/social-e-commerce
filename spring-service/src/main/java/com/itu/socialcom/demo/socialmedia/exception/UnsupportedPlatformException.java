package com.itu.socialcom.demo.socialmedia.exception;

/**
 * Exception thrown when attempting to use an unsupported social media platform.
 */
public class UnsupportedPlatformException extends RuntimeException {
    
    public UnsupportedPlatformException(String message) {
        super(message);
    }
    
    public UnsupportedPlatformException(String message, Throwable cause) {
        super(message, cause);
    }
}