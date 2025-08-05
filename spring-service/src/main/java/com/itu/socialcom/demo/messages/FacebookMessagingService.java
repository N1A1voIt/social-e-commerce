package com.itu.socialcom.demo.messages;

import com.itu.socialcom.demo.messages.dtol.MessageBody;
import com.itu.socialcom.demo.messages.inbox.InboxRepository;
import com.itu.socialcom.demo.messages.messagechild.MessageChild;
import com.itu.socialcom.demo.messages.messagechild.MessageChildRepository;
import com.itu.socialcom.demo.messages.messagemother.MessageMother;
import com.itu.socialcom.demo.messages.messagemother.MessageMotherRepository;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPageCPL;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageCPLRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class FacebookMessagingService extends MessageService{
    public FacebookMessagingService(InboxRepository inboxRepo, MessageMotherRepository motherRepo, MessageChildRepository childRepo, ManagedPageCPLRepository managedPageCPLRepository) {
        super(inboxRepo, motherRepo, childRepo,managedPageCPLRepository);
    }
    @Override
    public void sendMessage(String recipientId, String messageText, String pageAccessToken) {
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
                System.out.println("Failed to send message. HTTP error code: " + responseCode);
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Transactional
    public MessageChild answerMessageChild(MessageBody messageBody) {
        MessageChild messageChild = new MessageChild();
        messageChild.setIdMm( messageBody.getIdMm().intValue());
        messageChild.setFromPlatform(false);
        messageChild.setMessage(messageBody.getMessage());
        super.childRepo.save(messageChild);
        MessageMother messageMother = super.motherRepo.findById(messageBody.getIdMm().intValue()).orElse(null);
        ManagedPageCPL managedPageCPL = super.managedPageCPLRepository.findByPlatformIdentifierAndPlatform(messageMother.getIdMp().toString(), "facebook");
        sendMessage(managedPageCPL.getPlatformIdentifier(),messageBody.getMessage(),managedPageCPL.getRefreshToken());
        return messageChild;
    }
}
