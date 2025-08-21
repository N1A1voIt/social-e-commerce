package com.itu.socialcom.demo.orders.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2Service;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.dto.MessageOrdering;
import com.itu.socialcom.demo.orders.service.CreateOrderFromMessage;
import com.itu.socialcom.demo.orders.service.OrderCreationService;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController {
    @Autowired
    private OrderCreationService orderCreationService;
    @Autowired
    private CreateOrderFromMessage createOrderFromMessage;
    @Autowired
    private TokenV2Service tokenV2Service;
    @PostMapping("/api/orders/save")
    public ResponseEntity<ApiResponse> createOrder(OrderParent orderParent) {
        orderCreationService = createOrderFromMessage;
        try {
            OrderParent createdOrder = orderCreationService.createOrder(orderParent);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(createdOrder);
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
    @PostMapping("/api/orders/save-from-message")
    public ResponseEntity<ApiResponse> createOrderFromMessage(@RequestBody MessageOrdering orderParent, @RequestHeader(name = "Authorization") String token) {
        orderCreationService = createOrderFromMessage;
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to create an order")));
                return ResponseEntity.status(401).body(apiResponse);
            }
            OrderParent createdOrder = orderCreationService.createOrderFromMessage(orderParent);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(createdOrder);
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
