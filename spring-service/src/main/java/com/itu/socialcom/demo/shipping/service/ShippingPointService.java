package com.itu.socialcom.demo.shipping.service;

import com.itu.socialcom.demo.shipping.entity.ShippingPoint;
import com.itu.socialcom.demo.shipping.repository.ShippingPointRepository;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing shipping points
 */
@Service
public class ShippingPointService {

    private final ShippingPointRepository shippingPointRepository;
    private final ManagedPageRepository managedPageRepository;

    @Autowired
    public ShippingPointService(ShippingPointRepository shippingPointRepository, 
                               ManagedPageRepository managedPageRepository) {
        this.shippingPointRepository = shippingPointRepository;
        this.managedPageRepository = managedPageRepository;
    }

    /**
     * Get all shipping points for a seller
     */
    public List<ShippingPoint> getShippingPointsBySellerId(Long sellerId) {
        return shippingPointRepository.findBySellerIdThroughManagedPages(sellerId);
    }

    /**
     * Get all shipping points for a managed page
     */
    public List<ShippingPoint> getShippingPointsByManagedPageId(Long managedPageId) {
        return shippingPointRepository.findByManagedPageId(managedPageId);
    }

    /**
     * Get a specific shipping point by ID
     */
    public Optional<ShippingPoint> getShippingPointById(Long id) {
        return shippingPointRepository.findById(id);
    }

    /**
     * Create a new shipping point
     */
    @Transactional
    public ShippingPoint createShippingPoint(ShippingPoint shippingPoint, Long sellerId) {
        // Verify that the managed page belongs to the seller
        Optional<ManagedPage> managedPageOpt = managedPageRepository.findById(shippingPoint.getManagedPageId());
        
        if (managedPageOpt.isPresent() && managedPageOpt.get().getSellerId().equals(sellerId)) {
            return shippingPointRepository.save(shippingPoint);
        } else {
            throw new IllegalArgumentException("Managed page not found or does not belong to the seller");
        }
    }

    /**
     * Update an existing shipping point
     */
    @Transactional
    public ShippingPoint updateShippingPoint(Long id, ShippingPoint shippingPointDetails, Long sellerId) {
        // Find the shipping point and verify ownership through managed page
        Optional<ShippingPoint> shippingPointOpt = shippingPointRepository.findById(id);
        
        if (shippingPointOpt.isPresent()) {
            ShippingPoint existingShippingPoint = shippingPointOpt.get();
            
            // Verify that the managed page belongs to the seller
            Optional<ManagedPage> managedPageOpt = managedPageRepository.findById(existingShippingPoint.getManagedPageId());
            
            if (managedPageOpt.isPresent() && managedPageOpt.get().getSellerId().equals(sellerId)) {
                // Update fields
                existingShippingPoint.setPlaceName(shippingPointDetails.getPlaceName());
                existingShippingPoint.setLatitude(shippingPointDetails.getLatitude());
                existingShippingPoint.setLongitude(shippingPointDetails.getLongitude());
                existingShippingPoint.setDistance(shippingPointDetails.getDistance());
                existingShippingPoint.setOrigin(shippingPointDetails.getOrigin());
                
                // Only update managed page ID if it's provided and belongs to the seller
                if (shippingPointDetails.getManagedPageId() != null && 
                    !shippingPointDetails.getManagedPageId().equals(existingShippingPoint.getManagedPageId())) {
                    
                    Optional<ManagedPage> newManagedPageOpt = managedPageRepository.findById(shippingPointDetails.getManagedPageId());
                    
                    if (newManagedPageOpt.isPresent() && newManagedPageOpt.get().getSellerId().equals(sellerId)) {
                        existingShippingPoint.setManagedPageId(shippingPointDetails.getManagedPageId());
                    } else {
                        throw new IllegalArgumentException("New managed page not found or does not belong to the seller");
                    }
                }
                
                return shippingPointRepository.save(existingShippingPoint);
            } else {
                throw new IllegalArgumentException("Shipping point does not belong to the seller");
            }
        } else {
            throw new IllegalArgumentException("Shipping point not found");
        }
    }

    /**
     * Delete a shipping point
     */
    @Transactional
    public void deleteShippingPoint(Long id, Long sellerId) {
        // Find the shipping point and verify ownership through managed page
        Optional<ShippingPoint> shippingPointOpt = shippingPointRepository.findById(id);
        
        if (shippingPointOpt.isPresent()) {
            ShippingPoint shippingPoint = shippingPointOpt.get();
            
            // Verify that the managed page belongs to the seller
            Optional<ManagedPage> managedPageOpt = managedPageRepository.findById(shippingPoint.getManagedPageId());
            
            if (managedPageOpt.isPresent() && managedPageOpt.get().getSellerId().equals(sellerId)) {
                shippingPointRepository.deleteById(id);
            } else {
                throw new IllegalArgumentException("Shipping point does not belong to the seller");
            }
        } else {
            throw new IllegalArgumentException("Shipping point not found");
        }
    }
}