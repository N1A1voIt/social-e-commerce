package com.itu.socialcom.demo.products.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.authentication.user.SellerRepository;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
}
