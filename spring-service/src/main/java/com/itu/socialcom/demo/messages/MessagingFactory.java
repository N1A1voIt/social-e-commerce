package com.itu.socialcom.demo.messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessagingFactory {
    @Autowired
    FacebookMessagingService facebookMessagingService;
    public MessageService getMessageService(String platform) {
        switch (platform.toLowerCase()) {
            case "facebook":
                return facebookMessagingService;
            default:
                throw new IllegalArgumentException("Unsupported messaging platform: " + platform);
        }
    }
}
