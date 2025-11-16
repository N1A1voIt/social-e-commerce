package com.itu.socialcom.demo.products.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for updating variants (partial updates for title and price only)
 * Option value modifications are handled by separate endpoints
 */
@Data
public class UpdateVariantRequest {
    
    @JsonProperty("title")
    private String title;
    
    @DecimalMin(value = "0.00", message = "Price must be non-negative")
    @JsonProperty("price")
    private BigDecimal price;
    
    @JsonProperty("sku")
    private String sku;
    
    /**
     * Default constructor
     */
    public UpdateVariantRequest() {}
    
    /**
     * Constructor with all fields
     */
    public UpdateVariantRequest(String title, BigDecimal price, String sku) {
        this.title = title;
        this.price = price;
        this.sku = sku;
    }
}