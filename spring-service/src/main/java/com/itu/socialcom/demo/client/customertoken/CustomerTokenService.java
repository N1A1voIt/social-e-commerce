package com.itu.socialcom.demo.client.customertoken;

import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.client.customer.Customer;

import java.util.Optional;

public interface CustomerTokenService {
    CustomerToken saveToken(CustomerToken token);
    Optional<CustomerToken> getToken(String token);
    boolean isTokenValid(String token);
    CustomerToken createToken(Long idCustomer, String token, long expirationInMinutes);
    Optional<Long> findCustomerIdByToken(String token);
    Optional<Customer> findCustomerByToken(String token);
}
