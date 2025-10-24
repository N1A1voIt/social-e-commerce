package com.itu.socialcom.demo.client.cart.model;

import com.itu.socialcom.demo.products.model.Product;
import com.itu.socialcom.demo.products.variants.model.Variant;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing details of items in a shopping cart.
 */
@Data
@Entity
@Getter
@Setter
@Table(name = "cart_details")
public class CartDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cd")
    private Long idCd;
    
    /**
     * Foreign key reference to cart table
     */
    @ManyToOne
    @JoinColumn(name = "id_cart", nullable = false)
    private Cart cart;
    
    /**
     * Foreign key reference to products_v2 table
     */
    @ManyToOne
    @JoinColumn(name = "id_product", nullable = false)
    private Product product;
    
    /**
     * Foreign key reference to variants_v2 table
     */
    @ManyToOne
    @JoinColumn(name = "id_variant", nullable = false)
    private Variant variant;
    
    /**
     * Quantity of the product variant in the cart
     */
    @Column(name = "quantity", nullable = false, precision = 15, scale = 2)
    private BigDecimal quantity;
    
    /**
     * Timestamp when the item was added to the cart
     */
    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;
    
    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
    
    /**
     * Default constructor
     */
    public CartDetails() {}
    
    /**
     * Constructor with required fields
     */
    public CartDetails(Cart cart, Product product, Variant variant, BigDecimal quantity) {
        this.cart = cart;
        this.product = product;
        this.variant = variant;
        this.quantity = quantity;
    }
    
    /**
     * Get the total price for this cart item (price * quantity)
     * @return the total price
     */
    public BigDecimal getTotalPrice() {
        return variant.getPrice().multiply(quantity);
    }
}