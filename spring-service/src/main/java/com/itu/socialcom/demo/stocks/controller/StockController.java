package com.itu.socialcom.demo.stocks.controller;

import com.itu.socialcom.demo.stocks.StockChild;
import com.itu.socialcom.demo.stocks.StockParent;
import com.itu.socialcom.demo.stocks.repository.StockChildRepository;
import com.itu.socialcom.demo.stocks.services.StockServiceImpl;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StockController {
    @Autowired
    StockServiceImpl stockService;
    @Autowired
    StockChildRepository stockChildRepository;
    @PostMapping("/api/save")
    public ResponseEntity<ApiResponse> saveStock(StockParent stockParent) {
        try {
            StockParent savedStock = stockService.save(stockParent);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setData(savedStock);
            apiResponse.setStatus(200);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
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
