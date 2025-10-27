package com.itu.socialcom.demo.messages;

import com.fasterxml.jackson.databind.JsonNode;
import com.itu.socialcom.demo.messages.inbox.Inbox;
import com.itu.socialcom.demo.messages.messagechild.MessageChild;
import com.itu.socialcom.demo.messages.messagemother.MessageMother;
import com.itu.socialcom.demo.potentialCustomers.entity.PotentialCustomerV2;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPageCPL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InstagramWebhookService extends WebhookService {

    public PotentialCustomerV2 getSenderInfo(String senderId, String receiverId) {
        System.out.println("senderId: " + senderId);
        System.out.println("receiverId: " + receiverId);
        ManagedPageCPL managedPageCPL = managedPageCPLRepository.findByPlatformIdentifierAndPlatform(receiverId, "instagram");
        String url = UriComponentsBuilder
                .fromUriString("https://graph.facebook.com/v18.0/" + senderId)
                .queryParam("fields", "username")
                .queryParam("access_token", managedPageCPL.getRefreshToken())
                .toUriString();

        String response = restTemplate.getForObject(url, String.class);
        try {
            JsonNode node = objectMapper.readTree(response);
            PotentialCustomerV2 potentialCustomer = new PotentialCustomerV2();
            potentialCustomer.setIdentifierOnPlatform(senderId);
            // Instagram may not expose full name; use username as name
            String username = node.has("username") ? node.get("username").asText() : senderId;
            potentialCustomer.setName(username);
            String pfp = node.has("profile_picture_url") ? node.get("profile_picture_url").asText() : null;
            potentialCustomer.setMediaUrl(pfp);
            potentialCustomer.setSupportedPlatform(2L);
            potentialCustomer.setPlatform("instagram");
            return potentialCustomer;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Instagram Graph API response", e);
        }
    }

    @Transactional
    @Override
    public MessageChild handleCustomerMessage(JsonNode node) throws Exception {
        if (node.get("sender") == null || node.get("recipient") == null || node.get("message") == null) {
            return null;
        }
        List<PotentialCustomerV2> pc = potentialCustomerV2Repository
                .findByIdentifierOnPlatformAndSupportedPlatform(node.get("sender").get("id").asText(), 2L);
        boolean newCustomer = false;
        if (pc.isEmpty()) {
            PotentialCustomerV2 potentialCustomer = getSenderInfo(
                    node.get("sender").get("id").asText(),
                    node.get("recipient").get("id").asText()
            );
            potentialCustomerV2Repository.save(potentialCustomer);
            pc.add(potentialCustomer);
            newCustomer = true;
        }

        ManagedPage managedPage = managedPageRepository
                .findByPlatformIdentifierAndPlatform(node.get("recipient").get("id").asText(), 2L)
                .orElse(null);

        Inbox inbox = new Inbox();
        boolean newInbox = false;
        if ((inboxRepository.findByIdMp(managedPage.getId().intValue()).orElse(null) == null)) {
            inbox.setIdMp(managedPage.getId().intValue());
            inboxRepository.save(inbox);
            newInbox = true;
            System.out.println("New inbox created" + inbox.getId());
        } else {
            inbox = inboxRepository.findByIdMp(managedPage.getId().intValue()).orElse(null);
        }

        MessageMother messageMother = new MessageMother();
        if (newInbox || newCustomer) {
            messageMother.setIdPc(pc.get(0).getId());
            messageMother.setIdIm(inbox.getId());
            messageMother.setIdMp(managedPage.getId().intValue());
            messageMotherRepository.save(messageMother);
        } else {
            messageMother = messageMotherRepository.findByIdPcAndIdIm(pc.get(0).getId(), inbox.getId()).orElse(null);
        }

        MessageChild messageChild = new MessageChild();
        messageChild.setFromPlatform(true);
        messageChild.setMessage(node.get("message").get("text").asText());
        messageChild.setCreatedAt(LocalDateTime.now());
        messageChild.setIdMm(messageMother.getId());
        messageChildRepository.save(messageChild);
        return messageChild;
    }
}

