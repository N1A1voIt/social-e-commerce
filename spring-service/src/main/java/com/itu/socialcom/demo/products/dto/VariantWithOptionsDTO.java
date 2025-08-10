package com.itu.socialcom.demo.products.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for variant responses with associated option information
 */
@Data
public class VariantWithOptionsDTO {
    
    @JsonProperty("idVariant")
    private Long idVariant;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("price")
    private BigDecimal price;
    
    @JsonProperty("idProduct")
    private Long idProduct;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    @JsonProperty("stockQuantity")
    private Integer stockQuantity;
    
    @JsonProperty("stockStatus")
    private String stockStatus;
    
    @JsonProperty("options")
    private List<VariantOptionDTO> options;
    
    /**
     * Default constructor
     */
    public VariantWithOptionsDTO() {}
    
    /**
     * Constructor with required fields
     */
    public VariantWithOptionsDTO(Long idVariant, String title, BigDecimal price, Long idProduct) {
        this.idVariant = idVariant;
        this.title = title;
        this.price = price;
        this.idProduct = idProduct;
    }
    
    /**
     * Constructor with all fields
     */
    public VariantWithOptionsDTO(Long idVariant, String title, BigDecimal price, Long idProduct,
                                LocalDateTime createdAt, LocalDateTime updatedAt, 
                                Integer stockQuantity, String stockStatus, 
                                List<VariantOptionDTO> options) {
        this.idVariant = idVariant;
        this.title = title;
        this.price = price;
        this.idProduct = idProduct;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.stockQuantity = stockQuantity;
        this.stockStatus = stockStatus;
        this.options = options;
    }
}