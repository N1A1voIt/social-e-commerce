package com.itu.socialcom.demo.delivery.space.authentication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.itu.socialcom.demo.authentication.token.TokenV2;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.delivery.deliverydriver.DeliveryDriver;
import com.itu.socialcom.demo.delivery.deliverydriver.DeliveryDriverRepository;
import com.itu.socialcom.demo.delivery.space.authentication.token.DelivererToken;
import com.itu.socialcom.demo.delivery.space.authentication.token.DelivererTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthenticationService {
    @Autowired
    FirebaseAuth firebaseAuth;
    @Autowired
    DeliveryDriverRepository deliveryDriverRepository;
    @Autowired
    DelivererTokenRepository delivererTokenRepository;

    @Transactional
    public String signup (Map<String, Object> body, FirebaseToken decodedToken) throws FirebaseAuthException {
        if ( (double) body.get("minRange") < 0
                || (double) body.get("maxRange") <= 0
                || (double) body.get("minRange") > (double) body.get("maxRange")) {
            throw new IllegalArgumentException("Invalid range values");
        }
        String idToken = body.get("idToken").toString();
        String uid = decodedToken.getUid();
        String email = decodedToken.getEmail();

        // Get UserRecord from Firebase to access provider details
        UserRecord userRecord = firebaseAuth.getUser(uid);

        // Get name: use body name if present, else fallback to Firebase's displayName
        String name = (body.get("name") != null && !body.get("name").toString().isBlank())
                ? body.get("name").toString()
                : userRecord.getDisplayName(); // fallback to Firebase provider name

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        double minRange = (double) body.get("minRange");
        double maxRange = (double) body.get("maxRange");
        DeliveryDriver deliveryDriver = new DeliveryDriver();
        deliveryDriver.setFirebaseUid(uid);
        deliveryDriver.setEmail(email);
        deliveryDriver.setName(name);
        deliveryDriver.setMinRange(minRange);
        deliveryDriver.setMaxRange(maxRange);
        deliveryDriverRepository.save(deliveryDriver);
        DelivererToken delivererToken = new DelivererToken();
        delivererToken.setToken(idToken);
        delivererToken.setIdDeliverer(delivererToken.getIdDeliverer());
        delivererTokenRepository.save(delivererToken);
        return delivererToken.getToken();
    }
}
