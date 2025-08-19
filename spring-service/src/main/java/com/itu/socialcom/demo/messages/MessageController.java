package com.itu.socialcom.demo.messages;

import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.messages.dtol.MessageBody;
import com.itu.socialcom.demo.messages.fetchskus.FetchSkusQtyFromPython;
import com.itu.socialcom.demo.messages.fetchskus.UserQuery;
import com.itu.socialcom.demo.messages.messagechild.MessageChild;
import com.itu.socialcom.demo.messages.messagechild.MessageChildRepository;
import com.itu.socialcom.demo.products.variants.model.Variant;
import com.itu.socialcom.demo.utils.ApiResponse;
import com.itu.socialcom.demo.utils.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class MessageController {
    @Autowired
    TokenV2ServiceImpl tokenV2Service;
    @Autowired
    MessagingFactory messagingFactory;
    @Autowired
    MessageChildRepository messageChildRepository;
    @Autowired
    FetchSkusQtyFromPython fetchSkusQtyFromPython;

    @PostMapping("/api/messages/fetch-orders")
    public ResponseEntity<ApiResponse> fetchOrders(@RequestBody UserQuery uquery, @RequestHeader(name = "Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                throw new AuthException("Please log in to fetch orders");
            }
            List<Variant> messageChildren = fetchSkusQtyFromPython.fetchVariants(uquery, seller.getId());
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(messageChildren);
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
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
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
    @GetMapping("/api/messages")
    public ResponseEntity<ApiResponse> getMessages(@RequestParam(name = "idMm") Integer idMm, @RequestHeader(name = "Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                throw new AuthException("Please log in to view messages");
            }
            List<MessageChild> messageChildren = messageChildRepository.findByIdMmOrderByCreatedAtAsc(idMm);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(messageChildren);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse apiResponse = new ApiResponse();
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
