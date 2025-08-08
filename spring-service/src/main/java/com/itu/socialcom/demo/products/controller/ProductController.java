package com.itu.socialcom.demo.products.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.authentication.user.SellerRepository;
import com.itu.socialcom.demo.products.model.Product;
import com.itu.socialcom.demo.products.model.ProductCPL;
import com.itu.socialcom.demo.products.repository.ProductCplRepository;
import com.itu.socialcom.demo.products.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductController {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductCplRepository productCplRepository;
    @Autowired
    private TokenV2ServiceImpl sellerRepository;
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
}
