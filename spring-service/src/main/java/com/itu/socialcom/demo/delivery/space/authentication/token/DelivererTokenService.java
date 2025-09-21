package com.itu.socialcom.demo.delivery.space.authentication.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DelivererTokenService {
    @Autowired
    DelivererTokenRepository delivererTokenRepository;
    public boolean isTokenValid(String token) {
        return delivererTokenRepository.findValidToken(token)
                .map(t ->  t.getExpiryDate().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

}
