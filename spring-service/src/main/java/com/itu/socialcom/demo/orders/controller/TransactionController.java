package com.itu.socialcom.demo.orders.controller;

import com.google.protobuf.Api;
import com.itu.socialcom.demo.moneytransactions.*;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/transaction")
public class TransactionController {
    @Autowired
    ProviderFactory providerFactory;

    @PostMapping("/{method}")
    public ResponseEntity<ApiResponse> transaction(@PathVariable("method") String method, @RequestBody PaymentRequest transactionDetail) {
        try {
            PaymentProvider provider = providerFactory.getProvider(method);
            provider.initiateProvider("mvola.properties");
            PaymentResponse paymentResponse = provider.initiateTransaction(transactionDetail);
            ApiResponse apiResponse = new ApiResponse();

            apiResponse.setStatus(200);
            apiResponse.setData(paymentResponse);
            return ResponseEntity.status(200).body(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

