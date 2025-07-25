package com.itu.socialcom.demo.products.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a product variant in the social commerce platform.
 * This entity does not use JPA relationships to maintain manual control over data access.
 */
@Data
@Entity
@Getter
@Setter
@Table(name = "variants_v2")
public class Variant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_variant")
    private Long idVariant;
    
    /**
     * Variant title - required field
     */
    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;
    
    /**
     * Variant price - required field with precision for currency
     */
    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;
    
    /**
     * Foreign key reference to products_v2 table
     * Note: No JPA relationship annotation to maintain manual control
     */
    @Column(name = "id_product", nullable = false)
    private Long idProduct;
    
    // Audit fields for tracking creation and updates
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
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
    public Variant() {}
    
    /**
     * Constructor with required fields
     */
    public Variant(String title, BigDecimal price, Long idProduct) {
        this.title = title;
        this.price = price;
        this.idProduct = idProduct;
    }
    
    /**
     * Get formatted price as string
     * @return formatted price with 2 decimal places
     */
    public String getFormattedPrice() {
        return price != null ? String.format("%.2f", price) : "0.00";
    }
    
    /**
     * Check if this variant has a valid title
     * @return true if title is not null and not empty, false otherwise
     */
    public boolean hasValidTitle() {
        return title != null && !title.trim().isEmpty();
    }
    
    /**
     * Compare variant price with another price
     * @param otherPrice the price to compare with
     * @return negative if this price is less, zero if equal, positive if greater
     */
    public int comparePriceTo(BigDecimal otherPrice) {
        if (price == null && otherPrice == null) return 0;
        if (price == null) return -1;
        if (otherPrice == null) return 1;
        return price.compareTo(otherPrice);
    }
}