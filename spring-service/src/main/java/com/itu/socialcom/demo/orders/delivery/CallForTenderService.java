package com.itu.socialcom.demo.orders.delivery;

import com.itu.socialcom.demo.delivery.entity.Delivery;
import com.itu.socialcom.demo.orders.dto.CallForTendersRequest;
import com.itu.socialcom.demo.utils.ApiResponse;

public interface CallForTenderService {
    Delivery transfromToDelivery(CallForTendersRequest request);
    ApiResponse makeAcall(Delivery delivery);
    ApiResponse makeAcall(int deliveryId);

    /**
     * Sends a WhatsApp template message for the delivery.
     * Uses the "hello_world" template as specified in the example.
     *
     * @param delivery The delivery entity
     * @return ApiResponse with the result of the operation
     */
    ApiResponse sendTemplateMessage(Delivery delivery);

    /**
     * Sends a WhatsApp template message for the delivery with the given ID.
     * Uses the "hello_world" template as specified in the example.
     *
     * @param deliveryId The ID of the delivery
     * @return ApiResponse with the result of the operation
     */
    ApiResponse sendTemplateMessage(int deliveryId);
}
