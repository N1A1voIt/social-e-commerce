package com.itu.socialcom.demo.products.variants.controller;

import com.google.protobuf.Api;
import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.products.variants.model.Variant;
import com.itu.socialcom.demo.products.variants.model.VariantInStock;
import com.itu.socialcom.demo.products.variants.repository.VariantInStockRepository;
import com.itu.socialcom.demo.products.variants.repository.VariantRepository;
import com.itu.socialcom.demo.utils.ApiResponse;
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
}
