package com.itu.socialcom.demo.delivery.service;

import com.itu.socialcom.demo.delivery.entity.AmountDistance;
import com.itu.socialcom.demo.delivery.entity.Delivery;
import com.itu.socialcom.demo.delivery.repository.AmountDistanceRepository;
import com.itu.socialcom.demo.delivery.repository.DeliveryRepository;
import com.itu.socialcom.demo.shipping.entity.ShippingPoint;
import com.itu.socialcom.demo.shipping.repository.ShippingPointRepository;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing deliveries
 */
@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final ShippingPointRepository shippingPointRepository;
    private final ManagedPageRepository managedPageRepository;
    private final AmountDistanceRepository amountDistanceRepository;

    @Autowired
    public DeliveryService(DeliveryRepository deliveryRepository,
                          ShippingPointRepository shippingPointRepository,
                          ManagedPageRepository managedPageRepository,
                          AmountDistanceRepository amountDistanceRepository) {
        this.deliveryRepository = deliveryRepository;
        this.shippingPointRepository = shippingPointRepository;
        this.managedPageRepository = managedPageRepository;
        this.amountDistanceRepository = amountDistanceRepository;
    }

    /**
     * Get all deliveries for a seller
     */
    public List<Delivery> getDeliveriesBySellerId(Long sellerId) {
        return deliveryRepository.findBySellerIdThroughManagedPages(sellerId);
    }

    /**
     * Get a specific delivery by ID
     */
    public Optional<Delivery> getDeliveryById(Long id) {
        return deliveryRepository.findById(id);
    }

    /**
     * Get a specific delivery by ID and seller ID
     */
    public Optional<Delivery> getDeliveryByIdAndSellerId(Long id, Long sellerId) {
        return deliveryRepository.findByIdAndSellerId(id, sellerId);
    }

    /**
     * Create a new delivery
     */
    @Transactional
    public Delivery createDelivery(Long shippingPointId, Long orderId, Long sellerId) {
        // Verify that the shipping point exists and belongs to the seller
        Optional<ShippingPoint> shippingPointOpt = shippingPointRepository.findById(shippingPointId);

        if (shippingPointOpt.isPresent()) {
            ShippingPoint shippingPoint = shippingPointOpt.get();

            // Verify that the managed page belongs to the seller
            Optional<ManagedPage> managedPageOpt = managedPageRepository.findById(shippingPoint.getManagedPageId());

            if (managedPageOpt.isPresent() && managedPageOpt.get().getSellerId().equals(sellerId)) {
                // Calculate the delivery amount based on distance
                BigDecimal amount = calculateDeliveryAmount(shippingPoint.getDistance(), sellerId, shippingPoint.getManagedPageId());

                // Create the delivery
                Delivery delivery = new Delivery();
                delivery.setShippingPointId(shippingPointId);
                delivery.setOrderMotherId(orderId);
                delivery.setStartedAt(LocalDateTime.now());
                delivery.setStatus("pending");
                delivery.setAmount(amount);

                return deliveryRepository.save(delivery);
            } else {
                throw new IllegalArgumentException("Shipping point does not belong to the seller");
            }
        } else {
            throw new IllegalArgumentException("Shipping point not found");
        }
    }

    /**
     * Calculate the delivery amount based on distance
     */
    private BigDecimal calculateDeliveryAmount(BigDecimal distance, Long userId, Long managedPageId) {
        // Try to find a user-specific price configuration
        Optional<AmountDistance> userAmountOpt = amountDistanceRepository.findTopByUserIdOrderByIdDesc(userId);
        if (userAmountOpt.isPresent()) {
            return distance.multiply(userAmountOpt.get().getPricePerDistance());
        }

        // Try to find a managed page-specific price configuration
        Optional<AmountDistance> pageAmountOpt = amountDistanceRepository.findTopByManagedPageIdOrderByIdDesc(managedPageId);
        if (pageAmountOpt.isPresent()) {
            return distance.multiply(pageAmountOpt.get().getPricePerDistance());
        }

        // Try to find a default price configuration
        Optional<AmountDistance> defaultAmountOpt = amountDistanceRepository.findDefaultConfiguration();
        if (defaultAmountOpt.isPresent()) {
            return distance.multiply(defaultAmountOpt.get().getPricePerDistance());
        }

        // If no configuration is found, use a default calculation
        // For example, $1 per kilometer
        return distance.multiply(new BigDecimal("1.0"));
    }

    /**
     * Update delivery status
     */
    @Transactional
    public Delivery updateDeliveryStatus(Long id, String status, Long sellerId) {
        Optional<Delivery> deliveryOpt = deliveryRepository.findByIdAndSellerId(id, sellerId);

        if (deliveryOpt.isPresent()) {
            Delivery delivery = deliveryOpt.get();
            delivery.setStatus(status);

            if ("completed".equals(status)) {
                delivery.setEndedAt(LocalDateTime.now());
            }

            return deliveryRepository.save(delivery);
        } else {
            throw new IllegalArgumentException("Delivery not found or does not belong to the seller");
        }
    }
}
