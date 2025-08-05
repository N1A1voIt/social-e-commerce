package com.itu.socialcom.demo.messages;

import com.itu.socialcom.demo.messages.dtol.MessageBody;
import com.itu.socialcom.demo.messages.inbox.InboxRepository;
import com.itu.socialcom.demo.messages.messagechild.MessageChild;
import com.itu.socialcom.demo.messages.messagechild.MessageChildRepository;
import com.itu.socialcom.demo.messages.messagemother.MessageMother;
import com.itu.socialcom.demo.messages.messagemother.MessageMotherRepository;
import com.itu.socialcom.demo.potentialCustomers.entity.PotentialCustomerV2;
import com.itu.socialcom.demo.potentialCustomers.repository.PotentialCustomerV2Repository;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPageCPL;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageCPLRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class FacebookMessagingService extends MessageService{
    public FacebookMessagingService(InboxRepository inboxRepo, MessageMotherRepository motherRepo, MessageChildRepository childRepo, ManagedPageCPLRepository managedPageCPLRepository, PotentialCustomerV2Repository potentialCustomerV2Repository) {
        super(inboxRepo, motherRepo, childRepo,managedPageCPLRepository,potentialCustomerV2Repository);
    }
    @Override
    public void sendMessage(String recipientId, String messageText, String pageAccessToken) throws Exception{
        try {
            String apiUrl = "https://graph.facebook.com/v18.0/me/messages?access_token=" + pageAccessToken;

            // JSON payload
            String jsonPayload = "{"
                    + "\"recipient\":{\"id\":\"" + recipientId + "\"},"
                    + "\"message\":{\"text\":\"" + messageText + "\"}"
                    + "}";

            // Set up connection
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes("utf-8"));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Message sent successfully.");
            } else {
                throw new Exception("Failed to send message. HTTP error code: " + responseCode);
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    @Transactional
    public MessageChild answerMessageChild(MessageBody messageBody) throws Exception{
        MessageChild messageChild = new MessageChild();
        messageChild.setIdMm( messageBody.getIdMm().intValue());
        messageChild.setFromPlatform(false);
        messageChild.setMessage(messageBody.getMessage());
        super.childRepo.save(messageChild);
        MessageMother messageMother = super.motherRepo.findById(messageBody.getIdMm().intValue()).orElse(null);
//        System.out.println("MessageMother: " + messageMother.getIdMp());
        ManagedPageCPL managedPageCPL = super.managedPageCPLRepository.findById(messageMother.getIdMp().longValue()).orElse(null);
        System.out.println("ManagedPageCPL: " + managedPageCPL.getPlatformIdentifier() + ", Token: " + managedPageCPL.getRefreshToken());
        PotentialCustomerV2 potentialCustomerV2 = super.potentialCustomerV2Repository.findById(messageMother.getIdPc()).orElse(null);
        sendMessage(potentialCustomerV2.getIdentifierOnPlatform(),messageBody.getMessage(),managedPageCPL.getRefreshToken());
        return messageChild;
    }
}
