package com.itu.socialcom.demo.messages;

import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.messages.dtol.MessageBody;
import com.itu.socialcom.demo.messages.messagechild.MessageChild;
import com.itu.socialcom.demo.utils.ApiResponse;
import com.itu.socialcom.demo.utils.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
public class MessageController {
    @Autowired
    TokenV2ServiceImpl tokenV2Service;
    @Autowired
    MessagingFactory messagingFactory;
    @PostMapping("/api/messages")
    public ResponseEntity<ApiResponse> handleMessage(@RequestBody MessageBody messageChild, @RequestHeader(name = "Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                throw new AuthException("Please log in to answer messages");
            }
            MessageChild child = messagingFactory.getMessageService(messageChild.getPlatform()).answerMessageChild(messageChild);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(child);
            return ResponseEntity.ok(apiResponse);
        } catch (AuthException e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(401);
            apiResponse.setData(null);
            apiResponse.setErrors(new ArrayList<>(){
                {
                    add(e);
                }
            });
            return ResponseEntity.status(401).body(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(new ArrayList<>(){
                {
                    add(e);
                }
            });
            return ResponseEntity.status(500).body(apiResponse);
        }
    }

}
