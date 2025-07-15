package com.itu.socialcom.demo.authentication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.itu.socialcom.demo.authentication.token.TokenV2;
import com.itu.socialcom.demo.authentication.token.TokenV2Service;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.authentication.user.SellerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final FirebaseAuth firebaseAuth;
    @Autowired
    private SellerServiceImpl sellerService;
    @Autowired
    private SellerServiceImpl sellerServiceImpl;
    @Autowired
    private TokenV2Service tokenV2Service;

    public AuthController(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @PostMapping("/signup")
    @Transactional
    public ResponseEntity<?> signup(@RequestBody Map<String, Object> body) {
        if (body == null || !body.containsKey("idToken")) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "idToken is required"));
        }
        try {
            String idToken = body.get("idToken").toString();
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            String token = sellerServiceImpl.saveSeller(body,decodedToken);
            TokenDTO tokenDTO = new TokenDTO();
            tokenDTO.setToken(token);
            return ResponseEntity.ok(tokenDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new HashMap<String, String>() {{
                        put("error", "Invalid token");
                        put("message", e.getMessage());
                    }}
                    );
        }

    }

    @PostMapping("/signin")
    @Transactional
    public ResponseEntity<?> signin(@RequestBody Map<String, Object> body) {
        if (body == null || !body.containsKey("idToken")) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "idToken is required"));
        }
        try {
            String idToken = body.get("idToken").toString();
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            String uid = decodedToken.getUid();

            // Check if user exists
            Optional<Seller> existingSeller = sellerServiceImpl.getSellerByFirebaseUid(uid);
            if (existingSeller.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "User not found. Please sign up first."));
            }

            // Create token for existing user
            TokenV2 tokenV2 = tokenV2Service.createToken(existingSeller.get().getId(), idToken, 30);

            TokenDTO tokenDTO = new TokenDTO();
            tokenDTO.setToken(tokenV2.getToken());
            return ResponseEntity.ok(tokenDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new HashMap<String, String>() {{
                              put("error", "Invalid token");
                              put("message", e.getMessage());
                          }}
                    );
        }
    }
}
