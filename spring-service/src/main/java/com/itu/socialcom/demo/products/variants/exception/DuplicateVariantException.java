package com.itu.socialcom.demo.products.variants.exception;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Exception thrown when attempting to create a variant with option value combinations
 * that already exist for the same product.
 * 
 * Requirements addressed:
 * - 1.3: WHEN a seller creates a variant THEN the system SHALL ensure no duplicate option value combinations exist for the same product
 */
public class DuplicateVariantException extends VariantCreationException {
    
    private static final String ERROR_CODE = "DUPLICATE_VARIANT";
    
    private final List<Long> duplicateOptionValueIds;
    
    /**
     * Creates a new DuplicateVariantException with a default message.
     * 
     * @param duplicateOptionValueIds the list of option value IDs that form the duplicate combination
     */
    public DuplicateVariantException(List<Long> duplicateOptionValueIds) {
        super(ERROR_CODE, "A variant with this option value combination already exists for this product");
        this.duplicateOptionValueIds = duplicateOptionValueIds != null ? 
            List.copyOf(duplicateOptionValueIds) : Collections.emptyList();
    }
    
    /**
     * Creates a new DuplicateVariantException with a custom message.
     * 
     * @param message the custom error message
     * @param duplicateOptionValueIds the list of option value IDs that form the duplicate combination
     */
    public DuplicateVariantException(String message, List<Long> duplicateOptionValueIds) {
        super(ERROR_CODE, message);
        this.duplicateOptionValueIds = duplicateOptionValueIds != null ? 
            List.copyOf(duplicateOptionValueIds) : Collections.emptyList();
    }
    
    /**
     * Creates a new DuplicateVariantException with field-specific errors.
     * 
     * @param message the error message
     * @param fieldErrors map of field names to their specific error messages
     * @param duplicateOptionValueIds the list of option value IDs that form the duplicate combination
     */
    public DuplicateVariantException(String message, Map<String, String> fieldErrors, List<Long> duplicateOptionValueIds) {
        super(ERROR_CODE, message, fieldErrors);
        this.duplicateOptionValueIds = duplicateOptionValueIds != null ? 
            List.copyOf(duplicateOptionValueIds) : Collections.emptyList();
    }
    
    /**
     * Gets the option value IDs that form the duplicate combination.
     * 
     * @return an immutable list of option value IDs
     */
    public List<Long> getDuplicateOptionValueIds() {
        return duplicateOptionValueIds;
    }
    
    /**
     * Creates a DuplicateVariantException for a specific product and option value combination.
     * 
     * @param productId the ID of the product
     * @param optionValueIds the list of option value IDs that form the duplicate combination
     * @return a new DuplicateVariantException instance
     */
    public static DuplicateVariantException forProduct(Long productId, List<Long> optionValueIds) {
        String message = String.format(
            "A variant with option value combination %s already exists for product ID %d", 
            optionValueIds, 
            productId
        );
        return new DuplicateVariantException(message, optionValueIds);
    }
    
    /**
     * Creates a DuplicateVariantException with field-level validation errors.
     * 
     * @param productId the ID of the product
     * @param optionValueIds the list of option value IDs that form the duplicate combination
     * @param fieldErrors map of field names to their specific error messages
     * @return a new DuplicateVariantException instance
     */
    public static DuplicateVariantException forProductWithFieldErrors(Long productId, List<Long> optionValueIds, Map<String, String> fieldErrors) {
        String message = String.format(
            "A variant with option value combination %s already exists for product ID %d", 
            optionValueIds, 
            productId
        );
        return new DuplicateVariantException(message, fieldErrors, optionValueIds);
    }
}