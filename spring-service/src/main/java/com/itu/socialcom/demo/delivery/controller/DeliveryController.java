package com.itu.socialcom.demo.delivery.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.delivery.dto.CreateDeliveryRequest;
import com.itu.socialcom.demo.delivery.dto.DeliveryResponse;
import com.itu.socialcom.demo.delivery.dto.UpdateDeliveryStatusRequest;
import com.itu.socialcom.demo.delivery.entity.Delivery;
import com.itu.socialcom.demo.delivery.service.DeliveryService;
import com.itu.socialcom.demo.orders.delivery.ApplicantService;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for delivery endpoints
 */
@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final TokenV2ServiceImpl tokenV2Service;
    @Autowired
    ApplicantService applicantService;

    @GetMapping("/{id_delivery}/assign/{id_applicant}")
    public ResponseEntity<ApiResponse> assignDelivery(@PathVariable("id_delivery") Long idDelivery, @PathVariable("id_applicant") Long idApplicant) {
        try {
            Delivery delivery = applicantService.assignDelivery(idDelivery, idApplicant);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData("Delivery assigned successfully");
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(new java.util.ArrayList<>(){
                {
                    add(e);
                }
            });
            return ResponseEntity.status(500).body(apiResponse);
        }

    }
    @Autowired
    public DeliveryController(DeliveryService deliveryService, TokenV2ServiceImpl tokenV2Service) {
        this.deliveryService = deliveryService;
        this.tokenV2Service = tokenV2Service;
    }

    /**
     * Get all deliveries for the authenticated user
     */
    @GetMapping
    public ResponseEntity<List<DeliveryResponse>> getAllDeliveries(@RequestHeader("Authorization") String token) {
        try {
            Seller seller = getCurrentSeller(token);
            List<Delivery> deliveries = deliveryService.getDeliveriesBySellerId(seller.getId());
            List<DeliveryResponse> responseList = deliveries.stream()
                .map(DeliveryResponse::fromEntity)
                .collect(Collectors.toList());
            return ResponseEntity.ok(responseList);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Get a specific delivery by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> getDeliveryById(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            Seller seller = getCurrentSeller(token);
            Optional<Delivery> delivery = deliveryService.getDeliveryByIdAndSellerId(id, seller.getId());

            return delivery.map(d -> ResponseEntity.ok(DeliveryResponse.fromEntity(d)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Create a new delivery
     */
    @PostMapping
    public ResponseEntity<DeliveryResponse> createDelivery(@RequestBody CreateDeliveryRequest request, @RequestHeader("Authorization") String token) {
        try {
            // Extract parameters from request
            Long shippingPointId = request.getShippingPointId();
            Long orderId = request.getOrderId();

            if (shippingPointId == null || orderId == null) {
                return ResponseEntity.badRequest().build();
            }

            Seller seller = getCurrentSeller(token);
            Delivery delivery = deliveryService.createDelivery(shippingPointId, orderId, seller.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(DeliveryResponse.fromEntity(delivery));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Update delivery status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<DeliveryResponse> updateDeliveryStatus(@PathVariable Long id, @RequestBody UpdateDeliveryStatusRequest request, @RequestHeader("Authorization") String token) {
        try {
            String status = request.getStatus();

            if (status == null) {
                return ResponseEntity.badRequest().build();
            }

            Seller seller = getCurrentSeller(token);
            Delivery delivery = deliveryService.updateDeliveryStatus(id, status, seller.getId());

            return ResponseEntity.ok(DeliveryResponse.fromEntity(delivery));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.badRequest().body(null);
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
