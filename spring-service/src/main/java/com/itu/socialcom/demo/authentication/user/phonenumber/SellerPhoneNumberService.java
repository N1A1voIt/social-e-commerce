package com.itu.socialcom.demo.authentication.user.phonenumber;

import com.itu.socialcom.demo.authentication.user.phonenumber.dto.SellerPhoneNumberRequest;
import com.itu.socialcom.demo.authentication.user.phonenumber.dto.SellerPhoneNumberResponse;

import java.util.List;

public interface SellerPhoneNumberService {
    /**
     * Create or update a seller phone number for a specific payment method
     */
    SellerPhoneNumberResponse createOrUpdate(Long sellerId, SellerPhoneNumberRequest request);

    /**
     * Get all phone numbers configured for a seller
     */
    List<SellerPhoneNumberResponse> getAllBySellerId(Long sellerId);

    /**
     * Get a specific phone number configuration by ID
     */
    SellerPhoneNumberResponse getById(Long id);

    /**
     * Get phone number for a specific seller and payment method
     */
    SellerPhoneNumberResponse getBySellerAndPaymentMethod(Long sellerId, Long paymentMethodId);

    /**
     * Delete a phone number configuration
     */
    void delete(Long id, Long sellerId);
}

