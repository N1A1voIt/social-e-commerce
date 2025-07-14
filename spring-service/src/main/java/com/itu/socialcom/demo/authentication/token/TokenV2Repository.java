package com.itu.socialcom.demo.authentication.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenV2Repository extends JpaRepository<TokenV2, Long> {
    
    @Query("""
        SELECT t FROM TokenV2 t 
        WHERE t.token = :token AND 
              t.revoked = false AND 
              t.expired = false AND
              t.expiryDate > CURRENT_TIMESTAMP
    """)
    Optional<TokenV2> findValidToken(String token);
    
    @Query("""
        SELECT t.userId FROM TokenV2 t 
        WHERE t.token = :token AND 
              t.revoked = false AND 
              t.expired = false AND
              t.expiryDate > CURRENT_TIMESTAMP
    """)
    Optional<Long> findUserIdByToken(String token);
    
    @Query("""
        SELECT t FROM TokenV2 t 
        WHERE t.userId = :userId AND 
              t.revoked = false AND 
              t.expired = false AND
              t.expiryDate > CURRENT_TIMESTAMP
    """)
    List<TokenV2> findAllValidTokensByUser(Long userId);
    
    Optional<TokenV2> findByToken(String token);
    
    @Modifying
    @Query("UPDATE TokenV2 t SET t.revoked = true WHERE t.userId = :userId")
    void revokeAllUserTokens(@Param("userId") Long userId);
}
