package com.itu.socialcom.demo.messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessagingFactory {
    @Autowired
    FacebookMessagingService facebookMessagingService;
    @Autowired
    InstagramMessagingService instagramMessagingService;
    public MessageService getMessageService(String platform) {
        switch (platform.toLowerCase()) {
            case "facebook":
                return facebookMessagingService;
            case "instagram":
                return instagramMessagingService;
            default:
                throw new IllegalArgumentException("Unsupported messaging platform: " + platform);
        }
    }
}
