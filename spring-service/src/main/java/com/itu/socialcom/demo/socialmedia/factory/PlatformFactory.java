package com.itu.socialcom.demo.socialmedia.factory;

import com.itu.socialcom.demo.socialmedia.service.AuthService;
import com.itu.socialcom.demo.socialmedia.service.FacebookAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlatformFactory {
    @Autowired
    private FacebookAuth facebookAuthService;
    public AuthService getAuthService(String platformLabel) {
        AuthService service = switch (platformLabel.toLowerCase()) {
            case "facebook" -> facebookAuthService;
            default -> throw new IllegalArgumentException("Unsupported platform: " + platformLabel);
        };
        if (service == null) {
            throw new IllegalStateException("AuthService implementation not found for bean name: "+platformLabel);
        }
        return service;
    }
}
