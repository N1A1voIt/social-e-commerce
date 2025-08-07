package com.itu.socialcom.demo.messages.inbox;

import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.messages.messagemother.MessageMother;
import com.itu.socialcom.demo.messages.messagemother.MessageMotherCPLReposistory;
import com.itu.socialcom.demo.messages.messagemother.MessageMotherRepository;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageCPLRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InboxController {
    @Autowired
    private TokenV2ServiceImpl tokenV2Service;
    @Autowired
    private MessageMotherCPLReposistory messageMotherRepository;
    @Autowired
    private ManagedPageCPLRepository managedPageCPLRepository;
    @GetMapping("/api/inbox")
    public ResponseEntity<InboxDisplay> getInbox(@RequestParam(name = "idMp") Integer idMp, @RequestHeader("Authorization") String token) {
        try{
            if (tokenV2Service.findSellerByToken(token).isEmpty()) throw new Exception("Not logged in");
            InboxDisplay inboxDisplay = new InboxDisplay();
            inboxDisplay.setMothers(messageMotherRepository.findByIdMp(idMp.longValue()));
            inboxDisplay.setPage(managedPageCPLRepository.findByIdMp(idMp.longValue()));
            return ResponseEntity.ok(inboxDisplay);
        }catch (Exception e){
            return ResponseEntity.status(400).body(null);
        }
    }
}
