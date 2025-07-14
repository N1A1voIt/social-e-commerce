package com.itu.socialcom.demo.authentication.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;

    @Override
    @Transactional
    public Seller saveSeller(Seller seller) {
        return sellerRepository.save(seller);
    }

    @Override
    public Optional<Seller> getSellerById(Long id) {
        return sellerRepository.findById(id);
    }

    @Override
    public Optional<Seller> getSellerByEmail(String email) {
        return sellerRepository.findByEmail(email);
    }

    @Override
    public Optional<Seller> getSellerByUsername(String username) {
        return sellerRepository.findByUsername(username);
    }

    @Override
    public Optional<Seller> getSellerByFirebaseUid(String firebaseUid) {
        return sellerRepository.findByFirebaseUid(firebaseUid);
    }

    @Override
    public boolean existsByEmail(String email) {
        return sellerRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return sellerRepository.existsByUsername(username);
    }

    @Override
    @Transactional
    public void deleteSeller(Long id) {
        sellerRepository.deleteById(id);
    }
}
