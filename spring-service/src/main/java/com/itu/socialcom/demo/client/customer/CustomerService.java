package com.itu.socialcom.demo.client.customer;

import com.google.firebase.auth.FirebaseToken;

import java.util.Map;
import java.util.Optional;

public interface CustomerService {
    Customer saveCustomer(Customer customer);
    public String saveCustomer(Map<String, Object> body, FirebaseToken decodedToken) throws Exception;
    Optional<Customer> getCustomerById(Long id);
    Optional<Customer> getCustomerByEmail(String email);
    Optional<Customer> getCustomerByUsername(String username);
    Optional<Customer> getCustomerByFirebaseUid(String firebaseUid);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    void deleteCustomer(Long id);
}
