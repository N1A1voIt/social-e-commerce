package com.itu.socialcom.demo.socialmedia.controller;

import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import com.itu.socialcom.demo.socialmedia.factory.PlatformFactory;
import com.itu.socialcom.demo.socialmedia.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class PageManagementController {
    @Autowired
    PlatformFactory platformFactory;

    @GetMapping("/{platform}/login")
    public RedirectView login(@PathVariable String platform) {
        AuthService service = platformFactory.getAuthService(platform);
        String loginUrl = service.getLoginUrl();
        return new RedirectView(loginUrl);
    }

    @GetMapping("/{platform}/callback")
    public ResponseEntity<List<ManagedPage>> callback(@PathVariable String platform, @RequestParam Map<String, String> params) {
        try {
            AuthService service = platformFactory.getAuthService(platform);
            String codeOrToken = params.get("code");
            if (codeOrToken == null) {
                codeOrToken = params.get("oauth_token");
            }
            String accessToken = service.exchangeForAccessToken(codeOrToken);
            List<ManagedPage> managedPages = service.getManagedPages();
            return ResponseEntity.ok(managedPages);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
