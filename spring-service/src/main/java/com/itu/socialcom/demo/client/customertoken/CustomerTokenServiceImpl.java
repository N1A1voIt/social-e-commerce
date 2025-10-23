package com.itu.socialcom.demo.client.customertoken;

import com.itu.socialcom.demo.client.customer.Customer;
import com.itu.socialcom.demo.client.customer.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CustomerTokenServiceImpl implements CustomerTokenService {
    @Autowired
    private CustomerTokenRepository tokenV2Repository;
    @Autowired
    private CustomerRepository customerRepository;

    @Override
    @Transactional
    public CustomerToken saveToken(CustomerToken token) {
        return tokenV2Repository.save(token);
    }

    @Override
    public Optional<CustomerToken> getToken(String token) {
        return tokenV2Repository.findByToken(token);
    }

    @Override
    public boolean isTokenValid(String token) {
        return tokenV2Repository.findValidToken(token)
                .map(t ->  t.getExpiryDate().isAfter(LocalDateTime.now()))
                .orElse(false);
    }


    @Override
    @Transactional
    public CustomerToken createToken(Long userId, String token, long expirationInMinutes) {
        CustomerToken tokenV2 = new CustomerToken();
        tokenV2.setToken(token);
        tokenV2.setIdCustomer(userId);
        tokenV2.setExpiryDate(LocalDateTime.now().plusMinutes(expirationInMinutes));

        return saveToken(tokenV2);
    }

    @Override
    public Optional<Long> findCustomerIdByToken(String token) {
        return tokenV2Repository.findUserIdByToken(token);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findCustomerByToken(String token) {
        return findCustomerIdByToken(token)
                .flatMap(customerRepository::findById);
    }
}
