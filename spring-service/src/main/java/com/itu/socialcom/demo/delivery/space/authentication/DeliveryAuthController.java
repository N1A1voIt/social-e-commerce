package com.itu.socialcom.demo.delivery.space.authentication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.protobuf.Api;
import com.itu.socialcom.demo.authentication.TokenDTO;
import com.itu.socialcom.demo.authentication.token.TokenV2;
import com.itu.socialcom.demo.delivery.deliverydriver.DeliveryDriver;
import com.itu.socialcom.demo.delivery.deliverydriver.DeliveryDriverRepository;
import com.itu.socialcom.demo.delivery.space.authentication.token.DelivererToken;
import com.itu.socialcom.demo.delivery.space.authentication.token.DelivererTokenRepository;
import com.itu.socialcom.demo.delivery.space.authentication.token.DelivererTokenService;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping(("/api/delivery/auth"))
public class DeliveryAuthController {
    @Autowired
    private FirebaseAuth firebaseAuth;
    @Autowired
    AuthenticationService deliveryAuthService;
    @Autowired
    DeliveryDriverRepository deliveryDriverRepository;
    @Autowired
    DelivererTokenRepository delivererTokenRepository;
    @Autowired
    DelivererTokenService delivererTokenService;
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, Object> body) {
        if (body == null || !body.containsKey("idToken")) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "idToken is required"));
        }
        try {
            String idToken = body.get("idToken").toString();
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            String token = deliveryAuthService.signup(body,decodedToken);
//            ApiResponse apiResponse = new ApiResponse();
//            apiResponse.setData(token);
//            apiResponse.setStatus(200);
//            apiResponse.setErrors(Collections.emptyList());
            TokenDTO tokenDTO = new TokenDTO();
            tokenDTO.setToken(token);
            return ResponseEntity.ok(tokenDTO);
        } catch (Exception e) {
            e.printStackTrace();
//            ApiResponse apiResponse = new ApiResponse();
//            apiResponse.setData(null);
//            apiResponse.setStatus(401);
//            apiResponse.setErrors(List.of(e));
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
    public ResponseEntity<ApiResponse> signin(@RequestBody Map<String, Object> body) {
        if (body == null || !body.containsKey("idToken")) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setData(null);
            apiResponse.setStatus(400);
            Exception e = new Exception("idToken is required");
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.badRequest().body(apiResponse);
        }
        try {
            String idToken = body.get("idToken").toString();
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            String uid = decodedToken.getUid();

            Optional<DeliveryDriver> existingDeliveryDriver = deliveryDriverRepository.findDeliveryDriverByFirebaseUid(uid);
            if (existingDeliveryDriver.isEmpty()) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setData(null);
                apiResponse.setStatus(404);
                Exception e = new Exception("User not found. Please sign up first.");
                apiResponse.setErrors(List.of(e));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(apiResponse);
            }
            DelivererToken delivererToken = new DelivererToken();
            delivererToken.setToken(idToken);
            delivererToken.setIdDeliverer(existingDeliveryDriver.get().getId());
            delivererTokenRepository.save(delivererToken);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setData(delivererToken.getToken());
            apiResponse.setStatus(200);
            apiResponse.setErrors(Collections.emptyList());
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setData(null);
            apiResponse.setStatus(401);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }
    }
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody TokenDTO tokenDTO) {
        if (tokenDTO == null || tokenDTO.getToken() == null || tokenDTO.getToken().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Token is required"));
        }

        try {
            boolean isValid = delivererTokenService.isTokenValid(tokenDTO.getToken().replace("Bearer ", ""));

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
