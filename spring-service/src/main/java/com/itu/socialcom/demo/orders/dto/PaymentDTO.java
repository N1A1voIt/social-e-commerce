package com.itu.socialcom.demo.orders.dto;

import lombok.Data;

@Data
public class PaymentDTO {
    String amount;
    String description;
    String phoneNumber;
}
