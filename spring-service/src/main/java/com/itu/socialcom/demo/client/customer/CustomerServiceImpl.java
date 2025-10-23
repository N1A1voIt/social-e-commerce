package com.itu.socialcom.demo.client.customer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserRecord;
import com.itu.socialcom.demo.client.customertoken.CustomerToken;
import com.itu.socialcom.demo.client.customertoken.CustomerTokenServiceImpl;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService{

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CustomerTokenServiceImpl customerTokenServiceImpl;
    @Autowired
    private FirebaseAuth firebaseAuth;
    @Override
    @Transactional
    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }
    @Override
    @Transactional
    public String saveCustomer(Map<String, Object> body, FirebaseToken decodedToken) throws Exception {
        String idToken = body.get("idToken").toString();
        String uid = decodedToken.getUid();
        String email = decodedToken.getEmail();

        // Get UserRecord from Firebase to access provider details
        UserRecord userRecord = firebaseAuth.getUser(uid);

        // Determine provider type
//        String providerId = extractProviderId(userRecord);
//        Customer.ProviderType providerType = Customer.ProviderType.valueOf(providerId);

        // Get name: use body name if present, else fallback to Firebase's displayName
        String name = (body.get("name") != null && !body.get("name").toString().isBlank())
                ? body.get("name").toString()
                : userRecord.getDisplayName(); // fallback to Firebase provider name

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }

        Customer customer = new Customer();
        customer.setFirebaseUid(uid);
        customer.setEmail(email);
        customer.setUsername(name);
//        customer.setProvider(providerType);

        customerRepository.save(customer);

        // Generate and return access token
        CustomerToken token = customerTokenServiceImpl.createToken(customer.getIdCustomer(), idToken, 30);
        return token.getToken();
    }


    private static String extractProviderId(UserRecord userRecord) throws Exception {
        String providerId = "firebase"; // fallback
        for (UserInfo userInfo : userRecord.getProviderData()) {
            if (!userInfo.getProviderId().equals("firebase")) {
                providerId = userInfo.getProviderId(); // e.g., "google.com", "password"
                break;
            }
        }
        String enumName = switch (providerId) {
            case "google.com" -> "google";
            case "facebook.com" -> "facebook";
            case "x.com" -> "X";
            case "password" -> "basic";
            case "github.com" -> "github";
            default -> throw new Exception("Unsupported provider: " + providerId);
        };

        return enumName;
    }



    @Override
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Override
    public Optional<Customer> getCustomerByUsername(String username) {
        return customerRepository.findByUsername(username);
    }

    @Override
    public Optional<Customer> getCustomerByFirebaseUid(String firebaseUid) {
        return customerRepository.findByFirebaseUid(firebaseUid);
    }

    @Override
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return customerRepository.existsByUsername(username);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
}
