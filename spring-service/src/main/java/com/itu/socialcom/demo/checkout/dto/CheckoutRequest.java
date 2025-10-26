package com.itu.socialcom.demo.checkout.dto;

import lombok.Data;

@Data
public class CheckoutRequest {
    private Long customerId; // Will be set by controller from token
    private Long sellerId;
}
