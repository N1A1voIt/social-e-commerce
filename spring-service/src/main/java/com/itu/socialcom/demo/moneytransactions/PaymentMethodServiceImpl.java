package com.itu.socialcom.demo.moneytransactions;

import com.itu.socialcom.demo.authentication.user.phonenumber.dto.PaymentMethodResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> getAllPaymentMethods() {
        return paymentMethodRepository.findAll().stream()
                .map(PaymentMethodResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentMethodResponse getById(Long id) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment method not found with id: " + id));
        return PaymentMethodResponse.fromEntity(paymentMethod);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentMethodResponse getByName(String name) {
        PaymentMethod paymentMethod = paymentMethodRepository.findByPaymentName(name)
                .orElseThrow(() -> new RuntimeException("Payment method not found with name: " + name));
        return PaymentMethodResponse.fromEntity(paymentMethod);
    }
}

