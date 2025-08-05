package com.itu.socialcom.demo.messages;

import com.itu.socialcom.demo.messages.dtol.MessageBody;
import com.itu.socialcom.demo.messages.inbox.Inbox;
import com.itu.socialcom.demo.messages.inbox.InboxRepository;
import com.itu.socialcom.demo.messages.messagechild.MessageChild;
import com.itu.socialcom.demo.messages.messagechild.MessageChildRepository;
import com.itu.socialcom.demo.messages.messagemother.MessageMother;
import com.itu.socialcom.demo.messages.messagemother.MessageMotherRepository;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageCPLRepository;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public abstract class MessageService {

    final InboxRepository inboxRepo;
    final MessageMotherRepository motherRepo;
    final MessageChildRepository childRepo;
    final ManagedPageCPLRepository managedPageCPLRepository;

    public MessageService(
            InboxRepository inboxRepo,
            MessageMotherRepository motherRepo,
            MessageChildRepository childRepo,
            ManagedPageCPLRepository managedPageCPLRepository
    ) {
        this.inboxRepo = inboxRepo;
        this.motherRepo = motherRepo;
        this.childRepo = childRepo;
        this.managedPageCPLRepository = managedPageCPLRepository;
    }

    public Inbox saveInbox(Inbox inbox) {
        return inboxRepo.save(inbox);
    }

    public MessageMother saveMother(MessageMother mother) {
        return motherRepo.save(mother);
    }

    public MessageChild saveChild(MessageChild child) {
        return childRepo.save(child);
    }
    public abstract void sendMessage(String recipientId, String messageText, String pageAccessToken);
    public abstract MessageChild answerMessageChild(MessageBody messageBody);

    // Add fetch methods as needed
}
