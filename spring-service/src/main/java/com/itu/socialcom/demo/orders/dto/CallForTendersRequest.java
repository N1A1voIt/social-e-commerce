package com.itu.socialcom.demo.orders.dto;

import com.itu.socialcom.demo.orders.OrderParent;
import lombok.Data;

@Data
public class CallForTendersRequest {
    OrderParent orderParent;
    Long shippingPointId;
    @Override
    public String toString() {
        return "CallForTendersRequest{" +
                "orderParent=" + orderParent.toString() +
                ", shippingPointId=" + shippingPointId +
                '}';
    }
}
