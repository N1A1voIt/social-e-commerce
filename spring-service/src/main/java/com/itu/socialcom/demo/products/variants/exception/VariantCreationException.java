package com.itu.socialcom.demo.products.variants.exception;

import java.util.Collections;
import java.util.Map;

/**
 * Base exception class for variant creation operations.
 * Provides structured error handling with error codes and field-specific error messages.
 * 
 * This exception extends RuntimeException to provide unchecked exception behavior,
 * allowing for cleaner service layer code without forced exception handling.
 * 
 * Requirements addressed:
 * - 6.3: WHEN database operations fail THEN the system SHALL return appropriate error messages
 * - 6.4: WHEN invalid data is submitted THEN the system SHALL return validation error details with field-specific messages
 */
public class VariantCreationException extends RuntimeException {
    
    private final String errorCode;
    private final Map<String, String> fieldErrors;
    
    /**
     * Creates a new VariantCreationException with an error code and message.
     * 
     * @param errorCode the specific error code for this exception
     * @param message the error message
     */
    public VariantCreationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.fieldErrors = Collections.emptyMap();
    }
    
    /**
     * Creates a new VariantCreationException with an error code, message, and field-specific errors.
     * 
     * @param errorCode the specific error code for this exception
     * @param message the error message
     * @param fieldErrors map of field names to their specific error messages
     */
    public VariantCreationException(String errorCode, String message, Map<String, String> fieldErrors) {
        super(message);
        this.errorCode = errorCode;
        this.fieldErrors = fieldErrors != null ? Map.copyOf(fieldErrors) : Collections.emptyMap();
    }
    
    /**
     * Creates a new VariantCreationException with an error code, message, and cause.
     * 
     * @param errorCode the specific error code for this exception
     * @param message the error message
     * @param cause the underlying cause of this exception
     */
    public VariantCreationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.fieldErrors = Collections.emptyMap();
    }
    
    /**
     * Creates a new VariantCreationException with an error code, message, field errors, and cause.
     * 
     * @param errorCode the specific error code for this exception
     * @param message the error message
     * @param fieldErrors map of field names to their specific error messages
     * @param cause the underlying cause of this exception
     */
    public VariantCreationException(String errorCode, String message, Map<String, String> fieldErrors, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.fieldErrors = fieldErrors != null ? Map.copyOf(fieldErrors) : Collections.emptyMap();
    }
    
    /**
     * Gets the error code for this exception.
     * 
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Gets the field-specific error messages.
     * 
     * @return an immutable map of field names to error messages
     */
    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
    
    /**
     * Checks if this exception has field-specific errors.
     * 
     * @return true if field errors exist, false otherwise
     */
    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }
}