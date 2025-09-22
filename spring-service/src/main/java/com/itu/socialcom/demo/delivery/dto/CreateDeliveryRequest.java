package com.itu.socialcom.demo.delivery.dto;

import lombok.Data;

/**
 * DTO for creating a new delivery
 */
@Data
public class CreateDeliveryRequest {
    /**
     * ID of the shipping point for the delivery
     */
    private Long shippingPointId;
    
    /**
     * ID of the order associated with the delivery
     */

    private Long orderId;
}