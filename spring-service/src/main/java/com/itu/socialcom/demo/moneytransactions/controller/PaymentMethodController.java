package com.itu.socialcom.demo.moneytransactions.controller;

import com.itu.socialcom.demo.authentication.user.phonenumber.dto.PaymentMethodResponse;
import com.itu.socialcom.demo.moneytransactions.PaymentMethodService;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
public class PaymentMethodController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    /**
     * Get all available payment methods
     * GET /api/payment-methods
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getAllPaymentMethods() {
        ApiResponse response = new ApiResponse();

        try {
            List<PaymentMethodResponse> paymentMethods = paymentMethodService.getAllPaymentMethods();

            response.setStatus(HttpStatus.OK.value());
            response.setData(paymentMethods);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setErrors(Collections.singletonList(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get a payment method by ID
     * GET /api/payment-methods/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getPaymentMethodById(@PathVariable Long id) {
        ApiResponse response = new ApiResponse();

        try {
            PaymentMethodResponse paymentMethod = paymentMethodService.getById(id);

            response.setStatus(HttpStatus.OK.value());
            response.setData(paymentMethod);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setErrors(Collections.singletonList(e));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Get a payment method by name
     * GET /api/payment-methods/name/{name}
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse> getPaymentMethodByName(@PathVariable String name) {
        ApiResponse response = new ApiResponse();

        try {
            PaymentMethodResponse paymentMethod = paymentMethodService.getByName(name);

            response.setStatus(HttpStatus.OK.value());
            response.setData(paymentMethod);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setErrors(Collections.singletonList(e));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}

