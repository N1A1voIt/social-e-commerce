package com.itu.socialcom.demo.products.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for updating products (partial updates)
 */
@Data
public class UpdateProductRequest {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @DecimalMin(value = "0.00", message = "Product price must be greater than or equal to 0")
    @JsonProperty("price")
    private BigDecimal price;
    
    @JsonProperty("media")
    private String media;
    
    @JsonProperty("skuPrefix")
    private String skuPrefix;
    
    /**
     * Default constructor
     */
    public UpdateProductRequest() {}
    
    /**
     * Constructor with all fields
     */
    public UpdateProductRequest(String name, String description, BigDecimal price, String media, String skuPrefix) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.media = media;
        this.skuPrefix = skuPrefix;
    }
}