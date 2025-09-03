package com.itu.socialcom.demo.shipping.repository;

import com.itu.socialcom.demo.shipping.entity.ShippingPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ShippingPoint entity
 */
@Repository
public interface ShippingPointRepository extends JpaRepository<ShippingPoint, Long> {
    
    /**
     * Find all shipping points for a specific managed page
     */
    List<ShippingPoint> findByManagedPageId(Long managedPageId);
    
    /**
     * Find a specific shipping point by ID and managed page ID
     */
    Optional<ShippingPoint> findByIdAndManagedPageId(Long id, Long managedPageId);
    
    /**
     * Find all shipping points for a seller through managed pages
     */
    @Query("SELECT sp FROM ShippingPoint sp JOIN ManagedPage mp ON sp.managedPageId = mp.id WHERE mp.sellerId = :sellerId")
    List<ShippingPoint> findBySellerIdThroughManagedPages(@Param("sellerId") Long sellerId);
    
    /**
     * Check if a shipping point exists for a managed page
     */
    boolean existsByManagedPageId(Long managedPageId);
    
    /**
     * Delete shipping points by managed page ID
     */
    void deleteByManagedPageId(Long managedPageId);
}