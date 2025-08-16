package com.itu.socialcom.demo.products.variants.exception;

import com.itu.socialcom.demo.utils.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for variant-related operations.
 * Provides structured error responses with appropriate HTTP status codes
 * and field-level validation details.
 * 
 * Requirements addressed:
 * - 6.3: WHEN database operations fail THEN the system SHALL return appropriate error messages
 * - 6.4: WHEN invalid data is submitted THEN the system SHALL return validation error details with field-specific messages
 */
@ControllerAdvice
public class GlobalVariantExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalVariantExceptionHandler.class);
    
    /**
     * Handles DuplicateVariantException with HTTP 409 Conflict status.
     * 
     * @param ex the DuplicateVariantException
     * @param request the web request
     * @return ResponseEntity with structured error response
     */
    @ExceptionHandler(DuplicateVariantException.class)
    public ResponseEntity<ApiResponse> handleDuplicateVariantException(
            DuplicateVariantException ex, WebRequest request) {
        
        logger.warn("Duplicate variant creation attempt: {}", ex.getMessage());
        
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatus(HttpStatus.CONFLICT.value());
        apiResponse.setData(createErrorData(ex, request));
        apiResponse.setErrors(List.of(ex));
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiResponse);
    }
    
    /**
     * Handles InvalidOptionValueException with HTTP 400 Bad Request status.
     * 
     * @param ex the InvalidOptionValueException
     * @param request the web request
     * @return ResponseEntity with structured error response
     */
    @ExceptionHandler(InvalidOptionValueException.class)
    public ResponseEntity<ApiResponse> handleInvalidOptionValueException(
            InvalidOptionValueException ex, WebRequest request) {
        
        logger.warn("Invalid option values provided: {}", ex.getMessage());
        
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        apiResponse.setData(createErrorData(ex, request));
        apiResponse.setErrors(List.of(ex));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }
    
    /**
     * Handles VariantNotFoundException with HTTP 404 Not Found status.
     * 
     * @param ex the VariantNotFoundException
     * @param request the web request
     * @return ResponseEntity with structured error response
     */
    @ExceptionHandler(VariantNotFoundException.class)
    public ResponseEntity<ApiResponse> handleVariantNotFoundException(
            VariantNotFoundException ex, WebRequest request) {
        
        logger.warn("Variant not found: {}", ex.getMessage());
        
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatus(HttpStatus.NOT_FOUND.value());
        apiResponse.setData(createErrorData(ex, request));
        apiResponse.setErrors(List.of(ex));
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
    }
    
    /**
     * Handles UnauthorizedVariantAccessException with HTTP 403 Forbidden status.
     * 
     * @param ex the UnauthorizedVariantAccessException
     * @param request the web request
     * @return ResponseEntity with structured error response
     */
    @ExceptionHandler(UnauthorizedVariantAccessException.class)
    public ResponseEntity<ApiResponse> handleUnauthorizedVariantAccessException(
            UnauthorizedVariantAccessException ex, WebRequest request) {
        
        logger.warn("Unauthorized variant access attempt: {}", ex.getMessage());
        
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatus(HttpStatus.FORBIDDEN.value());
        apiResponse.setData(createErrorData(ex, request));
        apiResponse.setErrors(List.of(ex));
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);
    }
    
    /**
     * Handles general VariantCreationException with HTTP 422 Unprocessable Entity status.
     * 
     * @param ex the VariantCreationException
     * @param request the web request
     * @return ResponseEntity with structured error response
     */
    @ExceptionHandler(VariantCreationException.class)
    public ResponseEntity<ApiResponse> handleVariantCreationException(
            VariantCreationException ex, WebRequest request) {
        
        logger.error("Variant creation error: {}", ex.getMessage(), ex);
        
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
        apiResponse.setData(createErrorData(ex, request));
        apiResponse.setErrors(List.of(ex));
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(apiResponse);
    }
    
    /**
     * Handles validation errors from @Valid annotations with HTTP 400 Bad Request status.
     * 
     * @param ex the MethodArgumentNotValidException
     * @param request the web request
     * @return ResponseEntity with structured error response including field errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        logger.warn("Validation error: {}", ex.getMessage());
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
            } else {
                fieldErrors.put("global", error.getDefaultMessage());
            }
        });
        
        Map<String, Object> errorData = Map.of(
            "error", "VALIDATION_ERROR",
            "message", "Validation failed for one or more fields",
            "timestamp", LocalDateTime.now().toString(),
            "path", request.getDescription(false).replace("uri=", ""),
            "fieldErrors", fieldErrors
        );
        
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        apiResponse.setData(errorData);
        apiResponse.setErrors(List.of(ex));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }
    
    /**
     * Handles bind exceptions (form validation errors) with HTTP 400 Bad Request status.
     * 
     * @param ex the BindException
     * @param request the web request
     * @return ResponseEntity with structured error response including field errors
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse> handleBindException(
            BindException ex, WebRequest request) {
        
        logger.warn("Bind error: {}", ex.getMessage());
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
            } else {
                fieldErrors.put("global", error.getDefaultMessage());
            }
        });
        
        Map<String, Object> errorData = Map.of(
            "error", "BIND_ERROR",
            "message", "Data binding failed for one or more fields",
            "timestamp", LocalDateTime.now().toString(),
            "path", request.getDescription(false).replace("uri=", ""),
            "fieldErrors", fieldErrors
        );
        
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        apiResponse.setData(errorData);
        apiResponse.setErrors(List.of(ex));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }
    
    /**
     * Creates structured error data for VariantCreationException and its subclasses.
     * 
     * @param ex the VariantCreationException
     * @param request the web request
     * @return Map containing structured error information
     */
    private Map<String, Object> createErrorData(VariantCreationException ex, WebRequest request) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("error", ex.getErrorCode());
        errorData.put("message", ex.getMessage());
        errorData.put("timestamp", LocalDateTime.now().toString());
        errorData.put("path", request.getDescription(false).replace("uri=", ""));
        
        if (ex.hasFieldErrors()) {
            errorData.put("fieldErrors", ex.getFieldErrors());
        }
        
        // Add specific data for different exception types
        if (ex instanceof DuplicateVariantException) {
            DuplicateVariantException duplicateEx = (DuplicateVariantException) ex;
            if (!duplicateEx.getDuplicateOptionValueIds().isEmpty()) {
                errorData.put("duplicateOptionValueIds", duplicateEx.getDuplicateOptionValueIds());
            }
        } else if (ex instanceof InvalidOptionValueException) {
            InvalidOptionValueException invalidEx = (InvalidOptionValueException) ex;
            if (!invalidEx.getInvalidOptionValueIds().isEmpty()) {
                errorData.put("invalidOptionValueIds", invalidEx.getInvalidOptionValueIds());
            }
        } else if (ex instanceof VariantNotFoundException) {
            VariantNotFoundException notFoundEx = (VariantNotFoundException) ex;
            if (notFoundEx.getVariantId() != null) {
                errorData.put("variantId", notFoundEx.getVariantId());
            }
        } else if (ex instanceof UnauthorizedVariantAccessException) {
            UnauthorizedVariantAccessException unauthorizedEx = (UnauthorizedVariantAccessException) ex;
            errorData.put("sellerId", unauthorizedEx.getSellerId());
            errorData.put("resourceId", unauthorizedEx.getResourceId());
            errorData.put("resourceType", unauthorizedEx.getResourceType());
        }
        
        return errorData;
    }
}