package com.itu.socialcom.demo.orders.dto;

import lombok.Data;

@Data
public class RefundRequest {
    Integer orderId;
    double amount;
}
