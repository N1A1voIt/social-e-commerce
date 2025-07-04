package com.itu.socialcom.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiTest {
    @GetMapping("/api/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Hello World!");
    }
}
