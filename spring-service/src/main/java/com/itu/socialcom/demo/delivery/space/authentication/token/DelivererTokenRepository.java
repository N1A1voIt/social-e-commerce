package com.itu.socialcom.demo.delivery.space.authentication.token;

import com.itu.socialcom.demo.authentication.token.TokenV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DelivererTokenRepository extends JpaRepository<DelivererToken,Long> {
    @Query("""
        SELECT t FROM DelivererToken t 
        WHERE t.token = :token AND 
              t.expiryDate > CURRENT_TIMESTAMP
    """)
    Optional<DelivererToken> findValidToken(String token);
}
