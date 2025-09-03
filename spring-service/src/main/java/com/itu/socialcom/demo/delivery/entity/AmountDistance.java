package com.itu.socialcom.demo.delivery.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entity representing a configuration for delivery amount based on distance.
 */
@Data
@Entity
@Getter
@Setter
@Table(name = "amount_distance")
public class AmountDistance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_amount_distance")
    private Long id;

    /**
     * The price per unit of distance (e.g., price per kilometer)
     */
    @Column(name = "price_per_distance", nullable = false, precision = 15, scale = 2)
    private BigDecimal pricePerDistance;

    /**
     * Foreign key reference to users table (can be NULL)
     */
    @Column(name = "id_user")
    private Long userId;

    /**
     * Foreign key reference to managed_pages table (can be NULL)
     */
    @Column(name = "id_mp")
    private Long managedPageId;
}
