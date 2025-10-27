package com.itu.socialcom.demo.shipping.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.shipping.entity.ShippingPoint;
import com.itu.socialcom.demo.shipping.service.ShippingPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller for shipping points endpoints
 */
@RestController
@RequestMapping("/api/shipping-points")
public class ShippingPointController {

    private final ShippingPointService shippingPointService;
    private final TokenV2ServiceImpl tokenV2Service;

    @Autowired
    public ShippingPointController(ShippingPointService shippingPointService, TokenV2ServiceImpl tokenV2Service) {
        this.shippingPointService = shippingPointService;
        this.tokenV2Service = tokenV2Service;
    }

    /**
     * Get all shipping points for the authenticated user
     */
    @GetMapping
    public ResponseEntity<List<ShippingPoint>> getAllShippingPoints(@RequestHeader("Authorization") String token) {
        Seller seller = getCurrentSeller(token);
        List<ShippingPoint> shippingPoints = shippingPointService.getShippingPointsBySellerId(seller.getId());
        return ResponseEntity.ok(shippingPoints);
    }

    /**
     * Get a specific shipping point by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShippingPoint> getShippingPointById(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        Seller seller = getCurrentSeller(token);
        Optional<ShippingPoint> shippingPoint = shippingPointService.getShippingPointById(id);

        if (shippingPoint.isPresent()) {
            // Verify that the shipping point belongs to the seller through managed page
            List<ShippingPoint> sellerShippingPoints = shippingPointService.getShippingPointsBySellerId(seller.getId());
            boolean belongsToSeller = sellerShippingPoints.stream()
                .anyMatch(sp -> sp.getId().equals(id));

            if (belongsToSeller) {
                return ResponseEntity.ok(shippingPoint.get());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a new shipping point
     */
    @PostMapping
    public ResponseEntity<ShippingPoint> createShippingPoint(@RequestBody ShippingPoint shippingPoint, @RequestHeader("Authorization") String token) {
        try {
            Seller seller = getCurrentSeller(token);
            ShippingPoint createdShippingPoint = shippingPointService.createShippingPoint(shippingPoint, seller.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdShippingPoint);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update an existing shipping point
     */
    @PutMapping("/{id}")
    public ResponseEntity<ShippingPoint> updateShippingPoint(@PathVariable Long id, @RequestBody ShippingPoint shippingPoint, @RequestHeader("Authorization") String token) {
        try {
            Seller seller = getCurrentSeller(token);
            ShippingPoint updatedShippingPoint = shippingPointService.updateShippingPoint(id, shippingPoint, seller.getId());
            return ResponseEntity.ok(updatedShippingPoint);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
    }

    /**
     * Delete a shipping point
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShippingPoint(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            Seller seller = getCurrentSeller(token);
            shippingPointService.deleteShippingPoint(id, seller.getId());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
    }

    /**
     * Get shipping points for a specific managed page
     * If managedPageId is -1, returns all shipping points for the user
     */
    @GetMapping("/managed-page/{managedPageId}")
    public ResponseEntity<List<ShippingPoint>> getShippingPointsByManagedPage(@PathVariable Long managedPageId, @RequestHeader("Authorization") String token) {
        Seller seller = getCurrentSeller(token);

        List<ShippingPoint> shippingPoints;

        // Special case: if managedPageId is -1, return all shipping points for the seller
        if (managedPageId == -1) {
            shippingPoints = shippingPointService.getShippingPointsBySellerId(seller.getId());
        } else {
            // Verify that the managed page belongs to the seller
            shippingPoints = shippingPointService.getShippingPointsByManagedPageId(managedPageId);

            // Filter to only include shipping points for managed pages owned by the seller
            List<ShippingPoint> sellerShippingPoints = shippingPointService.getShippingPointsBySellerId(seller.getId());
            boolean managedPageBelongsToSeller = sellerShippingPoints.stream()
                .anyMatch(sp -> sp.getManagedPageId().equals(managedPageId));

            if (!managedPageBelongsToSeller) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        // Format place names as "{origin} -> {place_name}"
        shippingPoints.forEach(sp -> {
            if (sp.getOrigin() != null && !sp.getOrigin().isEmpty()) {
                sp.setPlaceName(sp.getOrigin() + " -> " + sp.getPlaceName());
            }
        });

        return ResponseEntity.ok(shippingPoints);
    }

    /**
     * Helper method to get the current authenticated seller from the token
     */
    private Seller getCurrentSeller(@RequestHeader("Authorization") String token) {
        return tokenV2Service.findSellerByToken(token)
            .orElseThrow(() -> new IllegalStateException("Authenticated user is not a valid seller"));
    }
}
