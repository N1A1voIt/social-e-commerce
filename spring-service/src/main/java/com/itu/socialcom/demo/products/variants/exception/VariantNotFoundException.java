package com.itu.socialcom.demo.products.variants.exception;

/**
 * Exception thrown when a requested variant cannot be found.
 * This is used for variant operations that require an existing variant.
 * 
 * This exception extends VariantCreationException to maintain consistency
 * in error handling across all variant operations.
 */
public class VariantNotFoundException extends VariantCreationException {
    
    private static final String ERROR_CODE = "VARIANT_NOT_FOUND";
    
    private final Long variantId;
    
    /**
     * Creates a new VariantNotFoundException with a default message.
     * 
     * @param variantId the ID of the variant that was not found
     */
    public VariantNotFoundException(Long variantId) {
        super(ERROR_CODE, String.format("Variant with ID %d not found", variantId));
        this.variantId = variantId;
    }
    
    /**
     * Creates a new VariantNotFoundException with a custom message.
     * 
     * @param message the custom error message
     * @param variantId the ID of the variant that was not found
     */
    public VariantNotFoundException(String message, Long variantId) {
        super(ERROR_CODE, message);
        this.variantId = variantId;
    }
    
    /**
     * Creates a new VariantNotFoundException with a cause.
     * 
     * @param message the error message
     * @param variantId the ID of the variant that was not found
     * @param cause the underlying cause of this exception
     */
    public VariantNotFoundException(String message, Long variantId, Throwable cause) {
        super(ERROR_CODE, message, cause);
        this.variantId = variantId;
    }
    
    /**
     * Gets the ID of the variant that was not found.
     * 
     * @return the variant ID
     */
    public Long getVariantId() {
        return variantId;
    }
    
    /**
     * Creates a VariantNotFoundException for a specific variant ID.
     * 
     * @param variantId the ID of the variant that was not found
     * @return a new VariantNotFoundException instance
     */
    public static VariantNotFoundException forId(Long variantId) {
        return new VariantNotFoundException(variantId);
    }
    
    /**
     * Creates a VariantNotFoundException for a variant that doesn't belong to a product.
     * 
     * @param variantId the ID of the variant
     * @param productId the ID of the product
     * @return a new VariantNotFoundException instance
     */
    public static VariantNotFoundException forProductMismatch(Long variantId, Long productId) {
        String message = String.format(
            "Variant with ID %d not found for product ID %d", 
            variantId, 
            productId
        );
        return new VariantNotFoundException(message, variantId);
    }
}