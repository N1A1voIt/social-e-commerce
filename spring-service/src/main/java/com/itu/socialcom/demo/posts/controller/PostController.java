package com.itu.socialcom.demo.posts.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2Service;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.posts.entity.Post;
import com.itu.socialcom.demo.posts.exceptions.SellerNotLogged;
import com.itu.socialcom.demo.posts.services.FacebookPostRetrieval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    TokenV2Service tokenV2Service;
    @Autowired
    FacebookPostRetrieval facebookPostRetrieval;
    @GetMapping()
    public ResponseEntity<List<Post>> extractPost(@RequestHeader(name = "Authorization") String token) {
        try{
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {throw new SellerNotLogged("Seller not found");}
            return ResponseEntity.ok(facebookPostRetrieval.loadPost(seller));
        } catch (SellerNotLogged e){
            return ResponseEntity.status(400).body(null);
        }
    }
}
