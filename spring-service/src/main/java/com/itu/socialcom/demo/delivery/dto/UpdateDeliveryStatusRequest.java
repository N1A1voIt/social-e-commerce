package com.itu.socialcom.demo.delivery.dto;

import lombok.Data;

/**
 * DTO for updating a delivery status
 */
@Data
public class UpdateDeliveryStatusRequest {
    /**
     * New status for the delivery
     */
    private String status;
}