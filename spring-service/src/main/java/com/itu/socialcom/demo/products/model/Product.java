package com.itu.socialcom.demo.products.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a product in the social commerce platform.
 * This entity does not use JPA relationships to maintain manual control over data access.
 */
@Data
@Entity
@Getter
@Setter
@Table(name = "products_v2")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_product")
    private Long idProduct;
    
    /**
     * Product name - required field
     */
    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;
    
    /**
     * Product description - optional field
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    /**
     * Product price - required field with precision for currency
     */
    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;
    
    /**
     * Associated media URLs or identifiers (product images, videos, etc.)
     */
    @Column(name = "media", columnDefinition = "TEXT")
    private String media;
    
    /**
     * Foreign key reference to seller_v2 table
     * Note: No JPA relationship annotation to maintain manual control
     */
    @Column(name = "id_seller", nullable = false)
    private Long idSeller;
    
    // Audit fields for tracking creation and updates
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "id_category", nullable = false)
    private Integer idCategory;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Default constructor
     */
    public Product() {}
    
    /**
     * Constructor with required fields
     */
    public Product(String name, BigDecimal price, Long idSeller) {
        this.name = name;
        this.price = price;
        this.idSeller = idSeller;
    }
    
    /**
     * Constructor with all fields except audit fields
     */
    public Product(String name, String description, BigDecimal price, String media, Long idSeller) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.media = media;
        this.idSeller = idSeller;
    }
    
    /**
     * Check if the product has a description
     * @return true if description is not null and not empty, false otherwise
     */
    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }
    
    /**
     * Check if the product has associated media
     * @return true if media is not null and not empty, false otherwise
     */
    public boolean hasMedia() {
        return media != null && !media.trim().isEmpty();
    }
    
    /**
     * Get formatted price as string
     * @return formatted price with 2 decimal places
     */
    public String getFormattedPrice() {
        return price != null ? String.format("%.2f", price) : "0.00";
    }
}