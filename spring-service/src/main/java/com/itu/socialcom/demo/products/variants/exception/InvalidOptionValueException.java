package com.itu.socialcom.demo.products.variants.exception;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Exception thrown when invalid option values are provided for variant creation.
 * This includes cases where option values don't exist, don't belong to the product,
 * or don't satisfy the product's option requirements.
 * 
 * Requirements addressed:
 * - 1.4: IF a seller tries to create a variant with invalid option values THEN the system SHALL reject the request with validation errors
 */
public class InvalidOptionValueException extends VariantCreationException {
    
    private static final String ERROR_CODE = "INVALID_OPTION_VALUES";
    
    private final List<Long> invalidOptionValueIds;
    
    /**
     * Creates a new InvalidOptionValueException with field-specific errors.
     * 
     * @param fieldErrors map of field names to their specific error messages
     */
    public InvalidOptionValueException(Map<String, String> fieldErrors) {
        super(ERROR_CODE, "Invalid option values provided", fieldErrors);
        this.invalidOptionValueIds = Collections.emptyList();
    }
    
    /**
     * Creates a new InvalidOptionValueException with a custom message and field errors.
     * 
     * @param message the error message
     * @param fieldErrors map of field names to their specific error messages
     */
    public InvalidOptionValueException(String message, Map<String, String> fieldErrors) {
        super(ERROR_CODE, message, fieldErrors);
        this.invalidOptionValueIds = Collections.emptyList();
    }
    
    /**
     * Creates a new InvalidOptionValueException with invalid option value IDs.
     * 
     * @param message the error message
     * @param invalidOptionValueIds the list of invalid option value IDs
     */
    public InvalidOptionValueException(String message, List<Long> invalidOptionValueIds) {
        super(ERROR_CODE, message);
        this.invalidOptionValueIds = invalidOptionValueIds != null ? 
            List.copyOf(invalidOptionValueIds) : Collections.emptyList();
    }
    
    /**
     * Creates a new InvalidOptionValueException with field errors and invalid option value IDs.
     * 
     * @param message the error message
     * @param fieldErrors map of field names to their specific error messages
     * @param invalidOptionValueIds the list of invalid option value IDs
     */
    public InvalidOptionValueException(String message, Map<String, String> fieldErrors, List<Long> invalidOptionValueIds) {
        super(ERROR_CODE, message, fieldErrors);
        this.invalidOptionValueIds = invalidOptionValueIds != null ? 
            List.copyOf(invalidOptionValueIds) : Collections.emptyList();
    }
    
    /**
     * Gets the invalid option value IDs.
     * 
     * @return an immutable list of invalid option value IDs
     */
    public List<Long> getInvalidOptionValueIds() {
        return invalidOptionValueIds;
    }
    
    /**
     * Creates an InvalidOptionValueException for non-existent option values.
     * 
     * @param nonExistentIds the list of option value IDs that don't exist
     * @return a new InvalidOptionValueException instance
     */
    public static InvalidOptionValueException forNonExistentValues(List<Long> nonExistentIds) {
        String message = String.format("Option values with IDs %s do not exist", nonExistentIds);
        Map<String, String> fieldErrors = Map.of(
            "optionValueIds", "One or more option values do not exist"
        );
        return new InvalidOptionValueException(message, fieldErrors, nonExistentIds);
    }
    
    /**
     * Creates an InvalidOptionValueException for option values that don't belong to the product.
     * 
     * @param productId the ID of the product
     * @param invalidIds the list of option value IDs that don't belong to the product
     * @return a new InvalidOptionValueException instance
     */
    public static InvalidOptionValueException forProductMismatch(Long productId, List<Long> invalidIds) {
        String message = String.format(
            "Option values with IDs %s do not belong to product ID %d", 
            invalidIds, 
            productId
        );
        Map<String, String> fieldErrors = Map.of(
            "optionValueIds", "One or more option values do not belong to this product"
        );
        return new InvalidOptionValueException(message, fieldErrors, invalidIds);
    }
    
    /**
     * Creates an InvalidOptionValueException for missing option selections.
     * 
     * @param missingOptionLabels the list of option labels that are missing selections
     * @return a new InvalidOptionValueException instance
     */
    public static InvalidOptionValueException forMissingOptions(List<String> missingOptionLabels) {
        String message = String.format(
            "Missing option value selections for options: %s", 
            String.join(", ", missingOptionLabels)
        );
        Map<String, String> fieldErrors = Map.of(
            "optionValueIds", "Must select exactly one value for each product option"
        );
        return new InvalidOptionValueException(message, fieldErrors);
    }
    
    /**
     * Creates an InvalidOptionValueException for multiple selections from the same option.
     * 
     * @param duplicateOptionLabels the list of option labels that have multiple selections
     * @return a new InvalidOptionValueException instance
     */
    public static InvalidOptionValueException forDuplicateOptionSelections(List<String> duplicateOptionLabels) {
        String message = String.format(
            "Multiple values selected for options: %s", 
            String.join(", ", duplicateOptionLabels)
        );
        Map<String, String> fieldErrors = Map.of(
            "optionValueIds", "Cannot select multiple values from the same option"
        );
        return new InvalidOptionValueException(message, fieldErrors);
    }
}