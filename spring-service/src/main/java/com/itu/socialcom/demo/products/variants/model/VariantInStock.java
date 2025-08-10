package com.itu.socialcom.demo.products.variants.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "v_variant_cpl")
@Immutable
public class VariantInStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_variant")
    private Long idVariant;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "id_product", nullable = false)
    private Long idProduct;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "variant_number")
    private Double variantNumber;

    @Column(name = "stock_status")
    private String stockStatus;

    // Getters
    public Long getIdVariant() {
        return idVariant;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Long getIdProduct() {
        return idProduct;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Double getVariantNumber() {
        return variantNumber;
    }

    public String getStockStatus() {
        return stockStatus;
    }
}
