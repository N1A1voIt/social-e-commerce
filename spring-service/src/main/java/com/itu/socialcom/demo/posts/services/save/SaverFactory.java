package com.itu.socialcom.demo.posts.services.save;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SaverFactory {
    @Autowired
    FacebookPostSaver facebookPostSaver;
    @Autowired
    InstagramPostSaver instagramPostSaver;
    public SavePostService getSaver(String platform) {
        SavePostService service = switch (platform) {
            case "facebook" -> facebookPostSaver;
            case "instagram" -> instagramPostSaver;
            default -> throw new IllegalArgumentException("Unsupported platform: " + platform);
        };
        return service;
    }
//    public SavePostService getSaver(Long idPlatform) {
//        SavePostService service = switch (idPlatform) {
//            case 1L -> facebookPostSaver;
//            case 2L -> instagramPostSaver;
//            default -> throw new IllegalArgumentException("Unsupported platform: " + platform);
//        };
//        return service;
//    }
}
