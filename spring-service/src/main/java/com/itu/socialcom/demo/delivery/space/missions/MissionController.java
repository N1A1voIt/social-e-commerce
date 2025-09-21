package com.itu.socialcom.demo.delivery.space.missions;

import com.itu.socialcom.demo.delivery.deliverydriver.DeliveryDriver;
import com.itu.socialcom.demo.delivery.entity.Delivery;
import com.itu.socialcom.demo.delivery.repository.DeliveryRepository;
import com.itu.socialcom.demo.delivery.service.DeliveryService;
import com.itu.socialcom.demo.delivery.space.authentication.token.DelivererTokenService;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/delivery/space/missions")
public class MissionController {
    @Autowired
    DelivererTokenService delivererTokenService;
    @Autowired
    DeliveryRepository deliveryService;
    @Autowired
    MissionHistoryRepository missionHistoryRepository;
    @GetMapping()
    public ResponseEntity<ApiResponse> getMissions(@RequestHeader(name = "Authorization") String token) {
        try {
            DeliveryDriver deliveryDriver = delivererTokenService.findByToken(token);
            List<Delivery> deliveries = deliveryService.findByAmountInRange(deliveryDriver.getMinRange(), deliveryDriver.getMaxRange());
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(deliveries);
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
    @GetMapping("/completed")
    public ResponseEntity<ApiResponse> getCompletedMissions(@RequestHeader(name = "Authorization") String token) {
        try {
            DeliveryDriver deliveryDriver = delivererTokenService.findByToken(token);
            List<MissionHistory> deliveries = missionHistoryRepository.findByIdDd(deliveryDriver.getId());
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(deliveries);
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
}
