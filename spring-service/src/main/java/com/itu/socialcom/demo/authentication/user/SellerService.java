package com.itu.socialcom.demo.authentication.user;

import java.util.Optional;

public interface SellerService {
    Seller saveSeller(Seller seller);
    Optional<Seller> getSellerById(Long id);
    Optional<Seller> getSellerByEmail(String email);
    Optional<Seller> getSellerByUsername(String username);
    Optional<Seller> getSellerByFirebaseUid(String firebaseUid);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    void deleteSeller(Long id);
}
