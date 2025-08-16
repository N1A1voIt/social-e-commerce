package com.itu.socialcom.demo.products.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for generating all variant combinations automatically
 */
@Data
public class GenerateVariantsRequest {
    
    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.00", message = "Price must be non-negative")
    @JsonProperty("basePrice")
    private BigDecimal basePrice;
    
    @JsonProperty("titlePrefix")
    private String titlePrefix = "Variant";
    
    @JsonProperty("overwriteExisting")
    private boolean overwriteExisting = false;
    
    /**
     * Default constructor
     */
    public GenerateVariantsRequest() {}
    
    /**
     * Constructor with required fields
     */
    public GenerateVariantsRequest(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
    
    /**
     * Constructor with all fields
     */
    public GenerateVariantsRequest(BigDecimal basePrice, String titlePrefix, boolean overwriteExisting) {
        this.basePrice = basePrice;
        this.titlePrefix = titlePrefix != null ? titlePrefix : "Variant";
        this.overwriteExisting = overwriteExisting;
    }
}