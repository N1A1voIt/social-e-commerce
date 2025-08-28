package com.itu.socialcom.demo.moneytransactions;

import lombok.Data;

@Data
public class PaymentRequest {
    private String amount;
    private String currency;
    private String payer;
    private String payee;
    private String description;
}

