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
import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController("/api/delivery/auth")
public class DeliveryAuthController {
    @Autowired
    private FirebaseAuth firebaseAuth;
    @Autowired
    AuthenticationService deliveryAuthService;
    @Autowired
    DeliveryDriverRepository deliveryDriverRepository;
    @Autowired
    DelivererTokenRepository delivererTokenRepository;
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@RequestBody Map<String, Object> body) {
        if (body == null || !body.containsKey("idToken")) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setData(null);
            apiResponse.setStatus(401);
            Exception e = new Exception("idToken is required");
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.badRequest().body(apiResponse);
        }
        try {
            String idToken = body.get("idToken").toString();
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            String token = deliveryAuthService.signup(body,decodedToken);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setData(token);
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
}
