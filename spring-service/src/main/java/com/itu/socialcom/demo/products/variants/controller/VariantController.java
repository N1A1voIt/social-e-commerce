package com.itu.socialcom.demo.products.variants.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.products.dto.*;
import com.itu.socialcom.demo.products.service.VariantService;
import com.itu.socialcom.demo.products.variants.model.Variant;
import com.itu.socialcom.demo.products.variants.model.VariantInStock;
import com.itu.socialcom.demo.products.variants.repository.VariantInStockRepository;
import com.itu.socialcom.demo.products.variants.repository.VariantRepository;
import com.itu.socialcom.demo.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/variants")
public class VariantController {
    @Autowired
    VariantRepository variantRepository;
    @Autowired
    VariantInStockRepository variantInStockRepository;
    @Autowired
    TokenV2ServiceImpl tokenV2Service;
    @Autowired
    VariantService variantService;

    @GetMapping("/{idProduct}")
    public ResponseEntity<ApiResponse> getVariantsByProductId(@PathVariable Long idProduct, @RequestHeader(name = "Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) throw new Exception("Not logged in");
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            List<VariantInStock> variants = variantInStockRepository.findVariantInStockByIdProduct(idProduct);
            apiResponse.setData(variants);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(500).body(apiResponse);

        }
    }
    @PostMapping()
    public ResponseEntity<ApiResponse> createVariant(@RequestBody Variant variant, @RequestHeader(name = "Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) throw new Exception("Not logged in");
            Variant savedVariant = variantRepository.save(variant);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(201);
            apiResponse.setData(savedVariant);
            return ResponseEntity.status(201).body(apiResponse);
        } catch (Exception e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(500).body(apiResponse); // Handle exceptions appropriately
        }
    }

    /**
     * Creates a new variant with specific option value combinations
     * POST /api/variants/products/{productId}/with-options
     */
    @PostMapping("/products/{productId}/with-options")
    public ResponseEntity<ApiResponse> createVariantWithOptions(
            @PathVariable Long productId,
            @RequestBody @Valid CreateVariantWithOptionsRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Extract seller ID from token
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                throw new IllegalArgumentException("Not logged in");
            }
            
            // Create variant with options using service
            VariantWithOptionsDTO createdVariant = variantService.createVariantWithOptions(
                productId, request, seller.getId());
            
            // Return success response
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(201);
            apiResponse.setData(createdVariant);
            return ResponseEntity.status(201).body(apiResponse);
            
        } catch (IllegalArgumentException e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(400);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e.getMessage()));
            return ResponseEntity.status(400).body(apiResponse);
            
        } catch (IllegalStateException e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(422);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e.getMessage()));
            return ResponseEntity.status(422).body(apiResponse);
            
        } catch (Exception e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e.getMessage()));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    /**
     * Generates all possible variant combinations automatically based on product options
     * POST /api/variants/products/{productId}/generate-all
     */
    @PostMapping("/products/{productId}/generate-all")
    public ResponseEntity<ApiResponse> generateAllVariants(
            @PathVariable Long productId,
            @RequestBody @Valid GenerateVariantsRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Extract seller ID from token
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                throw new IllegalArgumentException("Not logged in");
            }
            
            // Generate all variant combinations using service
            List<VariantWithOptionsDTO> createdVariants = variantService.generateAllVariantCombinations(
                productId, request, seller.getIdSeller().longValue());
            
            // Return success response with all created variants
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(201);
            apiResponse.setData(createdVariants);
            return ResponseEntity.status(201).body(apiResponse);
            
        } catch (IllegalArgumentException e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(400);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e.getMessage()));
            return ResponseEntity.status(400).body(apiResponse);
            
        } catch (IllegalStateException e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(422);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e.getMessage()));
            return ResponseEntity.status(422).body(apiResponse);
            
        } catch (Exception e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e.getMessage()));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    /**
     * Retrieves all variants for a product with their associated option details
     * GET /api/variants/products/{productId}/with-options
     */
    @GetMapping("/products/{productId}/with-options")
    public ResponseEntity<ApiResponse> getVariantsWithOptions(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String token) {
        try {
            // Extract seller ID from token
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                throw new IllegalArgumentException("Not logged in");
            }
            
            // Get variants with options using service
            List<VariantWithOptionsDTO> variants = variantService.getProductVariantsWithOptions(
                productId, seller.getIdSeller().longValue());
            
            // Return success response (handles empty results gracefully)
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(variants);
            return ResponseEntity.ok(apiResponse);
            
        } catch (IllegalArgumentException e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(400);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e.getMessage()));
            return ResponseEntity.status(400).body(apiResponse);
            
        } catch (Exception e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e.getMessage()));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    /**
     * Updates an existing variant's title and/or price
     * PUT /api/variants/products/{productId}/variants/{variantId}
     */
    @PutMapping("/products/{productId}/variants/{variantId}")
    public ResponseEntity<ApiResponse> updateVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @RequestBody @Valid UpdateVariantRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Extract seller ID from token
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                throw new IllegalArgumentException("Not logged in");
            }
            
            // Update variant using service (supports partial updates for title and price only)
            VariantWithOptionsDTO updatedVariant = variantService.updateVariant(
                productId, variantId, request, seller.getIdSeller().longValue());
            
            // Return updated variant data
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(updatedVariant);
            return ResponseEntity.ok(apiResponse);
            
        } catch (IllegalArgumentException e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(400);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e.getMessage()));
            return ResponseEntity.status(400).body(apiResponse);
            
        } catch (Exception e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e.getMessage()));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    /**
     * Deletes a variant and all its option value associations
     * DELETE /api/variants/products/{productId}/variants/{variantId}
     */
    @DeleteMapping("/products/{productId}/variants/{variantId}")
    public ResponseEntity<ApiResponse> deleteVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @RequestHeader("Authorization") String token) {
        try {
            // Extract seller ID from token
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                throw new IllegalArgumentException("Not logged in");
            }
            
            // Delete variant using service (handles cascade deletion of option value associations)
            variantService.deleteVariant(productId, variantId, seller.getIdSeller().longValue());
            
            // Return success confirmation
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData("Variant deleted successfully");
            return ResponseEntity.ok(apiResponse);
            
        } catch (IllegalArgumentException e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(400);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e.getMessage()));
            return ResponseEntity.status(400).body(apiResponse);
            
        } catch (Exception e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e.getMessage()));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }
}
