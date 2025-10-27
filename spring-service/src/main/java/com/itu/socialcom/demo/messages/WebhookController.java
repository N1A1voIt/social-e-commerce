package com.itu.socialcom.demo.messages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itu.socialcom.demo.orders.delivery.DeliveryLog;
import com.itu.socialcom.demo.whatsapp.service.WhatsAppServiceImpl;
import com.itu.socialcom.demo.whatsapp.service.WhatsappReceiverImpl;
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
import java.util.List;
import java.util.Map;

@RestController
public class WebhookController {
    private static final String TARGET_NUMBER = "whatsapp:+15551750923";
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    @Autowired
    FacebookWebhookService webhookService;
    @Autowired
    WhatsappReceiverImpl whatsappReceiver;
    @Autowired
    WhatsAppServiceImpl whatsAppService;
    @Value("${messenger.verify-token}")
    private String verifyToken;

    @Value("${facebook.secret}")
    private String appSecret;
    // Instagram-specific beans and secrets
    @Autowired
    private InstagramWebhookService instagramWebhookService;
    @Value("${instagram.secret}")
    private String instagramSecret;

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
    @GetMapping("/whatsapp/webhook")
    public ResponseEntity<String> verifyWhatsappWebhook(
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
    @PostMapping("/whatsapp/webhook")
    public ResponseEntity<String> receiveMessage(@RequestBody Map<String, Object> body) {
        System.out.println("Webhook received: " + body);

        try {
            Map entry = ((List<Map>) body.get("entry")).get(0);
            Map changes = ((List<Map>) entry.get("changes")).get(0);
            Map value = (Map) changes.get("value");

            if (value.containsKey("messages")) {
                Map message = ((List<Map>) value.get("messages")).get(0);
                Map contact = ((List<Map>) value.get("contacts")).get(0);

                String from = (String) message.get("from");
                String type = (String) message.get("type");

                if ("button".equals(type)) {
                    Map button = (Map) message.get("button");
                    String payload = (String) button.get("payload");
                    String buttonText = (String) button.get("text");

                    System.out.println("Button clicked: " + buttonText + " | Payload: " + payload);
                    DeliveryLog deliveryLog = whatsappReceiver.processIncomingMessage(payload, from,buttonText);

                } else if ("text".equals(type)) {
                    Map textObj = (Map) message.get("text");
                    String userMessage = (String) textObj.get("body");
                    System.out.println("User sent text: " + userMessage);
                    whatsAppService.sendMessage(from, "You are only allowed to click on a button, not chat in this channel.");
                } else {
                    System.out.println("Unhandled message type: " + type);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok("EVENT_RECEIVED");
    }


    // Webhook endpoint (POST request from Facebook)
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String body,
            HttpServletRequest request) {
        System.out.println("bruh");
        logger.info("Received webhook POST request");

        if (!verifyRequestSignature(request, body)) {
            logger.warn("Invalid request signature");
            return ResponseEntity.status(403).body("Invalid signature");
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(body);
            System.out.println("Hello");
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
    // Instagram webhook verification (GET)
    @GetMapping("/instagram/webhook")
    public ResponseEntity<String> verifyInstagramWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {
        logger.info("Instagram webhook verification - Mode: {}, Token: {}", mode, token);
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).body("Forbidden");
    }

    // Instagram webhook handler (POST)
    @PostMapping("/instagram/webhook")
    public ResponseEntity<String> handleInstagramWebhook(
            @RequestBody String body,
            HttpServletRequest request) {
        logger.info("Received Instagram webhook POST request");

//        if (!verifyInstagramRequestSignature(request, body)) {
//            logger.warn("Invalid Instagram request signature");
//            return ResponseEntity.status(403).body("Invalid signature");
//        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(body);
            JsonNode entries = jsonNode.get("entry");
            if (entries != null && entries.isArray()) {
                for (JsonNode entry : entries) {
                    JsonNode messagingEvents = entry.get("messaging");
                    if (messagingEvents != null && messagingEvents.isArray()) {
                        for (JsonNode event : messagingEvents) {
                            System.out.println(event.toString());
                            instagramWebhookService.handleCustomerMessage(event);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing Instagram webhook payload", e);
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

    private boolean verifyInstagramRequestSignature(HttpServletRequest request, String body) {
        // Prefer the new SHA256 header if present
        String signature256 = request.getHeader("X-Hub-Signature-256");
        try {
            if (signature256 != null) {
                String[] parts = signature256.split("=");
                if (parts.length == 2 && "sha256".equals(parts[0])) {
                    Mac mac = Mac.getInstance("HmacSHA256");
                    mac.init(new SecretKeySpec(instagramSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
                    byte[] digest = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
                    StringBuilder expected = new StringBuilder();
                    for (byte b : digest) expected.append(String.format("%02x", b));
                    return parts[1].equals(expected.toString());
                }
            }
        } catch (Exception e) {
            logger.error("Error verifying Instagram SHA256 signature", e);
            return false;
        }
        // Fallback to legacy SHA1 if 256 header not present
        String signature = request.getHeader("X-Hub-Signature");
        if (signature == null) {
            logger.warn("No X-Hub-Signature header found (Instagram)");
            return true; // Skip if no signature (testing)
        }
        try {
            String[] elements = signature.split("=");
            if (elements.length != 2 || !"sha1".equals(elements[0])) {
                return false;
            }
            String signatureHash = elements[1];
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(instagramSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] digest = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            StringBuilder expectedHash = new StringBuilder();
            for (byte b : digest) expectedHash.append(String.format("%02x", b));
            return signatureHash.equals(expectedHash.toString());
        } catch (Exception e) {
            logger.error("Error verifying Instagram SHA1 signature", e);
            return false;
        }
    }
}

