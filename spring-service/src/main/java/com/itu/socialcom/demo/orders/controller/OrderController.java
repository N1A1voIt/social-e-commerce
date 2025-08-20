package com.itu.socialcom.demo.orders.controller;

import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.dto.MessageOrdering;
import com.itu.socialcom.demo.orders.service.CreateOrderFromMessage;
import com.itu.socialcom.demo.orders.service.OrderCreationService;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController {
    @Autowired
    private OrderCreationService orderCreationService;
    @Autowired
    private CreateOrderFromMessage createOrderFromMessage;
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
    public ResponseEntity<ApiResponse> createOrderFromMessage(MessageOrdering orderParent) {
        orderCreationService = createOrderFromMessage;
        try {
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
