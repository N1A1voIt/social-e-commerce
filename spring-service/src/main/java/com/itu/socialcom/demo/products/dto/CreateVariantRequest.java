package com.itu.socialcom.demo.products.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for creating product variants
 */
@Data
public class CreateVariantRequest {
    
    @NotBlank(message = "Variant title cannot be blank")
    @JsonProperty("title")
    private String title;
    
    @NotNull(message = "Variant price is required")
    @DecimalMin(value = "0.00", message = "Variant price must be greater than or equal to 0")
    @JsonProperty("price")
    private BigDecimal price;
    
    /**
     * Default constructor
     */
    public CreateVariantRequest() {}
    
    /**
     * Constructor with all fields
     */
    public CreateVariantRequest(String title, BigDecimal price) {
        this.title = title;
        this.price = price;
    }
}