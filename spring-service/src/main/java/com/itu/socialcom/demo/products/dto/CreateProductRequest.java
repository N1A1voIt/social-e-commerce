package com.itu.socialcom.demo.products.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating products with variants and options
 */
@Data
public class CreateProductRequest {
    
    @NotBlank(message = "Product name cannot be blank")
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.00", message = "Product price must be greater than or equal to 0")
    @JsonProperty("price")
    private BigDecimal price;
    
    @JsonProperty("media")
    private String media;
    
    @Valid
    @JsonProperty("variants")
    private List<CreateVariantRequest> variants;
    
    @Valid
    @JsonProperty("options")
    private List<CreateOptionRequest> options;
    
    /**
     * Default constructor
     */
    public CreateProductRequest() {}
    
    /**
     * Constructor with required fields
     */
    public CreateProductRequest(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }
    
    /**
     * Constructor with all fields
     */
    public CreateProductRequest(String name, String description, BigDecimal price, String media,
                               List<CreateVariantRequest> variants, List<CreateOptionRequest> options) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.media = media;
        this.variants = variants;
        this.options = options;
    }
}