package com.itu.socialcom.demo.stocks.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2Service;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.products.repository.ProductRepository;
import com.itu.socialcom.demo.products.variants.repository.VariantRepository;
import com.itu.socialcom.demo.stocks.StockChild;
import com.itu.socialcom.demo.stocks.StockParent;
import com.itu.socialcom.demo.stocks.dto.StockUtilities;
import com.itu.socialcom.demo.stocks.repository.StockChildRepository;
import com.itu.socialcom.demo.stocks.services.StockServiceImpl;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class StockController {
    @Autowired
    StockServiceImpl stockService;
    @Autowired
    StockChildRepository stockChildRepository;

    @Autowired
    TokenV2Service tokenV2Service;
    @GetMapping("/api/stocks/utils")
    public ResponseEntity<ApiResponse> fetchUtilities(Pageable pageable, @RequestHeader(name = "Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) throw new Exception("Not logged in");
            StockUtilities stockUtilities = stockService.getStockUtilities(seller, pageable);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setData(stockUtilities);
            apiResponse.setStatus(200);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
    @PostMapping("/api/stocks")
    public ResponseEntity<ApiResponse> saveStock(@RequestBody StockParent stockParent, @RequestHeader(name = "Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) throw new Exception("Not logged in");
            stockParent.setIdSeller(seller.getId());
            StockParent savedStock = stockService.save(stockParent);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setData(savedStock);
            apiResponse.setStatus(200);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
    @GetMapping("/api/stocks")
    public ResponseEntity<ApiResponse> fetchParents() {
        try {
            List<StockParent> stockParents = stockService.findAll();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setData(stockParents);
            apiResponse.setStatus(200);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
    @GetMapping("/api/stocks/{idMvt}")
    public ResponseEntity<ApiResponse> fetchParentByIdMvt(@org.springframework.web.bind.annotation.PathVariable("idMvt") Long idMvt) {
        try {
           List<StockChild> stockChildren = stockChildRepository.findByIdMv(idMvt);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setData(stockChildren);
            apiResponse.setStatus(200);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
}
