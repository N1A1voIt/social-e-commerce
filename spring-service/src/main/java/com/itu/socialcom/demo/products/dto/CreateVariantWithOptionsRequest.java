package com.itu.socialcom.demo.products.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating variants with option value combinations
 */
@Data
public class CreateVariantWithOptionsRequest {
    
    @NotBlank(message = "Variant title is required")
    @JsonProperty("title")
    private String title;
    
    @NotNull(message = "Variant price is required")
    @DecimalMin(value = "0.00", message = "Price must be non-negative")
    @JsonProperty("price")
    private BigDecimal price;

    /**
     * Default constructor
     */
    public CreateVariantWithOptionsRequest() {}

    @NotNull(message = "SKU is needed for variant")
    @JsonProperty("sku")
    private String sku;

    @NotEmpty(message = "At least one option value must be selected")
    @JsonProperty("optionValueIds")
    private List<Long> optionValueIds;

    /**
     * Constructor with all fields
     */
    public CreateVariantWithOptionsRequest(String title, BigDecimal price, List<Long> optionValueIds) {
        this.title = title;
        this.price = price;
        this.optionValueIds = optionValueIds;
    }
}