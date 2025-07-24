package com.itu.socialcom.demo.products.controller;

import com.itu.socialcom.demo.products.dto.CreationStepsDTO;
import com.itu.socialcom.demo.products.service.StepsCreationService;
import com.itu.socialcom.demo.products.service.StepsCreationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/steps")
public class StepController {
    @Autowired
    private StepsCreationServiceImpl stepsCreationService;
     @PostMapping("/step1")
     public ResponseEntity<CreationStepsDTO> saveStep1(@RequestBody CreationStepsDTO creationStepsDTO, @RequestHeader("Authorization") String token) {
         try {
             // Validate the token and save step 1
             return ResponseEntity.ok(stepsCreationService.saveStep1(creationStepsDTO, token));
         } catch (Exception e) {
             // Handle exceptions appropriately
             return ResponseEntity.status(400).body(null); // or a custom error response
         }
     }

    // Example method to handle step 2
     @PostMapping("/step2")
     public ResponseEntity<CreationStepsDTO> saveStep2(@RequestBody CreationStepsDTO creationStepsDTO, @RequestHeader("Authorization") String token) {
         try {
             // Validate the token and save step 2
             return ResponseEntity.ok(stepsCreationService.saveStep2(creationStepsDTO, token));
         } catch (Exception e) {
             // Handle exceptions appropriately
             return ResponseEntity.status(400).body(null); // or a custom error response
         }
     }
}
