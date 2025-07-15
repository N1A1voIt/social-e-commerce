package com.itu.socialcom.demo.authentication.user;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserInfo;
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
    public String saveSeller(Map<String, Object> body, FirebaseToken decodedToken) throws Exception {
        String idToken = body.get("idToken").toString();
        String uid = decodedToken.getUid();
        String email = decodedToken.getEmail();

        // Get UserRecord from Firebase to access provider details
        UserRecord userRecord = firebaseAuth.getUser(uid);

        // Determine provider type
        String providerId = extractProviderId(userRecord);
        Seller.ProviderType providerType = Seller.ProviderType.valueOf(providerId);

        // Get name: use body name if present, else fallback to Firebase's displayName
        String name = (body.get("name") != null && !body.get("name").toString().isBlank())
                ? body.get("name").toString()
                : userRecord.getDisplayName(); // fallback to Firebase provider name

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }

        // Build and save Seller
        Seller seller = new Seller();
        seller.setFirebaseUid(uid);
        seller.setEmail(email);
        seller.setUsername(name);
        seller.setProvider(providerType);

        sellerRepository.save(seller);

        // Generate and return access token
        TokenV2 tokenV2 = tokenV2Service.createToken(seller.getId(), idToken, 30);
        return tokenV2.getToken();
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
