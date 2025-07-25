package com.itu.socialcom.demo.products.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for variant data transfer
 */
@Data
public class VariantDTO {
    
    @JsonProperty("id")
    private Long idVariant;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("price")
    private BigDecimal price;
    
    @JsonProperty("productId")
    private Long idProduct;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    /**
     * Default constructor
     */
    public VariantDTO() {}
    
    /**
     * Constructor with required fields
     */
    public VariantDTO(Long idVariant, String title, BigDecimal price, Long idProduct) {
        this.idVariant = idVariant;
        this.title = title;
        this.price = price;
        this.idProduct = idProduct;
    }
    
    /**
     * Constructor with all fields
     */
    public VariantDTO(Long idVariant, String title, BigDecimal price, Long idProduct, 
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.idVariant = idVariant;
        this.title = title;
        this.price = price;
        this.idProduct = idProduct;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}