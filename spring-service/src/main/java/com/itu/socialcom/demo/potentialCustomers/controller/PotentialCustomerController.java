package com.itu.socialcom.demo.potentialCustomers.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2Service;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.potentialCustomers.entity.PotentialCustomerV2;
import com.itu.socialcom.demo.potentialCustomers.repository.PotentialCustomerV2Service;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/potential-customers")
public class PotentialCustomerController {

    @Autowired
    private PotentialCustomerV2Service potentialCustomerService;

    @Autowired
    private TokenV2Service tokenV2Service;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllCustomers(@RequestHeader(name = "Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to view customers")));
                return ResponseEntity.status(401).body(apiResponse);
            }

            List<PotentialCustomerV2> customers = potentialCustomerService.findAll();
            
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(customers);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();

            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCustomerById(
            @RequestHeader(name = "Authorization") String token,
            @PathVariable String id) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to view customer")));
                return ResponseEntity.status(401).body(apiResponse);
            }

            PotentialCustomerV2 customer = potentialCustomerService.findById(id).orElse(null);
            if (customer == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(404);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Customer not found")));
                return ResponseEntity.status(404).body(apiResponse);
            }

            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(customer);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }
}
