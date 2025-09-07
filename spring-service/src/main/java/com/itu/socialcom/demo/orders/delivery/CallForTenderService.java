package com.itu.socialcom.demo.orders.delivery;

import com.itu.socialcom.demo.delivery.entity.Delivery;
import com.itu.socialcom.demo.orders.dto.CallForTendersRequest;

public interface CallForTenderService {
    Delivery transfromToDelivery(CallForTendersRequest request);
}
