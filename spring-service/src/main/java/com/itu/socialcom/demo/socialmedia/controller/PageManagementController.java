package com.itu.socialcom.demo.socialmedia.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2Repository;
import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.socialmedia.dto.ManagedPageWithToken;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPageCPL;
import com.itu.socialcom.demo.socialmedia.factory.PlatformFactory;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageCPLRepository;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageRepository;
import com.itu.socialcom.demo.socialmedia.service.AuthService;
import com.itu.socialcom.demo.socialmedia.service.CacheV1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    @Autowired
    private CacheV1 cacheV1;
    @Autowired
    private ManagedPageCPLRepository managedPageRepository;
    @Autowired
    private TokenV2ServiceImpl tokenV2Repository;
    @Value("${front-end.prefix}")
    String prefix;
    @GetMapping("/{platform}/login")
    public RedirectView login(@PathVariable String platform) {
        AuthService service = platformFactory.getAuthService(platform);
        String loginUrl = service.getLoginUrl();
        return new RedirectView(loginUrl);
    }

    @GetMapping("/{platform}/callback")
    public RedirectView callback(@PathVariable String platform, @RequestParam Map<String, String> params) {
        try {
            AuthService service = platformFactory.getAuthService(platform);
            String codeOrToken = params.get("code");
            if (codeOrToken == null) {
                codeOrToken = params.get("oauth_token");
            }
            String accessToken = service.exchangeForAccessToken(codeOrToken);
            List<ManagedPageWithToken> managedPages = service.getManagedPages();
            String uuid = cacheV1.cacheManagedPlatforms(managedPages);
            String url = prefix+"/auth/"+platform+"?uuid="+uuid;
            return new RedirectView(url);
        } catch (Exception e) {
            e.printStackTrace();
            return new RedirectView(prefix+"/error");
        }
    }
    @GetMapping("/{platform}/managed-pages")
    public ResponseEntity<List<ManagedPage>> getManagedPages(@PathVariable String platform,@RequestParam Map<String, String> params,@RequestHeader("Authorization") String token) {
        try {
            AuthService service = platformFactory.getAuthService(platform);
            List<ManagedPage> managedPages = service.savePages(params.get("uuid"),token);
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
    @GetMapping("/managed-pages-all")
    public ResponseEntity<List<ManagedPageCPL>> getAllManagedPages(@RequestHeader("Authorization") String token) {
        try {
            Seller seller = tokenV2Repository.findSellerByToken(token).orElse(null);
            if (seller == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            List<ManagedPageCPL> managedPages = managedPageRepository.findByIdSeller(seller.getId());
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
