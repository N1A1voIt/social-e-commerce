package com.itu.socialcom.demo.sales;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_details_v2")
public class SalesDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sale_details")
    private Integer idSaleDetails;

    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "quantity", precision = 15, scale = 2)
    private BigDecimal quantity;

    @Column(name = "product_name", columnDefinition = "TEXT")
    private String productName;

    @Column(name = "variant_name", columnDefinition = "TEXT")
    private String variantName;

    @Column(name = "id_product", nullable = false)
    private Integer idProduct;

    @Column(name = "id_variant", nullable = false)
    private Integer idVariant;

    @ManyToOne
    @JoinColumn(name = "id_sale_m")
    @JsonIgnore
    private Sales sale;


    // Constructors
    public SalesDetails() {
    }

    // Getters / Setters
    public Integer getIdSaleDetails() {
        return idSaleDetails;
    }

    public void setIdSaleDetails(Integer idSaleDetails) {
        this.idSaleDetails = idSaleDetails;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public Integer getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(Integer idProduct) {
        this.idProduct = idProduct;
    }

    public Integer getIdVariant() {
        return idVariant;
    }

    public void setIdVariant(Integer idVariant) {
        this.idVariant = idVariant;
    }

    public Sales getSale() {
        return sale;
    }

    public void setSale(Sales sale) {
        this.sale = sale;
    }
}
