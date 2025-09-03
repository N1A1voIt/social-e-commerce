package com.itu.socialcom.demo.delivery.repository;

import com.itu.socialcom.demo.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Delivery entity
 */
@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    
    /**
     * Find deliveries by shipping point ID
     */
    List<Delivery> findByShippingPointId(Long shippingPointId);
    
    /**
     * Find deliveries by order mother ID
     */
    List<Delivery> findByOrderMotherId(Long orderMotherId);
    
    /**
     * Find deliveries by status
     */
    List<Delivery> findByStatus(String status);
    
    /**
     * Find deliveries for a specific managed page through shipping points
     */
    @Query("SELECT d FROM Delivery d JOIN ShippingPoint sp ON d.shippingPointId = sp.id WHERE sp.managedPageId = :managedPageId")
    List<Delivery> findByManagedPageId(@Param("managedPageId") Long managedPageId);
    
    /**
     * Find deliveries for a specific seller through managed pages and shipping points
     */
    @Query("SELECT d FROM Delivery d JOIN ShippingPoint sp ON d.shippingPointId = sp.id JOIN ManagedPage mp ON sp.managedPageId = mp.id WHERE mp.sellerId = :sellerId")
    List<Delivery> findBySellerIdThroughManagedPages(@Param("sellerId") Long sellerId);
    
    /**
     * Find a specific delivery by ID and seller ID (through managed pages)
     */
    @Query("SELECT d FROM Delivery d JOIN ShippingPoint sp ON d.shippingPointId = sp.id JOIN ManagedPage mp ON sp.managedPageId = mp.id WHERE d.id = :deliveryId AND mp.sellerId = :sellerId")
    Optional<Delivery> findByIdAndSellerId(@Param("deliveryId") Long deliveryId, @Param("sellerId") Long sellerId);
}