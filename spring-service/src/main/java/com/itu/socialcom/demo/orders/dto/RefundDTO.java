package com.itu.socialcom.demo.orders.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RefundDTO {
    private Long id;
    private Long orderId;
    private Double amount;
    private Integer saleId;
    private LocalDateTime createdAt;
}
