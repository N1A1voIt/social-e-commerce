package com.itu.socialcom.demo.products.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for product data transfer with nested variants and options
 */
@Data
public class ProductDTO {
    
    @JsonProperty("id")
    private Long idProduct;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("price")
    private BigDecimal price;
    
    @JsonProperty("media")
    private String media;
    
    @JsonProperty("sellerId")
    private Integer idSeller;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    @JsonProperty("variants")
    private List<VariantDTO> variants;
    
    @JsonProperty("options")
    private List<OptionDTO> options;
    
    /**
     * Default constructor
     */
    public ProductDTO() {}
    
    /**
     * Constructor with basic fields
     */
    public ProductDTO(Long idProduct, String name, String description, BigDecimal price, 
                     String media, Integer idSeller) {
        this.idProduct = idProduct;
        this.name = name;
        this.description = description;
        this.price = price;
        this.media = media;
        this.idSeller = idSeller;
    }
    
    /**
     * Constructor with all fields
     */
    public ProductDTO(Long idProduct, String name, String description, BigDecimal price, 
                     String media, Integer idSeller, LocalDateTime createdAt, LocalDateTime updatedAt,
                     List<VariantDTO> variants, List<OptionDTO> options) {
        this.idProduct = idProduct;
        this.name = name;
        this.description = description;
        this.price = price;
        this.media = media;
        this.idSeller = idSeller;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.variants = variants;
        this.options = options;
    }
}