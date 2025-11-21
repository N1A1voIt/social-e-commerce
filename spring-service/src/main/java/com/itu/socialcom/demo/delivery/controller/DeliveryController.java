package com.itu.socialcom.demo.delivery.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.delivery.dto.CreateDeliveryRequest;
import com.itu.socialcom.demo.delivery.dto.DeliveryResponse;
import com.itu.socialcom.demo.delivery.dto.UpdateDeliveryStatusRequest;
import com.itu.socialcom.demo.delivery.dto.AiDeliveryAssistanceRequest;
import com.itu.socialcom.demo.delivery.dto.AiDeliveryAssistanceResponse;
import com.itu.socialcom.demo.delivery.entity.Delivery;
import com.itu.socialcom.demo.delivery.service.DeliveryService;
import com.itu.socialcom.demo.delivery.service.AiDeliveryAssistanceService;
import com.itu.socialcom.demo.orders.delivery.ApplicantService;
import com.itu.socialcom.demo.orders.deliveryapplicants.DeliveryApplicant;
import com.itu.socialcom.demo.orders.deliveryapplicants.DeliveryApplicantRepository;
import com.itu.socialcom.demo.utils.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for delivery endpoints
 */
@RestController
@RequestMapping("/api/deliveries")
@Slf4j
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final TokenV2ServiceImpl tokenV2Service;
    private final AiDeliveryAssistanceService aiDeliveryAssistanceService;
    
    @Autowired
    ApplicantService applicantService;
    
    @Autowired
    DeliveryApplicantRepository deliveryApplicantRepository;

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
    public DeliveryController(DeliveryService deliveryService, TokenV2ServiceImpl tokenV2Service, AiDeliveryAssistanceService aiDeliveryAssistanceService) {
        this.deliveryService = deliveryService;
        this.tokenV2Service = tokenV2Service;
        this.aiDeliveryAssistanceService = aiDeliveryAssistanceService;
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

    /**
     * Request AI assistance for delivery driver selection (async with callback)
     */
    @PostMapping("/ai-assistance")
    public ResponseEntity<ApiResponse> requestAiAssistance(@RequestBody AiDeliveryAssistanceRequest request, @RequestHeader("Authorization") String token) {
        try {
            getCurrentSeller(token); // Verify authentication
            
            log.info("Received AI assistance request for delivery ID: {}", request.getDeliveryId());
            
            // Send request asynchronously - AI server will call back with result
            aiDeliveryAssistanceService.requestAiAssistanceAsync(request);
            
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(202); // Accepted - processing asynchronously
            apiResponse.setData(Map.of(
                "message", "AI assistance request accepted. Waiting for AI response...",
                "deliveryId", request.getDeliveryId()
            ));
            
            return ResponseEntity.accepted().body(apiResponse);
        } catch (IllegalStateException e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(401);
            apiResponse.setErrors(List.of(new Exception("Unauthorized")));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        } catch (Exception e) {
            log.error("Error in AI assistance endpoint", e);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Callback endpoint for AI server to POST the selected driver
     */
    @PostMapping("/ai-callback")
    public ResponseEntity<ApiResponse> aiCallback(@RequestBody AiDeliveryAssistanceResponse response) {
        try {
            log.info("Received AI callback for delivery ID: {}", response.getDeliveryId());
            log.info("Selected UID: {}", response.getUid());
            
            Long deliveryId = response.getDeliveryId();
            String selectedUid = response.getUid();
            
            if (deliveryId == null || selectedUid == null) {
                log.error("Invalid callback data - missing deliveryId or uid");
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(400);
                apiResponse.setErrors(List.of(new Exception("Missing deliveryId or uid")));
                return ResponseEntity.badRequest().body(apiResponse);
            }
            
            // Find the delivery applicant with the selected firebase UID
            List<DeliveryApplicant> applicants = deliveryApplicantRepository.findBydStatusAndIdDelivery("CALL_FOR_TENDERED", deliveryId);
            DeliveryApplicant selectedApplicant = applicants.stream()
                .filter(applicant -> selectedUid.equals(applicant.getFirebaseUid()))
                .findFirst()
                .orElse(null);
            
            if (selectedApplicant == null) {
                log.error("No applicant found with firebase UID: {} for delivery ID: {}", selectedUid, deliveryId);
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(404);
                apiResponse.setErrors(List.of(new Exception("Applicant not found with provided UID")));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
            }
            
            // Assign the driver
            log.info("Assigning driver {} (ID: {}) to delivery {}", selectedApplicant.getDriverName(), selectedApplicant.getIdDd(), deliveryId);
            applicantService.assignDelivery(deliveryId, selectedApplicant.getIdDd());
            
            // Mark as processed
            aiDeliveryAssistanceService.markAsProcessed(deliveryId);
            
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(Map.of(
                "message", "Driver assigned successfully",
                "deliveryId", deliveryId,
                "driverName", selectedApplicant.getDriverName(),
                "driverId", selectedApplicant.getIdDd()
            ));
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            log.error("Error in AI callback endpoint", e);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

}
