package com.itu.socialcom.demo.checkout.service;

import com.itu.socialcom.demo.checkout.dto.CheckoutRequest;
import com.itu.socialcom.demo.orders.OrderParent;

public interface CheckoutService {
    OrderParent checkout(CheckoutRequest checkoutRequest);
}
