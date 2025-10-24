package com.itu.socialcom.demo.client.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for cart summary.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Long cartId;
    private Long customerId;
    private LocalDateTime createdAt;
    private Boolean active;
    private List<CartItemDTO> items;
    private long itemCount;
    private BigDecimal totalPrice;
}