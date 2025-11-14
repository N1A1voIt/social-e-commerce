package com.itu.socialcom.demo.delivery.space.profile;

import com.itu.socialcom.demo.delivery.deliverydriver.DeliveryDriver;
import com.itu.socialcom.demo.delivery.deliverydriver.DeliveryDriverRepository;
import com.itu.socialcom.demo.delivery.space.authentication.token.DelivererTokenService;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery/profile")
public class DeliveryProfileController {

    @Autowired
    private DelivererTokenService delivererTokenService;

    @Autowired
    private DeliveryDriverRepository deliveryDriverRepository;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentDeliveryDriver(@RequestHeader("Authorization") String authHeader) {
        ApiResponse response = new ApiResponse();
        
        try {
            String token = authHeader.replace("Bearer ", "");
            DeliveryDriver deliveryDriver = delivererTokenService.findByToken(token);
            
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("id", deliveryDriver.getId());
            profileData.put("name", deliveryDriver.getName());
            profileData.put("email", deliveryDriver.getEmail());
            profileData.put("phoneNumber", deliveryDriver.getPhoneNumber());
            profileData.put("minRange", deliveryDriver.getMinRange());
            profileData.put("maxRange", deliveryDriver.getMaxRange());
            
            response.setStatus(HttpStatus.OK.value());
            response.setData(profileData);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setData(null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setData(null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> updates) {
        ApiResponse response = new ApiResponse();
        
        try {
            String token = authHeader.replace("Bearer ", "");
            DeliveryDriver deliveryDriver = delivererTokenService.findByToken(token);
            
            // Update phone number if provided
            if (updates.containsKey("phoneNumber")) {
                deliveryDriver.setPhoneNumber(updates.get("phoneNumber").toString());
            }
            
            // Update min range if provided
            if (updates.containsKey("minRange")) {
                deliveryDriver.setMinRange(Double.parseDouble(updates.get("minRange").toString()));
            }
            
            // Update max range if provided
            if (updates.containsKey("maxRange")) {
                deliveryDriver.setMaxRange(Double.parseDouble(updates.get("maxRange").toString()));
            }
            
            // Save updated profile
            deliveryDriver = deliveryDriverRepository.save(deliveryDriver);
            
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("id", deliveryDriver.getId());
            profileData.put("name", deliveryDriver.getName());
            profileData.put("email", deliveryDriver.getEmail());
            profileData.put("phoneNumber", deliveryDriver.getPhoneNumber());
            profileData.put("minRange", deliveryDriver.getMinRange());
            profileData.put("maxRange", deliveryDriver.getMaxRange());
            
            response.setStatus(HttpStatus.OK.value());
            response.setData(profileData);
            response.setErrors(List.of());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setData(null);
            response.setErrors(List.of(e));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setData(null);
            response.setErrors(List.of(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
