package com.itu.socialcom.demo.products.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.products.model.Option;
import com.itu.socialcom.demo.products.model.Product;
import com.itu.socialcom.demo.products.repository.ProductRepository;
import com.itu.socialcom.demo.products.service.OptionService;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OptionController {

    @Autowired
    private OptionService optionService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private TokenV2ServiceImpl tokenService;

    /**
     * Get all options for a specific product
     */
    @GetMapping("/options/product/{productId}")
    public ResponseEntity<?> getOptionsByProduct(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productId) {
        try {
            // Validate token
            token = token.replace("Bearer ", "");
            Seller seller = tokenService.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse response = new ApiResponse();
                response.setStatus(401);
                return ResponseEntity.status(401).body(response);
            }

            // Check if product exists and belongs to the seller
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null || !product.getIdSeller().equals(seller.getId())) {
                ApiResponse response = new ApiResponse();
                response.setStatus(404);
                return ResponseEntity.status(404).body(response);
            }

            // Get options for the product
            List<Option> options = optionService.fetchOptionsByProductId(productId);
            
            ApiResponse response = new ApiResponse();
            response.setStatus(200);
            response.setData(options);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse response = new ApiResponse();
            response.setStatus(500);
            return ResponseEntity.status(500).body(response);
        }
    }
}