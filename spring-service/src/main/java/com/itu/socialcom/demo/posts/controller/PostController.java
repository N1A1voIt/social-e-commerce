package com.itu.socialcom.demo.posts.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2Service;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.posts.dto.*;
import com.itu.socialcom.demo.posts.entity.Post;
import com.itu.socialcom.demo.posts.exceptions.SellerNotLogged;
import com.itu.socialcom.demo.posts.repository.PostChildMediaRepository;
import com.itu.socialcom.demo.posts.repository.PostRepository;
import com.itu.socialcom.demo.posts.services.etl.PostRetriever;
import com.itu.socialcom.demo.posts.services.get.PostGetter;
import com.itu.socialcom.demo.posts.services.save.FacebookPostSaver;
import com.itu.socialcom.demo.posts.services.save.GeneralPostSaver;
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
    PostRetriever postRetriever;
    @Autowired
    PostGetter postGetter;
    @Autowired
    PostChildMediaRepository postChildMediaRepository;
    @Autowired
    FacebookPostSaver facebookPostSaver;
    @Autowired
    GeneralPostSaver generalPostSaver;
    @Autowired
    PostRepository postRepository;
    @GetMapping("/loads")
    public ResponseEntity<List<Post>> extractPost(@RequestHeader(name = "Authorization") String token) {
        try{
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {throw new SellerNotLogged("Seller not found");}
            ExtractorArgs extractorArgs = new ExtractorArgs();
            extractorArgs.setSeller(seller);
            return ResponseEntity.ok(postRetriever.retrievePosts(extractorArgs));
        } catch (SellerNotLogged e){
            return ResponseEntity.status(400).body(null);
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
    @GetMapping()
    public ResponseEntity<List<DisplayPost>> extractPosts(@RequestHeader(name = "Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {throw new SellerNotLogged("Seller not found");}
            return ResponseEntity.ok(postGetter.mapToDisplayPosts(postChildMediaRepository.findAll()));
        } catch (SellerNotLogged e){
            return ResponseEntity.status(400).body(null);
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/make-post")
    public ResponseEntity<?> testMedia(@RequestBody SavePostArgs args,@RequestHeader("Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {throw new SellerNotLogged("Seller not found");}
            return ResponseEntity.ok(generalPostSaver.func(args,seller));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/fetch-mother")
    public ResponseEntity<List<MotherPostDisplay>> fetchMother(@RequestHeader("Authorization") String token) {
        try {
          Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {throw new SellerNotLogged("Seller not found");}
            return ResponseEntity.ok(postRetriever.motherPostDisplays(postRepository.findAll()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}
