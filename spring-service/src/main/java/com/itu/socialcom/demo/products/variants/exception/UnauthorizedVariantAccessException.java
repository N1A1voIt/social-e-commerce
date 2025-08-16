package com.itu.socialcom.demo.products.variants.exception;

/**
 * Exception thrown when a seller attempts to perform operations on variants
 * or products they don't own.
 * 
 * Requirements addressed:
 * - 4.4: IF a seller tries to update a variant they don't own THEN the system SHALL reject the request
 * - 5.3: IF a seller tries to delete a variant they don't own THEN the system SHALL reject the request
 * - 6.1: WHEN any variant operation is performed THEN the system SHALL validate seller ownership of the parent product
 */
public class UnauthorizedVariantAccessException extends VariantCreationException {
    
    private static final String ERROR_CODE = "UNAUTHORIZED_VARIANT_ACCESS";
    
    private final Integer sellerId;
    private final Long resourceId;
    private final String resourceType;
    
    /**
     * Creates a new UnauthorizedVariantAccessException with a default message.
     * 
     * @param sellerId the ID of the seller attempting the operation
     * @param resourceId the ID of the resource (product or variant)
     * @param resourceType the type of resource ("product" or "variant")
     */
    public UnauthorizedVariantAccessException(Integer sellerId, Long resourceId, String resourceType) {
        super(ERROR_CODE, String.format(
            "Seller with ID %d is not authorized to access %s with ID %d", 
            sellerId, 
            resourceType, 
            resourceId
        ));
        this.sellerId = sellerId;
        this.resourceId = resourceId;
        this.resourceType = resourceType;
    }
    
    /**
     * Creates a new UnauthorizedVariantAccessException with a custom message.
     * 
     * @param message the custom error message
     * @param sellerId the ID of the seller attempting the operation
     * @param resourceId the ID of the resource (product or variant)
     * @param resourceType the type of resource ("product" or "variant")
     */
    public UnauthorizedVariantAccessException(String message, Integer sellerId, Long resourceId, String resourceType) {
        super(ERROR_CODE, message);
        this.sellerId = sellerId;
        this.resourceId = resourceId;
        this.resourceType = resourceType;
    }
    
    /**
     * Creates a new UnauthorizedVariantAccessException with a cause.
     * 
     * @param message the error message
     * @param sellerId the ID of the seller attempting the operation
     * @param resourceId the ID of the resource (product or variant)
     * @param resourceType the type of resource ("product" or "variant")
     * @param cause the underlying cause of this exception
     */
    public UnauthorizedVariantAccessException(String message, Integer sellerId, Long resourceId, String resourceType, Throwable cause) {
        super(ERROR_CODE, message, cause);
        this.sellerId = sellerId;
        this.resourceId = resourceId;
        this.resourceType = resourceType;
    }
    
    /**
     * Gets the ID of the seller attempting the operation.
     * 
     * @return the seller ID
     */
    public Integer getSellerId() {
        return sellerId;
    }
    
    /**
     * Gets the ID of the resource being accessed.
     * 
     * @return the resource ID
     */
    public Long getResourceId() {
        return resourceId;
    }
    
    /**
     * Gets the type of resource being accessed.
     * 
     * @return the resource type
     */
    public String getResourceType() {
        return resourceType;
    }
    
    /**
     * Creates an UnauthorizedVariantAccessException for product access.
     * 
     * @param sellerId the ID of the seller attempting the operation
     * @param productId the ID of the product
     * @return a new UnauthorizedVariantAccessException instance
     */
    public static UnauthorizedVariantAccessException forProduct(Integer sellerId, Long productId) {
        return new UnauthorizedVariantAccessException(sellerId, productId, "product");
    }
    
    /**
     * Creates an UnauthorizedVariantAccessException for variant access.
     * 
     * @param sellerId the ID of the seller attempting the operation
     * @param variantId the ID of the variant
     * @return a new UnauthorizedVariantAccessException instance
     */
    public static UnauthorizedVariantAccessException forVariant(Integer sellerId, Long variantId) {
        return new UnauthorizedVariantAccessException(sellerId, variantId, "variant");
    }
    
    /**
     * Creates an UnauthorizedVariantAccessException for product ownership validation.
     * 
     * @param sellerId the ID of the seller attempting the operation
     * @param productId the ID of the product
     * @param operation the operation being attempted
     * @return a new UnauthorizedVariantAccessException instance
     */
    public static UnauthorizedVariantAccessException forProductOperation(Integer sellerId, Long productId, String operation) {
        String message = String.format(
            "Seller with ID %d is not authorized to %s product with ID %d", 
            sellerId, 
            operation, 
            productId
        );
        return new UnauthorizedVariantAccessException(message, sellerId, productId, "product");
    }
    
    /**
     * Creates an UnauthorizedVariantAccessException for variant ownership validation.
     * 
     * @param sellerId the ID of the seller attempting the operation
     * @param variantId the ID of the variant
     * @param operation the operation being attempted
     * @return a new UnauthorizedVariantAccessException instance
     */
    public static UnauthorizedVariantAccessException forVariantOperation(Integer sellerId, Long variantId, String operation) {
        String message = String.format(
            "Seller with ID %d is not authorized to %s variant with ID %d", 
            sellerId, 
            operation, 
            variantId
        );
        return new UnauthorizedVariantAccessException(message, sellerId, variantId, "variant");
    }
}