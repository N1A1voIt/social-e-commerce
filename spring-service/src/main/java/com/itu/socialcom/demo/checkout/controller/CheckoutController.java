package com.itu.socialcom.demo.checkout.controller;

import com.google.protobuf.Api;
import com.itu.socialcom.demo.checkout.dto.CheckoutRequest;
import com.itu.socialcom.demo.checkout.service.CheckoutService;
import com.itu.socialcom.demo.client.customer.Customer;
import com.itu.socialcom.demo.client.customer.CustomerRepository;
import com.itu.socialcom.demo.client.customer.CustomerServiceImpl;
import com.itu.socialcom.demo.client.customertoken.CustomerTokenServiceImpl;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final CustomerTokenServiceImpl customerService;
    @PostMapping
    public ResponseEntity<ApiResponse> checkout(@RequestBody CheckoutRequest checkoutRequest, @RequestHeader(name = "Authorization") String token) {
        try {
            Customer customer = customerService.findCustomerByToken(token.replace("Bearer ","")).orElse(null);
            checkoutRequest.setCustomerId(customer.getIdCustomer());
            ApiResponse apiResponse = new ApiResponse();
            if (customer == null) {
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to checkout")));
                return ResponseEntity.status(401).body(apiResponse);
            }
            apiResponse.setStatus(200);
            checkoutRequest.setSellerId(checkoutRequest.getSellerId());
            OrderParent order = checkoutService.checkout(checkoutRequest);
            apiResponse.setData(order);
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
}
