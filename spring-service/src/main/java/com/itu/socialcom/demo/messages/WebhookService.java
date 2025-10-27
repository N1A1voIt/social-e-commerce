package com.itu.socialcom.demo.messages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itu.socialcom.demo.messages.inbox.InboxRepository;
import com.itu.socialcom.demo.messages.messagechild.MessageChild;
import com.itu.socialcom.demo.messages.messagechild.MessageChildRepository;
import com.itu.socialcom.demo.messages.messagemother.MessageMotherRepository;
import com.itu.socialcom.demo.potentialCustomers.entity.PotentialCustomerV2;
import com.itu.socialcom.demo.potentialCustomers.repository.PotentialCustomerV2Repository;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPageCPL;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageCPLRepository;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public abstract class WebhookService {
//    private void processMessageEvent(JsonNode event) {
//        JsonNode sender = event.get("sender");
//        JsonNode recipient = event.get("recipient");
//        JsonNode message = event.get("message");
//
//        if (sender != null && message != null && message.get("text") != null) {
//            String senderId = sender.get("id").asText();
//            String recipientId = recipient.get("id").asText();
//            String text = message.get("text").asText();
//
//            System.out.println("Message from {} to {}: {}"+ senderId+ recipientId+ text);
//
//            // TODO: Insert into your local messaging system
////            syncToLocalMessagingSystem(senderId, recipientId, text);
//        }
//    }
    @Autowired
    PotentialCustomerV2Repository potentialCustomerV2Repository;
    @Autowired
    InboxRepository inboxRepository;
    @Autowired
    ManagedPageRepository managedPageRepository;
    @Autowired
    MessageMotherRepository messageMotherRepository;
    @Autowired
    MessageChildRepository messageChildRepository;
    @Autowired
    ManagedPageCPLRepository managedPageCPLRepository;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    ObjectMapper objectMapper;

    public abstract MessageChild handleCustomerMessage(JsonNode node) throws Exception;


}

