package com.itu.socialcom.demo.client.cart.model;

import com.itu.socialcom.demo.client.customer.Customer;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing a shopping cart in the social commerce platform.
 */
@Data
@Entity
@Getter
@Setter
@Table(name = "cart")
public class Cart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cart")
    private Long idCart;
    
    /**
     * Foreign key reference to customer table
     */
    @ManyToOne
    @JoinColumn(name = "id_customer", nullable = false)
    private Customer customer;
    
    /**
     * Creation timestamp
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * Cart state (active/inactive)
     */
    @Column(name = "state", nullable = false)
    private Boolean state;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (state == null) {
            state = true;
        }
    }
    
    /**
     * Default constructor
     */
    public Cart() {}
    
    /**
     * Constructor with customer
     */
    public Cart(Customer customer) {
        this.customer = customer;
        this.state = true;
    }
    
    /**
     * Check if the cart is active
     * @return true if the cart is active, false otherwise
     */
    public boolean isActive() {
        return state != null && state;
    }
}