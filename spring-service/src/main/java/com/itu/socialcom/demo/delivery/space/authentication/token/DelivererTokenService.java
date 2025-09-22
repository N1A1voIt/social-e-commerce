package com.itu.socialcom.demo.delivery.space.authentication.token;

import com.itu.socialcom.demo.delivery.deliverydriver.DeliveryDriver;
import com.itu.socialcom.demo.delivery.deliverydriver.DeliveryDriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DelivererTokenService {
    @Autowired
    DelivererTokenRepository delivererTokenRepository;
    @Autowired
    DeliveryDriverRepository deliveryDriverRepository;
    public boolean isTokenValid(String token) {
        return delivererTokenRepository.findValidToken(token)
                .map(t ->  t.getExpiryDate().isAfter(LocalDateTime.now()))
                .orElse(false);
    }
    public DeliveryDriver findByToken(String token) {
        DelivererToken delivererToken = delivererTokenRepository.findValidToken(token).orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));
        DeliveryDriver deliveryDriver = deliveryDriverRepository.findById(delivererToken.getIdDeliverer().intValue()).orElseThrow(() -> new IllegalArgumentException("Delivery driver not found"));
        return deliveryDriver;
    }
}
