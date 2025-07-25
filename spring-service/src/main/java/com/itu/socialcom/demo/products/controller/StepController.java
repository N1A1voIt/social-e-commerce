package com.itu.socialcom.demo.products.controller;

import com.itu.socialcom.demo.products.dto.CreationStepsDTO;
import com.itu.socialcom.demo.products.service.StepsCreationService;
import com.itu.socialcom.demo.products.service.StepsCreationServiceImpl;
import com.itu.socialcom.demo.products.service.StepsRecoveryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/steps")
public class StepController {
    @Autowired
    private StepsCreationServiceImpl stepsCreationService;
    @Autowired
    private StepsRecoveryImpl stepsRecovery;
     @PostMapping("/step1")
     public ResponseEntity<CreationStepsDTO> saveStep1(@RequestBody CreationStepsDTO creationStepsDTO, @RequestHeader("Authorization") String token) {
         try {
             return ResponseEntity.ok(stepsCreationService.saveStep1(creationStepsDTO, token));
         } catch (Exception e) {
             return ResponseEntity.status(400).body(null); // or a custom error response
         }
     }

     @PostMapping("/step2")
     public ResponseEntity<CreationStepsDTO> saveStep2(@RequestBody CreationStepsDTO creationStepsDTO, @RequestHeader("Authorization") String token) {
         try {
             return ResponseEntity.ok(stepsCreationService.saveStep2(creationStepsDTO, token));
         } catch (Exception e) {
             return ResponseEntity.status(400).body(null);
         }
     }

    @GetMapping("/recovery")
    public ResponseEntity<CreationStepsDTO> recover(@RequestHeader("Authorization") String token) {
        try {
            CreationStepsDTO creationStepsDTO = stepsRecovery.recoverStep1(token);
            return ResponseEntity.ok(creationStepsDTO);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

}
