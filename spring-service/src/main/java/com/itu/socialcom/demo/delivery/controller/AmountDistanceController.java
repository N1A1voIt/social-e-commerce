package com.itu.socialcom.demo.delivery.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.delivery.entity.AmountDistance;
import com.itu.socialcom.demo.delivery.repository.AmountDistanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller for amount distance configuration endpoints
 */
@RestController
@RequestMapping("/api/amount-distances")
public class AmountDistanceController {

    private final AmountDistanceRepository amountDistanceRepository;
    private final TokenV2ServiceImpl tokenV2Service;

    @Autowired
    public AmountDistanceController(AmountDistanceRepository amountDistanceRepository, TokenV2ServiceImpl tokenV2Service) {
        this.amountDistanceRepository = amountDistanceRepository;
        this.tokenV2Service = tokenV2Service;
    }

    /**
     * Get all amount distance configurations for the authenticated user
     */
    @GetMapping
    public ResponseEntity<List<AmountDistance>> getAllAmountDistances(@RequestHeader("Authorization") String token) {
        try {
            Seller seller = getCurrentSeller(token);
            List<AmountDistance> amountDistances = amountDistanceRepository.findByUserId(seller.getId());
            return ResponseEntity.ok(amountDistances);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Get amount distance configurations for a specific managed page
     */
    @GetMapping("/managed-page/{managedPageId}")
    public ResponseEntity<List<AmountDistance>> getAmountDistancesByManagedPage(@PathVariable Long managedPageId, @RequestHeader("Authorization") String token) {
        try {
            // Authentication is handled by getCurrentSeller
            getCurrentSeller(token);
            List<AmountDistance> amountDistances = amountDistanceRepository.findByManagedPageId(managedPageId);
            return ResponseEntity.ok(amountDistances);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Create a new amount distance configuration
     */
    @PostMapping
    public ResponseEntity<AmountDistance> createAmountDistance(@RequestBody AmountDistance amountDistance, @RequestHeader("Authorization") String token) {
        try {
            Seller seller = getCurrentSeller(token);

//            if (amountDistance.getUserId() != null) {
                amountDistance.setUserId(seller.getId());
//            }

            if (amountDistance.getUserId() == null && amountDistance.getManagedPageId() == null) {
                return ResponseEntity.badRequest().build();
            }

            AmountDistance savedAmountDistance = amountDistanceRepository.save(amountDistance);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAmountDistance);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Update an existing amount distance configuration
     */
    @PutMapping("/{id}")
    public ResponseEntity<AmountDistance> updateAmountDistance(@PathVariable Long id, @RequestBody AmountDistance amountDistance, @RequestHeader("Authorization") String token) {
        try {
            Seller seller = getCurrentSeller(token);

            Optional<AmountDistance> existingAmountDistanceOpt = amountDistanceRepository.findById(id);
            if (existingAmountDistanceOpt.isPresent()) {
                AmountDistance existingAmountDistance = existingAmountDistanceOpt.get();

                // Check if the user has permission to update this configuration
                if (existingAmountDistance.getUserId() != null && !existingAmountDistance.getUserId().equals(seller.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }

                // Update fields
                existingAmountDistance.setPricePerDistance(amountDistance.getPricePerDistance());

                // If this is a user-specific configuration, ensure the user ID is the current seller
//                if (amountDistance.getUserId() != null) {
                    existingAmountDistance.setUserId(seller.getId());
//                }

                // Validate that at least one of userId or managedPageId is set
                if (existingAmountDistance.getUserId() == null && existingAmountDistance.getManagedPageId() == null) {
                    return ResponseEntity.badRequest().build();
                }

                AmountDistance updatedAmountDistance = amountDistanceRepository.save(existingAmountDistance);
                return ResponseEntity.ok(updatedAmountDistance);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Delete an amount distance configuration
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAmountDistance(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            Seller seller = getCurrentSeller(token);

            Optional<AmountDistance> amountDistanceOpt = amountDistanceRepository.findById(id);
            if (amountDistanceOpt.isPresent()) {
                AmountDistance amountDistance = amountDistanceOpt.get();

                // Check if the user has permission to delete this configuration
                if (amountDistance.getUserId() != null && !amountDistance.getUserId().equals(seller.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }

                amountDistanceRepository.deleteById(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Helper method to get the current authenticated seller from the token
     */
    private Seller getCurrentSeller(String token) {
        return tokenV2Service.findSellerByToken(token)
                .orElseThrow(() -> new IllegalStateException("Authenticated user is not a valid seller"));
    }
}
