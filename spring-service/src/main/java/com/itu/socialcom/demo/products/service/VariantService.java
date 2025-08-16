package com.itu.socialcom.demo.products.service;

import com.itu.socialcom.demo.products.dto.CreateVariantWithOptionsRequest;
import com.itu.socialcom.demo.products.dto.GenerateVariantsRequest;
import com.itu.socialcom.demo.products.dto.UpdateVariantRequest;
import com.itu.socialcom.demo.products.dto.VariantWithOptionsDTO;

import java.util.List;

/**
 * Service interface for variant operations with option value associations
 * Provides comprehensive variant management functionality including creation,
 * automatic generation, updates, deletion, and retrieval with option details
 */
public interface VariantService {
    
    /**
     * Creates a new variant with specific option value combinations
     * 
     * @param productId The ID of the product to create variant for
     * @param request The variant creation request with title, price, and option values
     * @param sellerId The ID of the seller (for ownership validation)
     * @return The created variant with option details
     * @throws IllegalArgumentException if product doesn't exist or seller doesn't own it
     * @throws IllegalStateException if option values are invalid or duplicate combination exists
     */
    VariantWithOptionsDTO createVariantWithOptions(Long productId, CreateVariantWithOptionsRequest request, Long sellerId);
    
    /**
     * Generates all possible variant combinations automatically based on product options
     * 
     * @param productId The ID of the product to generate variants for
     * @param request The generation request with base price and configuration
     * @param sellerId The ID of the seller (for ownership validation)
     * @return List of all created variants with their option combinations
     * @throws IllegalArgumentException if product doesn't exist or seller doesn't own it
     * @throws IllegalStateException if no options exist for the product
     */
    List<VariantWithOptionsDTO> generateAllVariantCombinations(Long productId, GenerateVariantsRequest request, Long sellerId);
    
    /**
     * Updates an existing variant's title and/or price
     * Option value associations remain unchanged
     * 
     * @param productId The ID of the product containing the variant
     * @param variantId The ID of the variant to update
     * @param request The update request with optional title and price
     * @param sellerId The ID of the seller (for ownership validation)
     * @return The updated variant with option details
     * @throws IllegalArgumentException if variant doesn't exist or seller doesn't own it
     */
    VariantWithOptionsDTO updateVariant(Long productId, Long variantId, UpdateVariantRequest request, Long sellerId);
    
    /**
     * Deletes a variant and all its option value associations
     * 
     * @param productId The ID of the product containing the variant
     * @param variantId The ID of the variant to delete
     * @param sellerId The ID of the seller (for ownership validation)
     * @throws IllegalArgumentException if variant doesn't exist or seller doesn't own it
     */
    void deleteVariant(Long productId, Long variantId, Long sellerId);
    
    /**
     * Retrieves all variants for a product with their option details and stock information
     * Results are ordered by creation date (newest first)
     * 
     * @param productId The ID of the product to get variants for
     * @param sellerId The ID of the seller (for ownership validation)
     * @return List of variants with option details, empty list if no variants exist
     * @throws IllegalArgumentException if product doesn't exist or seller doesn't own it
     */
    List<VariantWithOptionsDTO> getProductVariantsWithOptions(Long productId, Long sellerId);
    
    /**
     * Retrieves a specific variant with its option details and stock information
     * 
     * @param productId The ID of the product containing the variant
     * @param variantId The ID of the variant to retrieve
     * @param sellerId The ID of the seller (for ownership validation)
     * @return The variant with option details
     * @throws IllegalArgumentException if variant doesn't exist or seller doesn't own it
     */
    VariantWithOptionsDTO getVariantWithOptions(Long productId, Long variantId, Long sellerId);
}