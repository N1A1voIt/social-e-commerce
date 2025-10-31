package com.itu.socialcom.demo.authentication.user.phonenumber.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.authentication.user.SellerService;
import com.itu.socialcom.demo.authentication.user.phonenumber.SellerPhoneNumberService;
import com.itu.socialcom.demo.authentication.user.phonenumber.dto.SellerPhoneNumberRequest;
import com.itu.socialcom.demo.authentication.user.phonenumber.dto.SellerPhoneNumberResponse;
import com.itu.socialcom.demo.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/sellers/phone-numbers")
public class SellerPhoneNumberController {

    @Autowired
    private SellerPhoneNumberService sellerPhoneNumberService;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private FirebaseAuth firebaseAuth;

    /**
     * Helper method to extract seller from Firebase token
     */
    private Seller getSellerFromToken(String authHeader) throws Exception {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid or missing Authorization header");
        }

        String idToken = authHeader.substring(7);
        FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
        String firebaseUid = decodedToken.getUid();

        return sellerService.getSellerByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("Seller not found"));
    }

    /**
     * Create or update a phone number configuration for a payment method
     * POST /api/sellers/phone-numbers
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createOrUpdatePhoneNumber(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody SellerPhoneNumberRequest request) {
        ApiResponse response = new ApiResponse();

        try {
            Seller seller = getSellerFromToken(authHeader);
            SellerPhoneNumberResponse result = sellerPhoneNumberService.createOrUpdate(seller.getId(), request);

            response.setStatus(HttpStatus.CREATED.value());
            response.setData(result);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setErrors(Collections.singletonList(e));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get all phone number configurations for the authenticated seller
     * GET /api/sellers/phone-numbers
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getAllPhoneNumbers(@RequestHeader("Authorization") String authHeader) {
        ApiResponse response = new ApiResponse();

        try {
            Seller seller = getSellerFromToken(authHeader);
            List<SellerPhoneNumberResponse> phoneNumbers = sellerPhoneNumberService.getAllBySellerId(seller.getId());

            response.setStatus(HttpStatus.OK.value());
            response.setData(phoneNumbers);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setErrors(Collections.singletonList(e));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get a specific phone number configuration by ID
     * GET /api/sellers/phone-numbers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getPhoneNumberById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        ApiResponse response = new ApiResponse();

        try {
            Seller seller = getSellerFromToken(authHeader);
            SellerPhoneNumberResponse phoneNumber = sellerPhoneNumberService.getById(id);

            // Verify the phone number belongs to the authenticated seller
            if (!phoneNumber.getSellerId().equals(seller.getId())) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            response.setStatus(HttpStatus.OK.value());
            response.setData(phoneNumber);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setErrors(Collections.singletonList(e));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get phone number configuration for a specific payment method
     * GET /api/sellers/phone-numbers/payment-method/{paymentMethodId}
     */
    @GetMapping("/payment-method/{paymentMethodId}")
    public ResponseEntity<ApiResponse> getPhoneNumberByPaymentMethod(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long paymentMethodId) {
        ApiResponse response = new ApiResponse();

        try {
            Seller seller = getSellerFromToken(authHeader);
            SellerPhoneNumberResponse phoneNumber = sellerPhoneNumberService
                    .getBySellerAndPaymentMethod(seller.getId(), paymentMethodId);

            response.setStatus(HttpStatus.OK.value());
            response.setData(phoneNumber);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setErrors(Collections.singletonList(e));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Update an existing phone number configuration
     * PUT /api/sellers/phone-numbers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updatePhoneNumber(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody SellerPhoneNumberRequest request) {
        ApiResponse response = new ApiResponse();

        try {
            Seller seller = getSellerFromToken(authHeader);

            // Verify the phone number belongs to the authenticated seller
            SellerPhoneNumberResponse existing = sellerPhoneNumberService.getById(id);
            if (!existing.getSellerId().equals(seller.getId())) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            SellerPhoneNumberResponse result = sellerPhoneNumberService.createOrUpdate(seller.getId(), request);

            response.setStatus(HttpStatus.OK.value());
            response.setData(result);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setErrors(Collections.singletonList(e));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Delete a phone number configuration
     * DELETE /api/sellers/phone-numbers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deletePhoneNumber(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        ApiResponse response = new ApiResponse();

        try {
            Seller seller = getSellerFromToken(authHeader);
            sellerPhoneNumberService.delete(id, seller.getId());

            response.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setErrors(Collections.singletonList(e));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}

