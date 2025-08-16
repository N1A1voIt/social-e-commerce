package com.itu.socialcom.demo.products.validation;

import com.itu.socialcom.demo.products.model.Product;
import com.itu.socialcom.demo.products.repository.ProductRepository;
import com.itu.socialcom.demo.products.variants.model.Variant;
import com.itu.socialcom.demo.products.variants.repository.VariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Validator for seller ownership verification in variant operations.
 * Ensures that sellers can only perform operations on products and variants they own.
 * 
 * Requirements addressed:
 * - 4.4: Validate seller ownership before variant operations
 * - 5.3: Reject requests from sellers who don't own the variant
 * - 6.1: Validate seller ownership of the parent product
 */
@Component
public class SellerOwnershipValidator {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private VariantRepository variantRepository;
    
    /**
     * Validates that the seller owns the specified product.
     * 
     * @param productId the ID of the product
     * @param sellerId the ID of the seller
     * @throws UnauthorizedVariantAccessException if the seller doesn't own the product
     * 
     * Requirements:
     * - 6.1: WHEN any variant operation is performed THEN the system SHALL validate seller ownership of the parent product
     */
    public void validateProductOwnership(Long productId, Integer sellerId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller ID cannot be null");
        }
        
        boolean exists = productRepository.existsByIdProductAndIdSeller(productId, sellerId);
        if (!exists) {
            throw new UnauthorizedVariantAccessException(
                "Seller with ID " + sellerId + " does not own product with ID " + productId
            );
        }
    }
    
    /**
     * Validates that the seller owns the specified product and returns the product if found.
     * 
     * @param productId the ID of the product
     * @param sellerId the ID of the seller
     * @return the Product entity if owned by the seller
     * @throws UnauthorizedVariantAccessException if the seller doesn't own the product
     * 
     * Requirements:
     * - 6.1: WHEN any variant operation is performed THEN the system SHALL validate seller ownership of the parent product
     */
    public Product validateAndGetProduct(Long productId, Integer sellerId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller ID cannot be null");
        }
        
        Optional<Product> productOpt = productRepository.findByIdProductAndIdSeller(productId, sellerId);
        if (productOpt.isEmpty()) {
            throw new UnauthorizedVariantAccessException(
                "Seller with ID " + sellerId + " does not own product with ID " + productId
            );
        }
        
        return productOpt.get();
    }
    
    /**
     * Validates that the seller owns the variant through product ownership.
     * 
     * @param variantId the ID of the variant
     * @param sellerId the ID of the seller
     * @throws UnauthorizedVariantAccessException if the seller doesn't own the variant
     * @throws VariantNotFoundException if the variant doesn't exist
     * 
     * Requirements:
     * - 4.4: IF a seller tries to update a variant they don't own THEN the system SHALL reject the request
     * - 5.3: IF a seller tries to delete a variant they don't own THEN the system SHALL reject the request
     */
    public void validateVariantOwnership(Long variantId, Integer sellerId) {
        if (variantId == null) {
            throw new IllegalArgumentException("Variant ID cannot be null");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller ID cannot be null");
        }
        
        // First, get the variant to find its product
        Optional<Variant> variantOpt = variantRepository.findById(variantId);
        if (variantOpt.isEmpty()) {
            throw new VariantNotFoundException("Variant with ID " + variantId + " not found");
        }
        
        Variant variant = variantOpt.get();
        Long productId = variant.getIdProduct();
        
        // Then validate product ownership
        validateProductOwnership(productId, sellerId);
    }
    
    /**
     * Validates that the seller owns the variant and returns both the variant and product.
     * 
     * @param variantId the ID of the variant
     * @param sellerId the ID of the seller
     * @return VariantOwnershipResult containing the variant and product
     * @throws UnauthorizedVariantAccessException if the seller doesn't own the variant
     * @throws VariantNotFoundException if the variant doesn't exist
     * 
     * Requirements:
     * - 4.4: IF a seller tries to update a variant they don't own THEN the system SHALL reject the request
     * - 5.3: IF a seller tries to delete a variant they don't own THEN the system SHALL reject the request
     */
    public VariantOwnershipResult validateAndGetVariantWithProduct(Long variantId, Integer sellerId) {
        if (variantId == null) {
            throw new IllegalArgumentException("Variant ID cannot be null");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller ID cannot be null");
        }
        
        // First, get the variant to find its product
        Optional<Variant> variantOpt = variantRepository.findById(variantId);
        if (variantOpt.isEmpty()) {
            throw new VariantNotFoundException("Variant with ID " + variantId + " not found");
        }
        
        Variant variant = variantOpt.get();
        Long productId = variant.getIdProduct();
        
        // Then validate product ownership and get the product
        Product product = validateAndGetProduct(productId, sellerId);
        
        return new VariantOwnershipResult(variant, product);
    }
    
    /**
     * Validates that the seller owns both the product and the variant.
     * This is useful when both product ID and variant ID are provided in the request.
     * 
     * @param productId the ID of the product
     * @param variantId the ID of the variant
     * @param sellerId the ID of the seller
     * @throws UnauthorizedVariantAccessException if the seller doesn't own the product or variant
     * @throws VariantNotFoundException if the variant doesn't exist
     * @throws IllegalArgumentException if the variant doesn't belong to the specified product
     * 
     * Requirements:
     * - 4.4: IF a seller tries to update a variant they don't own THEN the system SHALL reject the request
     * - 5.3: IF a seller tries to delete a variant they don't own THEN the system SHALL reject the request
     * - 6.1: WHEN any variant operation is performed THEN the system SHALL validate seller ownership of the parent product
     */
    public void validateProductAndVariantOwnership(Long productId, Long variantId, Integer sellerId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (variantId == null) {
            throw new IllegalArgumentException("Variant ID cannot be null");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller ID cannot be null");
        }
        
        // First validate product ownership
        validateProductOwnership(productId, sellerId);
        
        // Then get the variant and verify it belongs to the product
        Optional<Variant> variantOpt = variantRepository.findById(variantId);
        if (variantOpt.isEmpty()) {
            throw new VariantNotFoundException("Variant with ID " + variantId + " not found");
        }
        
        Variant variant = variantOpt.get();
        if (!variant.getIdProduct().equals(productId)) {
            throw new IllegalArgumentException(
                "Variant with ID " + variantId + " does not belong to product with ID " + productId
            );
        }
    }
    
    /**
     * Validates that the seller owns both the product and variant, and returns both entities.
     * 
     * @param productId the ID of the product
     * @param variantId the ID of the variant
     * @param sellerId the ID of the seller
     * @return VariantOwnershipResult containing the variant and product
     * @throws UnauthorizedVariantAccessException if the seller doesn't own the product or variant
     * @throws VariantNotFoundException if the variant doesn't exist
     * @throws IllegalArgumentException if the variant doesn't belong to the specified product
     */
    public VariantOwnershipResult validateAndGetProductAndVariant(Long productId, Long variantId, Integer sellerId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (variantId == null) {
            throw new IllegalArgumentException("Variant ID cannot be null");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller ID cannot be null");
        }
        
        // First validate product ownership and get the product
        Product product = validateAndGetProduct(productId, sellerId);
        
        // Then get the variant and verify it belongs to the product
        Optional<Variant> variantOpt = variantRepository.findById(variantId);
        if (variantOpt.isEmpty()) {
            throw new VariantNotFoundException("Variant with ID " + variantId + " not found");
        }
        
        Variant variant = variantOpt.get();
        if (!variant.getIdProduct().equals(productId)) {
            throw new IllegalArgumentException(
                "Variant with ID " + variantId + " does not belong to product with ID " + productId
            );
        }
        
        return new VariantOwnershipResult(variant, product);
    }
    
    /**
     * Result class containing both variant and product entities after ownership validation.
     */
    public static class VariantOwnershipResult {
        private final Variant variant;
        private final Product product;
        
        public VariantOwnershipResult(Variant variant, Product product) {
            this.variant = variant;
            this.product = product;
        }
        
        public Variant getVariant() {
            return variant;
        }
        
        public Product getProduct() {
            return product;
        }
    }
    
    /**
     * Exception thrown when a seller tries to access a variant they don't own.
     */
    public static class UnauthorizedVariantAccessException extends RuntimeException {
        public UnauthorizedVariantAccessException(String message) {
            super(message);
        }
        
        public UnauthorizedVariantAccessException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Exception thrown when a variant is not found.
     */
    public static class VariantNotFoundException extends RuntimeException {
        public VariantNotFoundException(String message) {
            super(message);
        }
        
        public VariantNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}