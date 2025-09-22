package com.itu.socialcom.demo.whatsapp.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Implementation of WhatsAppService that uses the Meta Graph API.
 */
@Service
@Slf4j
public class WhatsAppServiceImpl implements WhatsAppService {

    @Value("${whatsapp.api.url:https://graph.facebook.com/v17.0}")
    private String apiUrl;

    @Value("${whatsapp.phone.number.id}")
    private String phoneNumberId;

    @Value("${whatsapp.business.account.id}")
    private String businessAccountId;

//    @Value("${whatsapp.app.id}")
    private String appId;

//    @Value("${whatsapp.app.secret}")
    private String appSecret;

    @Value("${whatsapp.token}")
    private String accessToken;

    private LocalDateTime tokenExpiryTime;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

//    @PostConstruct
    public void init() {
        generateAccessToken();
    }

    /**
     * Scheduled task to renew the access token every 24 hours.
     * This ensures we always have a valid token.
     */
//    @Scheduled(fixedRate = 24 * 60 * 60 * 1000) // 24 hours in milliseconds
    public void renewAccessToken() {
        generateAccessToken();
    }

    /**
     * Generates a new access token using the app credentials.
     */
    private void generateAccessToken() {
        try {
            String tokenUrl = String.format(
                "https://graph.facebook.com/oauth/access_token?client_id=%s&client_secret=%s&grant_type=client_credentials",
                appId, appSecret);

            ResponseEntity<String> response = restTemplate.getForEntity(tokenUrl, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            accessToken = root.get("access_token").asText();

            // Set token expiry to 23 hours from now (to be safe)
            tokenExpiryTime = LocalDateTime.now().plusHours(23);

            log.info("WhatsApp access token generated successfully. Valid until: {}", tokenExpiryTime);
        } catch (Exception e) {
            log.error("Failed to generate WhatsApp access token", e);
            throw new RuntimeException("Failed to generate WhatsApp access token", e);
        }
    }

    @Override
    public String getAccessToken() {
        // Check if token is expired or about to expire
        if (accessToken == null || LocalDateTime.now().isAfter(tokenExpiryTime)) {
            generateAccessToken();
        }
        return accessToken;
    }

    @Override
    public boolean sendMessage(String phoneNumber, String message) {
        try {
            String url = apiUrl + "/" + phoneNumberId + "/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(this.accessToken);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("messaging_product", "whatsapp");
            requestBody.put("to", phoneNumber);
            requestBody.put("type", "text");

            ObjectNode messageNode = requestBody.putObject("text");
            messageNode.put("body", message);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            log.info(headers.toString() + url+" Sending WhatsApp message: {}", requestBody.toString());

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

            log.info("WhatsApp message sent to {}: {}", phoneNumber, response.getBody());
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to {}", phoneNumber, e);
            return false;
        }
    }
//    @Transactional
    @Override
    public boolean sendTemplateMessage(String phoneNumber, String templateName, String buttonPayload, Object... parameters) {
        try {
            String url = apiUrl + "/" + phoneNumberId + "/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(this.accessToken);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("messaging_product", "whatsapp");
            requestBody.put("to", phoneNumber);
            requestBody.put("type", "template");

            ObjectNode templateNode = requestBody.putObject("template");
            templateNode.put("name", templateName);

            ObjectNode languageNode = templateNode.putObject("language");
            languageNode.put("code", "en");

            var componentsArray = templateNode.putArray("components");

            if (parameters != null && parameters.length > 0) {
                var bodyComponent = componentsArray.addObject();
                bodyComponent.put("type", "body");
                var bodyParams = bodyComponent.putArray("parameters");

                for (Object paramValue : parameters) {
                    var param = bodyParams.addObject();
                    param.put("type", "text");
                    param.put("text", paramValue.toString());
                }
            }

            // --- Button with custom payload ---
            if (buttonPayload != null) {
                var buttonComponent = componentsArray.addObject();
                buttonComponent.put("type", "button");
                buttonComponent.put("sub_type", "quick_reply");
                buttonComponent.put("index", "0"); // button index in your template

                var buttonParams = buttonComponent.putArray("parameters");
                var payloadNode = buttonParams.addObject();
                payloadNode.put("type", "payload");
                payloadNode.put("payload", buttonPayload);
            }

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            log.debug("Sending WhatsApp template message: {}", requestBody.toString());

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            log.info("WhatsApp template message sent to {}: {}", phoneNumber, response.getBody());
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Failed to send WhatsApp template message to {}", phoneNumber, e);
            throw e;
        }
    }

    @Override
    public boolean sendHelloWorldTemplate(String phoneNumber) {
        try {
            String url = apiUrl + "/" + phoneNumberId + "/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(this.accessToken);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            String requestBody = "{ \"messaging_product\": \"whatsapp\", \"to\": \"" + phoneNumber + 
                "\", \"type\": \"template\", \"template\": { \"name\": \"hello_world\", \"language\": { \"code\": \"en_US\" } } }";

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            log.debug("Sending WhatsApp hello_world template: {}", requestBody);

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

            log.info("WhatsApp hello_world template sent to {}: {}", phoneNumber, response.getBody());
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Failed to send WhatsApp hello_world template to {}", phoneNumber, e);
            return false;
        }
    }


}
