package com.itu.socialcom.demo.client.customertoken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CustomerTokenRepository extends JpaRepository<CustomerToken,Long> {
    @Query("""
        SELECT t FROM CustomerToken t 
        WHERE t.token = :token AND 
              t.expiryDate > CURRENT_TIMESTAMP
    """)
    Optional<CustomerToken> findValidToken(String token);

    @Query("""
        SELECT t.idCustomer FROM CustomerToken t 
        WHERE t.token = :token AND 
              t.expiryDate > CURRENT_TIMESTAMP
    """)
    Optional<Long> findUserIdByToken(String token);

    List<CustomerToken> findAllValidTokensByIdCustomer(Long idCustomer);

    Optional<CustomerToken> findByToken(String token);
}
