package com.itu.socialcom.demo.moneytransactions;

import com.itu.socialcom.demo.authentication.user.phonenumber.dto.PaymentMethodResponse;

import java.util.List;

public interface PaymentMethodService {
    /**
     * Get all available payment methods
     */
    List<PaymentMethodResponse> getAllPaymentMethods();

    /**
     * Get a payment method by ID
     */
    PaymentMethodResponse getById(Long id);

    /**
     * Get a payment method by name
     */
    PaymentMethodResponse getByName(String name);
}

