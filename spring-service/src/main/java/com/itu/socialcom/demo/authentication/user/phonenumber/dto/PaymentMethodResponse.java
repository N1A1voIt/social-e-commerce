package com.itu.socialcom.demo.authentication.user.phonenumber.dto;

import com.itu.socialcom.demo.moneytransactions.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodResponse {
    private Long id;
    private String paymentName;

    public static PaymentMethodResponse fromEntity(PaymentMethod entity) {
        return new PaymentMethodResponse(entity.getId(), entity.getPaymentName());
    }
}

