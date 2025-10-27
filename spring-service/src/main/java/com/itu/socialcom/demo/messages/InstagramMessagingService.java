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
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
public class InstagramMessagingService extends MessageService {
    public InstagramMessagingService(
            InboxRepository inboxRepo,
            MessageMotherRepository motherRepo,
            MessageChildRepository childRepo,
            ManagedPageCPLRepository managedPageCPLRepository,
            PotentialCustomerV2Repository potentialCustomerV2Repository
    ) {
        super(inboxRepo, motherRepo, childRepo, managedPageCPLRepository, potentialCustomerV2Repository);
    }

    @Override
    public void sendMessage(String recipientId, String messageText, String pageAccessToken) throws Exception {
        String apiUrl = "https://graph.facebook.com/v18.0/me/messages?access_token=" + pageAccessToken;
        String jsonPayload = "{" +
                "\"recipient\":{\"id\":\"" + recipientId + "\"}," +
                "\"message\":{\"text\":\"" + messageText + "\"}," +
                "\"messaging_type\":\"RESPONSE\"," +
                "\"messaging_product\":\"instagram\"" +
                "}";
        HttpURLConnection conn = null;
        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            }
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("Failed to send Instagram message. HTTP error code: " + responseCode);
            }
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    @Transactional
    @Override
    public MessageChild answerMessageChild(MessageBody messageBody) throws Exception {
        MessageChild messageChild = new MessageChild();
        messageChild.setIdMm(messageBody.getIdMm().intValue());
        messageChild.setFromPlatform(false);
        messageChild.setMessage(messageBody.getMessage());
        messageChild.setCreatedAt(LocalDateTime.now());
        super.childRepo.save(messageChild);

        MessageMother messageMother = super.motherRepo.findById(messageBody.getIdMm().intValue()).orElse(null);
        if (messageMother == null) {
            throw new IllegalArgumentException("MessageMother not found for idMm=" + messageBody.getIdMm());
        }
        ManagedPageCPL managedPageCPL = super.managedPageCPLRepository.findById(messageMother.getIdMp().longValue()).orElse(null);
        if (managedPageCPL == null) {
            throw new IllegalArgumentException("ManagedPageCPL not found for idMp=" + messageMother.getIdMp());
        }
        PotentialCustomerV2 potentialCustomerV2 = super.potentialCustomerV2Repository.findById(messageMother.getIdPc()).orElse(null);
        if (potentialCustomerV2 == null) {
            throw new IllegalArgumentException("PotentialCustomerV2 not found for idPc=" + messageMother.getIdPc());
        }

        sendMessage(potentialCustomerV2.getIdentifierOnPlatform(), messageBody.getMessage(), managedPageCPL.getRefreshToken());
        return messageChild;
    }
}

