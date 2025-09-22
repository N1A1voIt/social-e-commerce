package com.itu.socialcom.demo.delivery.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.itu.socialcom.demo.shipping.entity.ShippingPoint;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a delivery.
 */
@Data
@Entity
@Getter
@Setter
@Table(name = "delivery_v2")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_delivery")
    private Long id;
    
    /**
     * Shipping address for the delivery
     */
    @Column(name = "shipping_address", length = 50)
    private String shippingAddress;
    
    /**
     * When the delivery ended
     */
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    /**
     * Contact phone number for the delivery
     */
    @Column(name = "phone_number", length = 50)
    private String phoneNumber;
    
    /**
     * When the delivery started
     */
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    
    /**
     * Current status of the delivery
     */
    @Column(name = "d_status", length = 50, nullable = false)
    private String status;
    
    /**
     * Amount charged for the delivery
     */
    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;
    
    /**
     * Foreign key reference to shipping_points table
     */
    @Column(name = "id_shp", nullable = false)
    private Long shippingPointId;
    
    /**
     * Foreign key reference to order_mother table
     */
    @Column(name = "id_order_m", nullable = false)
    private Long orderMotherId;

    @Column(name = "id_dd")
    private Long deliveryDriverId;
    
    /**
     * ManyToOne relationship with ShippingPoint
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_shp", insertable = false, updatable = false)
    private ShippingPoint shippingPoint;

    @Column(name = "distance")
    private double distance; // in km
    
    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
        if (status == null) {
            status = "pending"; // Default status
        }
    }
}