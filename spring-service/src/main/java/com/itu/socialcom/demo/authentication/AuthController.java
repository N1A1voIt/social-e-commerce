package com.itu.socialcom.demo.authentication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.itu.socialcom.demo.authentication.token.TokenV2;
import com.itu.socialcom.demo.authentication.token.TokenV2Service;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.authentication.user.SellerServiceImpl;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.itu.socialcom.demo.authentication.token.TokenV2Service;

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

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getAccountInfo(@RequestHeader("Authorization") String authHeader) {
        ApiResponse response = new ApiResponse();
        
        try {
            // Extract token from header (format: "Bearer <token>")
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setErrors(Collections.singletonList(new Exception("Invalid or missing Authorization header")));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            
            // Find seller by token
            Optional<Seller> sellerOpt = tokenV2Service.findSellerByToken(token);
            
            if (sellerOpt.isPresent()) {
                response.setStatus(HttpStatus.OK.value());
                response.setData(sellerOpt.get());
                return ResponseEntity.ok(response);
            } else {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setErrors(Collections.singletonList(new Exception("Invalid or expired token")));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setErrors(Collections.singletonList(e));
            return ResponseEntity.internalServerError().body(response);
        }
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

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody TokenDTO tokenDTO) {
        if (tokenDTO == null || tokenDTO.getToken() == null || tokenDTO.getToken().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Token is required"));
        }

        try {
            boolean isValid = tokenV2Service.isTokenValid(tokenDTO.getToken().replace("Bearer ", ""));

            ApiResponse response = new ApiResponse();
            response.setStatus(isValid ? HttpStatus.OK.value() : HttpStatus.UNAUTHORIZED.value());

            Map<String, Boolean> data = new HashMap<>();
            data.put("valid", isValid);
            response.setData(data);

            return ResponseEntity.status(isValid ? HttpStatus.OK : HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new HashMap<String, String>() {{
                        put("error", "Error validating token");
                        put("message", e.getMessage());
                    }});
        }
    }
}
