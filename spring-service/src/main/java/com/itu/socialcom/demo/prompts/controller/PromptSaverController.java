package com.itu.socialcom.demo.prompts.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.prompts.dto.PlatformInfo;
import com.itu.socialcom.demo.prompts.dto.PromptSaverRequest;
import com.itu.socialcom.demo.prompts.dto.PromptSaverResponse;
import com.itu.socialcom.demo.prompts.service.PromptSaverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Controller for managing AI prompts per platform and seller.
 * All operations are scoped to the authenticated seller only.
 */
@Slf4j
@RestController
@RequestMapping("/api/prompts")
@RequiredArgsConstructor
@Validated
public class PromptSaverController {

    private final PromptSaverService promptSaverService;
    private final TokenV2ServiceImpl tokenV2Service;

    /**
     * Get all prompts for the authenticated seller
     * 
     * @param token Authorization token
     * @return List of prompts for the authenticated seller
     */
    @GetMapping
    public ResponseEntity<List<PromptSaverResponse>> getAllPrompts(@RequestHeader("Authorization") String token) {
        try {
            Seller seller = getCurrentSeller(token);
            log.info("Fetching all prompts for seller: {}", seller.getId());
            
            List<PromptSaverResponse> prompts = promptSaverService.getPromptsBySeller(seller.getId());
            return ResponseEntity.ok(prompts);
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error fetching prompts: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a specific prompt by platform ID for the authenticated seller
     * 
     * @param platformId The platform ID
     * @param token Authorization token
     * @return The prompt for the specified platform, if exists
     */
    @GetMapping("/platform/{platformId}")
    public ResponseEntity<PromptSaverResponse> getPromptByPlatform(
            @PathVariable Long platformId,
            @RequestHeader("Authorization") String token) {
        try {
            Seller seller = getCurrentSeller(token);
            log.info("Fetching prompt for seller: {} and platform: {}", seller.getId(), platformId);
            
            Optional<PromptSaverResponse> prompt = promptSaverService.getPromptBySellerAndPlatform(seller.getId(), platformId);
            
            if (prompt.isPresent()) {
                return ResponseEntity.ok(prompt.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error fetching prompt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create or update a prompt for a specific platform (authenticated seller only)
     * 
     * @param request The prompt request containing prompt text and platform ID
     * @param token Authorization token
     * @return The created/updated prompt
     */
    @PostMapping
    public ResponseEntity<PromptSaverResponse> createOrUpdatePrompt(
            @Valid @RequestBody PromptSaverRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            Seller seller = getCurrentSeller(token);
            log.info("Creating/updating prompt for seller: {} and platform: {}", seller.getId(), request.getPlatformId());
            
            PromptSaverResponse response = promptSaverService.createOrUpdatePrompt(seller.getId(), request);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating/updating prompt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a prompt for a specific platform (authenticated seller only)
     * 
     * @param platformId The platform ID
     * @param token Authorization token
     * @return No content if successful, 404 if not found
     */
    @DeleteMapping("/platform/{platformId}")
    public ResponseEntity<Void> deletePrompt(
            @PathVariable Long platformId,
            @RequestHeader("Authorization") String token) {
        try {
            Seller seller = getCurrentSeller(token);
            log.info("Deleting prompt for seller: {} and platform: {}", seller.getId(), platformId);
            
            boolean deleted = promptSaverService.deletePrompt(seller.getId(), platformId);
            
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error deleting prompt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all available platforms
     * 
     * @param token Authorization token
     * @return List of available platforms
     */
    @GetMapping("/platforms")
    public ResponseEntity<List<PlatformInfo>> getAllPlatforms(@RequestHeader("Authorization") String token) {
        try {
            // Validate token but platforms are the same for all users
            getCurrentSeller(token);
            log.info("Fetching all available platforms");
            
            List<PlatformInfo> platforms = promptSaverService.getAllPlatforms();
            return ResponseEntity.ok(platforms);
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error fetching platforms: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * Helper method to get the current authenticated seller from the token
     * 
     * @param token Authorization token
     * @return Authenticated seller
     * @throws IllegalStateException if token is invalid or seller not found
     */
    private Seller getCurrentSeller(@RequestHeader("Authorization") String token) {
        return tokenV2Service.findSellerByToken(token.replace("Bearer ",""))
            .orElseThrow(() -> new IllegalStateException("Authenticated user is not a valid seller"));
    }
}
