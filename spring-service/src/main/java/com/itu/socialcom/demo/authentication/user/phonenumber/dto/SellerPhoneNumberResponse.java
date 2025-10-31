package com.itu.socialcom.demo.authentication.user.phonenumber.dto;

import com.itu.socialcom.demo.authentication.user.phonenumber.SellerPhoneNumber;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerPhoneNumberResponse {
    private Long id;
    private String phoneNumber;
    private String associatedName;
    private Long paymentMethodId;
    private String paymentMethodName;
    private Long sellerId;

    public static SellerPhoneNumberResponse fromEntity(SellerPhoneNumber entity) {
        SellerPhoneNumberResponse response = new SellerPhoneNumberResponse();
        response.setId(entity.getId());
        response.setPhoneNumber(entity.getPhoneNumber());
        response.setAssociatedName(entity.getAssociatedName());
        response.setPaymentMethodId(entity.getPaymentMethod().getId());
        response.setPaymentMethodName(entity.getPaymentMethod().getPaymentName());
        response.setSellerId(entity.getSeller().getId());
        return response;
    }
}

