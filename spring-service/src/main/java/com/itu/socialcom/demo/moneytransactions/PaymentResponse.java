package com.itu.socialcom.demo.moneytransactions;

import lombok.Data;

@Data
public class PaymentResponse {
    private String status;
    private String transactionId;
    private String correlationId;
    private String rawResponse;
}
