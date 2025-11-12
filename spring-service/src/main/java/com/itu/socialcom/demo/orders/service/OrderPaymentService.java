package com.itu.socialcom.demo.orders.service;

import com.itu.socialcom.demo.moneytransactions.PaymentResponse;
import com.itu.socialcom.demo.orders.dto.PaymentDTO;

public interface OrderPaymentService {
    PaymentResponse processOrderPayment(PaymentDTO paymentDTO, String detailsIdentifier) throws Exception;
    PaymentResponse processFullOrderPayment(PaymentDTO paymentDTO, String detailsIdentifier) throws Exception;
    PaymentResponse processCashPayment(Long orderId);
}
