package com.itu.socialcom.demo.orders.dto;

import lombok.Data;

@Data
public class OrderItemDTO {
    private Long idOrderDetails;
    private Double price;
    private Double quantity;
    private Long idVariant;
    private Long idProduct;
    private String mediaUrl;
    private String sku;
    private String productName;
}

