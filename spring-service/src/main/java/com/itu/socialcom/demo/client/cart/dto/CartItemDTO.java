package com.itu.socialcom.demo.client.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for cart item details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long productId;
    private String productName;
    private String productMedia;
    private Long variantId;
    private String variantTitle;
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal totalPrice;
}