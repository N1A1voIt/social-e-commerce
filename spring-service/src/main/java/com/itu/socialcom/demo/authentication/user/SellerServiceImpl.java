package com.itu.socialcom.demo.authentication.user;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.itu.socialcom.demo.authentication.token.TokenV2;
import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;
    private final TokenV2ServiceImpl tokenV2Service;
    private final FirebaseAuth firebaseAuth;
    @Override
    @Transactional
    public Seller saveSeller(Seller seller) {
        return sellerRepository.save(seller);
    }
    @Override
    @Transactional
    public String saveSeller(Map<String, Object> body, FirebaseToken decodedToken) throws Exception{
        String idToken = body.get("idToken").toString();
        String name = body.get("name") != null ? body.get("name").toString() : null;
        String uid = decodedToken.getUid();
        String email = decodedToken.getEmail();
        Seller seller = new Seller();
        seller.setFirebaseUid(uid);
        seller.setEmail(email);
        seller.setUsername(name);
        UserRecord userRecord = firebaseAuth.getUser(uid);
        System.out.println(decodedToken.getTenantId());
        //seller.setProvider(userRecord.getProviderId());

        sellerRepository.save(seller);
        TokenV2 tokenV2 = tokenV2Service.createToken(seller.getId(), idToken, 30);
        return tokenV2.getToken();
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
