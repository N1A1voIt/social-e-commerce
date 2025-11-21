package com.itu.socialcom.demo.delivery.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiDeliveryAssistanceRequest {
    private Long deliveryId;
    private List<FirebaseUidDTO> firebaseUIDs;
    private String callbackUrl; // URL where AI server will POST the response

    @Data
    public static class FirebaseUidDTO {
        private String uid;
    }
}
