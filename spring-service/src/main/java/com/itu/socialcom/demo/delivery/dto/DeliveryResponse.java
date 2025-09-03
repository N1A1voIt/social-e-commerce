package com.itu.socialcom.demo.delivery.dto;

import com.itu.socialcom.demo.delivery.entity.Delivery;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for delivery responses
 */
@Data
public class DeliveryResponse {
    /**
     * ID of the delivery
     */
    private Long id;
    
    /**
     * Shipping address for the delivery
     */
    private String shippingAddress;
    
    /**
     * When the delivery ended
     */
    private LocalDateTime endedAt;
    
    /**
     * Contact phone number for the delivery
     */
    private String phoneNumber;
    
    /**
     * When the delivery started
     */
    private LocalDateTime startedAt;
    
    /**
     * Current status of the delivery
     */
    private String status;
    
    /**
     * Amount charged for the delivery
     */
    private BigDecimal amount;
    
    /**
     * ID of the shipping point for the delivery
     */
    private Long shippingPointId;
    
    /**
     * ID of the order associated with the delivery
     */
    private Long orderMotherId;
    
    /**
     * Convert a Delivery entity to a DeliveryResponse DTO
     */
    public static DeliveryResponse fromEntity(Delivery delivery) {
        DeliveryResponse response = new DeliveryResponse();
        response.setId(delivery.getId());
        response.setShippingAddress(delivery.getShippingAddress());
        response.setEndedAt(delivery.getEndedAt());
        response.setPhoneNumber(delivery.getPhoneNumber());
        response.setStartedAt(delivery.getStartedAt());
        response.setStatus(delivery.getStatus());
        response.setAmount(delivery.getAmount());
        response.setShippingPointId(delivery.getShippingPointId());
        response.setOrderMotherId(delivery.getOrderMotherId());
        return response;
    }
}