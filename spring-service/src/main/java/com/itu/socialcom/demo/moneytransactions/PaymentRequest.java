package com.itu.socialcom.demo.moneytransactions;

import lombok.Data;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.LocalDateTime;

@Data
public class PaymentRequest {
    private String amount;
    private String currency;
    private String payer;
    private String payee;
    private String description;
    private String customerMsisdn;
    private LocalDateTime requestDate;
}

