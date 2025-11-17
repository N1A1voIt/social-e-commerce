package com.itu.socialcom.demo.orders.service;

import com.itu.socialcom.demo.orders.Payment;
import com.itu.socialcom.demo.orders.Refund;
import com.itu.socialcom.demo.orders.dto.OrderPaymentDTO;
import com.itu.socialcom.demo.orders.dto.RefundDTO;
import com.itu.socialcom.demo.orders.repository.PaymentRepository;
import com.itu.socialcom.demo.orders.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentRefundService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    public List<OrderPaymentDTO> getPaymentsByOrderId(Long orderId) {
        List<Payment> payments = paymentRepository.findPaymentsByOrderId(orderId);
        return payments.stream()
                .map(this::mapToPaymentDTO)
                .collect(Collectors.toList());
    }

    public List<RefundDTO> getRefundsByOrderId(Long orderId) {
        List<Refund> refunds = refundRepository.findByOrderId(orderId);
        return refunds.stream()
                .map(this::mapToRefundDTO)
                .collect(Collectors.toList());
    }

    private OrderPaymentDTO mapToPaymentDTO(Payment payment) {
        OrderPaymentDTO dto = new OrderPaymentDTO();
        dto.setId(payment.getId());
        dto.setSalesId(payment.getSalesId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethodId(payment.getPaymentMethodId());
        dto.setPaymentMethodName(payment.getPaymentMethodName());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }

    private RefundDTO mapToRefundDTO(Refund refund) {
        RefundDTO dto = new RefundDTO();
        dto.setId(refund.getId());
        dto.setOrderId(refund.getOrderId());
        dto.setAmount(refund.getAmount());
        dto.setSaleId(refund.getSaleId());
        dto.setCreatedAt(refund.getCreatedAt());
        return dto;
    }
}
