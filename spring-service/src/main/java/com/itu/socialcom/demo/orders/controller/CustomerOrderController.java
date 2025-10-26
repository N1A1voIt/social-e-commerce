package com.itu.socialcom.demo.orders.controller;

import com.itu.socialcom.demo.client.customertoken.CustomerTokenServiceImpl;
import com.itu.socialcom.demo.client.customer.Customer;
import com.itu.socialcom.demo.orders.dto.CustomerOrderDTO;
import com.itu.socialcom.demo.orders.service.CustomerOrderService;
import com.itu.socialcom.demo.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/orders")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final CustomerOrderService customerOrderService;
    private final CustomerTokenServiceImpl customerTokenService;

    @GetMapping
    public ResponseEntity<ApiResponse> getCustomerOrders(@RequestHeader(name = "Authorization") String token) {
        try {
            Customer customer = customerTokenService.findCustomerByToken(token.replace("Bearer ", "")).orElse(null);
            
            if (customer == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to view orders")));
                return ResponseEntity.status(401).body(apiResponse);
            }

            List<CustomerOrderDTO> orders = customerOrderService.getCustomerOrders(customer.getIdCustomer());
            
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(orders);
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

