package com.itu.socialcom.demo.orders.delivery;

import com.itu.socialcom.demo.delivery.entity.Delivery;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeliveryController {
    @Autowired
    ApplicantService applicantService;
    @GetMapping("/api/delivery/{id_delivery}/assign/{id_applicant}")
    public ResponseEntity<ApiResponse> assignDelivery(@PathVariable("id_delivery") Long idDelivery, @PathVariable("id_applicant") Long idApplicant) {
        try {
            Delivery delivery = applicantService.assignDelivery(idDelivery, idApplicant);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(delivery);
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
