package com.itu.socialcom.demo.delivery.service;

import com.itu.socialcom.demo.delivery.dto.AiDeliveryAssistanceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiDeliveryAssistanceService {

    private final RestTemplate restTemplate = new RestTemplate();

    // Store pending AI requests waiting for callback
    private final Map<Long, PendingAiRequest> pendingRequests = new ConcurrentHashMap<>();

    @Value("${ai.delivery.assistance.url:http://localhost:5000/api/ai-delivery-selection}")
    private String aiAssistanceUrl;

    @Value("${server.base-url:http://localhost:8080}")
    private String serverBaseUrl;

    public void requestAiAssistanceAsync(AiDeliveryAssistanceRequest request) {
        try {
            log.info("Requesting AI assistance (async) for delivery ID: {}", request.getDeliveryId());
            log.info("Firebase UIDs: {}", request.getFirebaseUIDs());

            // Set callback URL
            String callbackUrl = serverBaseUrl + "/api/deliveries/ai-callback";
            request.setCallbackUrl(callbackUrl);

            // Store request as pending
            pendingRequests.put(request.getDeliveryId(), new PendingAiRequest(
                    request.getDeliveryId(),
                    System.currentTimeMillis()
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<AiDeliveryAssistanceRequest> entity = new HttpEntity<>(request, headers);

            // Send request asynchronously
            new Thread(() -> {
                try {
                    restTemplate.postForEntity(
                            aiAssistanceUrl,
                            entity,
                            String.class
                    );
                    log.info("AI assistance request sent successfully for delivery ID: {}", request.getDeliveryId());
                } catch (Exception e) {
                    log.error("Error sending AI assistance request", e);
                    pendingRequests.remove(request.getDeliveryId());
                }
            }).start();

        } catch (Exception e) {
            log.error("Error requesting AI assistance", e);
            throw new RuntimeException("Failed to initiate AI assistance: " + e.getMessage());
        }
    }

    public boolean isPending(Long deliveryId) {
        return pendingRequests.containsKey(deliveryId);
    }

    public void markAsProcessed(Long deliveryId) {
        pendingRequests.remove(deliveryId);
    }

    // Inner class to track pending requests
    @RequiredArgsConstructor
    private static class PendingAiRequest {
        private final Long deliveryId;
        private final long timestamp;
    }
}
