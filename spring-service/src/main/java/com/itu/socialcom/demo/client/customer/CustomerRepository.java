package com.itu.socialcom.demo.client.customer;

import com.itu.socialcom.demo.authentication.user.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer,Long> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByFirebaseUid(String firebaseUid);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
