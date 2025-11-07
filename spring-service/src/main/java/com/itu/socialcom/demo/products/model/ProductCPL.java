package com.itu.socialcom.demo.products.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "v_product_stock_cpl")
@Immutable
@Data
public class ProductCPL {
    @Id
    @Column(name = "id_product")
    private Long idPc;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "price")
    private Double price;
    @Column(name = "media")
    private String media;
    @Column(name = "id_seller")
    private Long idSeller;
    @Column(name = "id_category")
    private Long idCategory;
    @Column(name = "category")
    private String categoryName;
    @Column(name = "product_number")
    private Double productNumber;
    @Column(name = "stock_status")
    private String stockStatus;
    @Column(name = "sku_prefix")
    private String skuPrefix;
}
