package com.itu.socialcom.demo.messages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    @Autowired
    FacebookWebhookService webhookService;

    @Value("${messenger.verify-token}")
    private String verifyToken;

    @Value("${facebook.secret}")
    private String appSecret;

    // Webhook verification (GET request from Facebook)
    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        logger.info("Webhook verification request - Mode: {}, Token: {}", mode, token);

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            logger.info("WEBHOOK_VERIFIED");
            return ResponseEntity.ok(challenge);
        } else {
            logger.warn("Webhook verification failed - incorrect token");
            return ResponseEntity.status(403).body("Forbidden");
        }
    }

    // Webhook endpoint (POST request from Facebook)
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String body,
            HttpServletRequest request) {

        logger.info("Received webhook POST request");

        if (!verifyRequestSignature(request, body)) {
            logger.warn("Invalid request signature");
            return ResponseEntity.status(403).body("Invalid signature");
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(body);

            JsonNode entries = jsonNode.get("entry");
            if (entries != null && entries.isArray()) {
                for (JsonNode entry : entries) {
                    JsonNode messagingEvents = entry.get("messaging");
                    if (messagingEvents != null && messagingEvents.isArray()) {
                        for (JsonNode event : messagingEvents) {
                            webhookService.handleCustomerMessage(event);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing webhook payload", e);
        }

        return ResponseEntity.ok("EVENT_RECEIVED");
    }
    private void processMessageEvent(JsonNode event) {
        JsonNode sender = event.get("sender");
        JsonNode recipient = event.get("recipient");
        JsonNode message = event.get("message");

        if (sender != null && message != null && message.get("text") != null) {
            String senderId = sender.get("id").asText();
            String recipientId = recipient.get("id").asText();
            String text = message.get("text").asText();

            logger.info("Message from {} to {}: {}", senderId, recipientId, text);

            // TODO: Insert into your local messaging system
//            syncToLocalMessagingSystem(senderId, recipientId, text);
        }
    }

    private boolean verifyRequestSignature(HttpServletRequest request, String body) {
        String signature = request.getHeader("X-Hub-Signature");

        if (signature == null) {
            logger.warn("No X-Hub-Signature header found");
            return true; // Skip verification if no signature (for testing)
        }

        try {
            String[] elements = signature.split("=");
            if (elements.length != 2 || !"sha1".equals(elements[0])) {
                return false;
            }

            String signatureHash = elements[1];

            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            mac.init(secretKeySpec);

            byte[] digest = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            StringBuilder expectedHash = new StringBuilder();
            for (byte b : digest) {
                expectedHash.append(String.format("%02x", b));
            }

            boolean valid = signatureHash.equals(expectedHash.toString());
            logger.info("Signature verification: {}", valid ? "PASSED" : "FAILED");
            return valid;

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Error verifying request signature", e);
            return false;
        }
    }
}