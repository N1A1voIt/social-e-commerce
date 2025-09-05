package com.itu.socialcom.demo.shipping.entity;

import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entity representing a shipping point.
 */
@Data
@Entity
@Getter
@Setter
@Table(name = "shipping_points")
public class ShippingPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_shp")
    private Long id;
    
    /**
     * Name of the shipping point location
     */
    @Column(name = "place_name", nullable = false, columnDefinition = "TEXT")
    private String placeName;
    
    /**
     * Latitude coordinate of the shipping point
     */
    @Column(name = "latitude", precision = 8, scale = 6)
    private BigDecimal latitude;
    
    /**
     * Longitude coordinate of the shipping point
     */
    @Column(name = "longitude", precision = 8, scale = 6)
    private BigDecimal longitude;
    
    /**
     * Distance from a reference point (e.g., seller location)
     */
    @Column(name = "distance", nullable = false, precision = 15, scale = 2)
    private BigDecimal distance;
    
    /**
     * Origin or source information for the shipping point
     */
    @Column(name = "origin", length = 250)
    private String origin;
    
    /**
     * Foreign key reference to managed_pages table
     * Note: No JPA relationship annotation to maintain manual control
     */
    @Column(name = "id_mp", nullable = false)
    private Long managedPageId;
    
    /**
     * ManyToOne relationship with ManagedPage
     */
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "id_mp", insertable = false, updatable = false)
//    private ManagedPage managedPage;
}