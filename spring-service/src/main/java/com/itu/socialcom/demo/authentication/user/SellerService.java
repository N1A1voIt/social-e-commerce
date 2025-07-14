package com.itu.socialcom.demo.authentication.user;

import com.google.firebase.auth.FirebaseToken;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public interface SellerService {
    Seller saveSeller(Seller seller);
    public String saveSeller(Map<String, Object> body, FirebaseToken decodedToken) throws Exception;
    Optional<Seller> getSellerById(Long id);
    Optional<Seller> getSellerByEmail(String email);
    Optional<Seller> getSellerByUsername(String username);
    Optional<Seller> getSellerByFirebaseUid(String firebaseUid);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    void deleteSeller(Long id);
}
