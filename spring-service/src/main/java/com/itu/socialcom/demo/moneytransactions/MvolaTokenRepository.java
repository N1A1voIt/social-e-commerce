package com.itu.socialcom.demo.moneytransactions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MvolaTokenRepository extends JpaRepository<MvolaTokens,Long> {
    @Query(value = "SELECT * FROM mvola_tokens WHERE expiration_date < current_timestamp LIMIT 1", nativeQuery = true)
    Optional<MvolaTokens> findValid();

}
