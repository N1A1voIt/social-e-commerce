package com.itu.socialcom.demo.authentication.user.phonenumber.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class SellerPhoneNumberRequest {
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Associated name is required")
    private String associatedName;

    @NotNull(message = "Payment method ID is required")
    private Long paymentMethodId;
}

