package com.itu.socialcom.demo.sales.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representing a row from the sales CSV file.
 * Each row contains sale information and details about one product in that sale.
 */
@Data
public class SalesCsvRow {
    
    // Sales table fields
    private Integer idSale;
    private BigDecimal amount;
    private LocalDateTime effectuedAt;
    private String fromNumber;
    private String fromName;
    private String description;
    private String platform;
    private String idOrderM;
    
    // Sales details fields
    private BigDecimal price;
    private Integer quantity;
    private String productName;
    private String variantName;
    private String skuProduct;
    private String skuVariant;
}

