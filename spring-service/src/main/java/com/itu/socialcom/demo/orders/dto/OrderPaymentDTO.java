package com.itu.socialcom.demo.orders.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderPaymentDTO {
    private Long id;
    private Integer salesId;
    private Double amount;
    private Integer paymentMethodId;
    private String paymentMethodName;
    private LocalDateTime createdAt;
}
