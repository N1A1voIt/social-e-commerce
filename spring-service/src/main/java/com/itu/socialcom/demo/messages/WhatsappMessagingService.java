package com.itu.socialcom.demo.messages;

import com.itu.socialcom.demo.messages.dtol.MessageBody;
import com.itu.socialcom.demo.messages.inbox.InboxRepository;
import com.itu.socialcom.demo.messages.messagechild.MessageChild;
import com.itu.socialcom.demo.messages.messagechild.MessageChildRepository;
import com.itu.socialcom.demo.messages.messagemother.MessageMotherRepository;
import com.itu.socialcom.demo.potentialCustomers.repository.PotentialCustomerV2Repository;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageCPLRepository;
import org.springframework.stereotype.Service;

//@Service
public class WhatsappMessagingService extends MessageService {
    public WhatsappMessagingService(InboxRepository inboxRepo, MessageMotherRepository motherRepo, MessageChildRepository childRepo, ManagedPageCPLRepository managedPageCPLRepository, PotentialCustomerV2Repository potentialCustomerV2Repository) {
        super(inboxRepo, motherRepo, childRepo, managedPageCPLRepository, potentialCustomerV2Repository);
    }

    @Override
    public void sendMessage(String recipientId, String messageText, String pageAccessToken) throws Exception {

    }

    @Override
    public MessageChild answerMessageChild(MessageBody messageBody) throws Exception {
        return null;
    }
}
