package com.itu.socialcom.demo.products.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "v_stock_movements")
public class StockMovement {
    @Id
    @Column(name = "id_st_ch")
    private Long idStCh;
    
    @Column(name = "action_at")
    private LocalDateTime actionAt;
    
    @Column(name = "input")
    private BigDecimal input;
    
    @Column(name = "output")
    private BigDecimal output;
    
    @Column(name = "price")
    private BigDecimal price;
    
    @Column(name = "product_stock_after")
    private BigDecimal productStockAfter;
    
    @Column(name = "variant_stock_after")
    private BigDecimal variantStockAfter;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "id_product")
    private Long idProduct;
    
    @Column(name = "product_name")
    private String productName;
    
    @Column(name = "sku_prefix")
    private String skuPrefix;
    
    @Column(name = "product_media")
    private String productMedia;
    
    @Column(name = "id_seller")
    private Long idSeller;
    
    @Column(name = "id_variant")
    private Long idVariant;
    
    @Column(name = "variant_name")
    private String variantName;
    
    @Column(name = "variant_sku")
    private String variantSku;
    
    @Column(name = "variant_media")
    private String variantMedia;
    
    @Column(name = "id_category")
    private Long idCategory;
    
    @Column(name = "category_name")
    private String categoryName;
    
    @Column(name = "id_mv")
    private Long idMv;
    
    @Column(name = "movement_description")
    private String movementDescription;
    
    @Column(name = "id_order_m")
    private Long idOrderM;
    
    @Column(name = "d_customer_name")
    private String customerName;
    
    @Column(name = "order_status")
    private String orderStatus;
    
    @Column(name = "movement_type")
    private String movementType;
    
    @Column(name = "net_movement")
    private BigDecimal netMovement;

    // Constructors
    public StockMovement() {}

    // Getters and Setters
    public Long getIdStCh() { return idStCh; }
    public void setIdStCh(Long idStCh) { this.idStCh = idStCh; }

    public LocalDateTime getActionAt() { return actionAt; }
    public void setActionAt(LocalDateTime actionAt) { this.actionAt = actionAt; }

    public BigDecimal getInput() { return input; }
    public void setInput(BigDecimal input) { this.input = input; }

    public BigDecimal getOutput() { return output; }
    public void setOutput(BigDecimal output) { this.output = output; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getProductStockAfter() { return productStockAfter; }
    public void setProductStockAfter(BigDecimal productStockAfter) { this.productStockAfter = productStockAfter; }

    public BigDecimal getVariantStockAfter() { return variantStockAfter; }
    public void setVariantStockAfter(BigDecimal variantStockAfter) { this.variantStockAfter = variantStockAfter; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getIdProduct() { return idProduct; }
    public void setIdProduct(Long idProduct) { this.idProduct = idProduct; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSkuPrefix() { return skuPrefix; }
    public void setSkuPrefix(String skuPrefix) { this.skuPrefix = skuPrefix; }

    public String getProductMedia() { return productMedia; }
    public void setProductMedia(String productMedia) { this.productMedia = productMedia; }

    public Long getIdSeller() { return idSeller; }
    public void setIdSeller(Long idSeller) { this.idSeller = idSeller; }

    public Long getIdVariant() { return idVariant; }
    public void setIdVariant(Long idVariant) { this.idVariant = idVariant; }

    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }

    public String getVariantSku() { return variantSku; }
    public void setVariantSku(String variantSku) { this.variantSku = variantSku; }

    public String getVariantMedia() { return variantMedia; }
    public void setVariantMedia(String variantMedia) { this.variantMedia = variantMedia; }

    public Long getIdCategory() { return idCategory; }
    public void setIdCategory(Long idCategory) { this.idCategory = idCategory; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Long getIdMv() { return idMv; }
    public void setIdMv(Long idMv) { this.idMv = idMv; }

    public String getMovementDescription() { return movementDescription; }
    public void setMovementDescription(String movementDescription) { this.movementDescription = movementDescription; }

    public Long getIdOrderM() { return idOrderM; }
    public void setIdOrderM(Long idOrderM) { this.idOrderM = idOrderM; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getMovementType() { return movementType; }
    public void setMovementType(String movementType) { this.movementType = movementType; }

    public BigDecimal getNetMovement() { return netMovement; }
    public void setNetMovement(BigDecimal netMovement) { this.netMovement = netMovement; }
}