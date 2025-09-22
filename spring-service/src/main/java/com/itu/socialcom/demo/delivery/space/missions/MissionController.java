package com.itu.socialcom.demo.delivery.space.missions;

import com.itu.socialcom.demo.delivery.deliverydriver.DeliveryDriver;
import com.itu.socialcom.demo.delivery.entity.Delivery;
import com.itu.socialcom.demo.delivery.repository.DeliveryRepository;
import com.itu.socialcom.demo.delivery.service.DeliveryService;
import com.itu.socialcom.demo.delivery.space.authentication.token.DelivererTokenService;
import com.itu.socialcom.demo.orders.delivery.DeliveryLog;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    MissionService missionService;
    @Autowired
    PendingMissionRepository pendingMissionRepository;
    @GetMapping()
    public ResponseEntity<ApiResponse> getMissions(@RequestHeader(name = "Authorization") String token) {
        try {
            DeliveryDriver deliveryDriver = delivererTokenService.findByToken(token);
            List<Delivery> deliveries = deliveryService.findByAmountInRange(deliveryDriver.getId(),deliveryDriver.getMinRange(), deliveryDriver.getMaxRange());
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
            List<MissionHistory> deliveries = missionHistoryRepository.findByIdDdAndLogIdDeliverer(deliveryDriver.getId(),deliveryDriver.getId());
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
    @GetMapping("/pending-requests")
    public ResponseEntity<ApiResponse> getPendingRequests(@RequestHeader(name = "Authorization") String token) {
        try {
            DeliveryDriver deliveryDriver = delivererTokenService.findByToken(token);
            List<PendingMission> pendingMissions = pendingMissionRepository.findByLogIdDeliverer(deliveryDriver.getId());
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(pendingMissions);
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
    @GetMapping("/apply/{id_mission}")
    public ResponseEntity<ApiResponse> applyForMission(@RequestHeader(name = "Authorization") String token, @PathVariable(name = "id_mission") Long idMission) {
        try {
            DeliveryDriver deliveryDriver = delivererTokenService.findByToken(token);
            DeliveryLog deliveryLog = missionService.applyTo(idMission,deliveryDriver);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(deliveryLog);
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
