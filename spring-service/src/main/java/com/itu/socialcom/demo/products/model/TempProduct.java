package com.itu.socialcom.demo.products.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "temporary_product")
public class TempProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_temp_product")
    private Long idProduct;

    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "media", columnDefinition = "TEXT")
    private String media;

    @Column(name = "id_seller", nullable = false)
    private Long idSeller;

    @Column(name = "id_category", nullable = false)
    private Integer idCategory;

    @Column(name = "state")
    private Boolean state;

    @Column(name = "sku_prefix", nullable = false)
    private String skuPrefix;
}
