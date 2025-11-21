package com.itu.socialcom.demo.products.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.products.dto.UpdateProductRequest;
import com.itu.socialcom.demo.products.model.Option;
import com.itu.socialcom.demo.products.model.OptionValue;
import com.itu.socialcom.demo.products.model.Product;
import com.itu.socialcom.demo.products.model.ProductCPL;
import com.itu.socialcom.demo.products.repository.OptionRepository;
import com.itu.socialcom.demo.products.repository.OptionValueRepository;
import com.itu.socialcom.demo.products.repository.ProductCplRepository;
import com.itu.socialcom.demo.products.repository.ProductRepository;
import com.itu.socialcom.demo.products.service.OptionService;
import com.itu.socialcom.demo.products.variants.model.Variant;
import com.itu.socialcom.demo.products.variants.repository.VariantRepository;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class ProductController {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductCplRepository productCplRepository;
    @Autowired
    private TokenV2ServiceImpl sellerRepository;
    @Autowired
    private OptionRepository optionRepository;
    @Autowired
    private OptionValueRepository optionValueRepository;
    @Autowired
    private OptionService optionService;
    @Autowired
    private VariantRepository variantRepository;

    @GetMapping("/api/products")
    public ResponseEntity<List<Product>> products(@RequestHeader("Authorization") String token, Pageable pageable) {
        try {
            Seller seller = sellerRepository.findSellerByToken(token).orElse(null);
            if (seller == null) throw new Exception("Not logged in");
            return ResponseEntity.ok(productRepository.findByIdSeller(seller.getId().intValue(),pageable).getContent());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("/api/products/cpl")
    public ResponseEntity<List<ProductCPL>> productsCpl(@RequestHeader("Authorization") String token) {
        try {
            Seller seller = sellerRepository.findSellerByToken(token).orElse(null);
            if (seller == null) throw new Exception("Not logged in");
            return ResponseEntity.ok(productCplRepository.findByIdSeller(seller.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    @GetMapping("/api/products/variant/{SKU}")
    public ResponseEntity<ApiResponse> fetchProductByVariantSKU(@RequestHeader("Authorization") String token, @PathVariable String SKU) {
        try {
            Seller seller = sellerRepository.findSellerByToken(token).orElse(null);
            if (seller == null) throw new Exception("Not logged in");
            List<Variant> product = variantRepository.findByIdSellerAndSkuIn(seller.getId(), List.of(SKU));
            ApiResponse response = new ApiResponse();
            if (product == null) {
                response.setStatus(404);
                return ResponseEntity.status(404).body(response);
            }
            response.setStatus(200);
            response.setData(product);
            response.setErrors(null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse response = new ApiResponse();
            response.setStatus(500);
            response.setData(null);
            response.setErrors(List.of(e));
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/api/products/{productId}/options")
    public ResponseEntity<List<Option>> fetchProductOptions(@RequestHeader("Authorization") String token,@PathVariable Long productId) {
        try {
            Seller seller = sellerRepository.findSellerByToken(token).orElse(null);
            if (seller == null) throw new Exception("Not logged in");
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null || !product.getIdSeller().equals(seller.getId())) {
                return ResponseEntity.status(404).body(null);
            }
            List<Option> options = optionService.fetchOptionsByProductId(productId);
            return ResponseEntity.ok(options);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/api/products/{productId}")
    public ResponseEntity<ApiResponse> updateProduct(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productId,
            @RequestBody UpdateProductRequest updateRequest) {
        try {
            Seller seller = sellerRepository.findSellerByToken(token.replace("Bearer ","")).orElse(null);
            if (seller == null) throw new Exception("Not logged in");
            
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null || !product.getIdSeller().equals(seller.getId())) {
                ApiResponse response = new ApiResponse();
                response.setStatus(404);
                return ResponseEntity.status(404).body(response);
            }

            // Update only provided fields
            if (updateRequest.getName() != null) {
                product.setName(updateRequest.getName());
            }
            if (updateRequest.getDescription() != null) {
                product.setDescription(updateRequest.getDescription());
            }
            if (updateRequest.getPrice() != null) {
                product.setPrice(updateRequest.getPrice());
            }
            if (updateRequest.getMedia() != null) {
                product.setMedia(updateRequest.getMedia());
            }
            if (updateRequest.getSkuPrefix() != null && !updateRequest.getSkuPrefix().trim().isEmpty()) {
                product.setSkuPrefix(updateRequest.getSkuPrefix().trim());
            }

            productRepository.save(product);

            ApiResponse response = new ApiResponse();
            response.setStatus(200);
            response.setData(product);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse response = new ApiResponse();
            response.setStatus(500);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/api/products/{productId}/options")
    public ResponseEntity<ApiResponse> updateProductOptions(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productId,
            @RequestBody List<Option> options) {
        try {
            Seller seller = sellerRepository.findSellerByToken(token.replace("Bearer ","")).orElse(null);
            if (seller == null) throw new Exception("Not logged in");
            
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null || !product.getIdSeller().equals(seller.getId())) {
                ApiResponse response = new ApiResponse();
                response.setStatus(404);
                return ResponseEntity.status(404).body(response);
            }

            // Get existing options for this product
            List<Option> existingOptions = optionRepository.findByIdProduct(productId);
            Map<Long, Option> existingOptionsMap = existingOptions.stream()
                    .collect(Collectors.toMap(Option::getIdOption, Function.identity()));

            // Process incoming options
            Set<Long> updatedOptionIds = new HashSet<>();
            
            for (Option incomingOption : options) {
                Option optionToSave;
                
                if (incomingOption.getIdOption() != null && existingOptionsMap.containsKey(incomingOption.getIdOption())) {
                    // Update existing option
                    optionToSave = existingOptionsMap.get(incomingOption.getIdOption());
                    optionToSave.setLabel(incomingOption.getLabel());
                    updatedOptionIds.add(incomingOption.getIdOption());
                } else {
                    // Create new option
                    optionToSave = new Option();
                    optionToSave.setLabel(incomingOption.getLabel());
                    optionToSave.setIdProduct(productId);
                }
                
                Option savedOption = optionRepository.save(optionToSave);
                
                // Handle option values - replace all values for this option
                List<OptionValue> existingValues = optionValueRepository.findByIdOption(savedOption.getIdOption());
                for (OptionValue existingValue : existingValues) {
                    optionValueRepository.delete(existingValue);
                }
                
                // Add new values
                if (incomingOption.getOptionValues() != null) {
                    for (OptionValue value : incomingOption.getOptionValues()) {
                        OptionValue newValue = new OptionValue();
                        newValue.setIdOption(savedOption.getIdOption());
                        newValue.setValue(value.getValue());
                        optionValueRepository.save(newValue);
                    }
                }
            }

            // Delete options that weren't included in the update
            for (Option existingOption : existingOptions) {
                if (!updatedOptionIds.contains(existingOption.getIdOption())) {
                    // Delete option values first
                    List<OptionValue> valuesToDelete = optionValueRepository.findByIdOption(existingOption.getIdOption());
                    for (OptionValue value : valuesToDelete) {
                        optionValueRepository.delete(value);
                    }
                    // Delete the option
                    optionRepository.delete(existingOption);
                }
            }

            ApiResponse response = new ApiResponse();
            response.setStatus(200);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse response = new ApiResponse();
            response.setStatus(500);
            return ResponseEntity.status(500).body(response);
        }
    }
}
