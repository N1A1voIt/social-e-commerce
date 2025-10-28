package com.itu.socialcom.demo.prompts.service;

import com.itu.socialcom.demo.prompts.dto.PlatformInfo;
import com.itu.socialcom.demo.prompts.dto.PromptSaverRequest;
import com.itu.socialcom.demo.prompts.dto.PromptSaverResponse;
import com.itu.socialcom.demo.prompts.entity.PromptSaver;
import com.itu.socialcom.demo.prompts.repository.PromptSaverRepository;
import com.itu.socialcom.demo.socialmedia.entity.SupportedPlatform;
import com.itu.socialcom.demo.socialmedia.repository.SupportedPlatformRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing PromptSaver operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptSaverService {
    
    private final PromptSaverRepository promptSaverRepository;
    private final SupportedPlatformRepository supportedPlatformRepository;
    
    /**
     * Create or update a prompt for a seller and platform
     * @param sellerId the seller ID
     * @param request the prompt request
     * @return the created/updated prompt response
     * @throws IllegalArgumentException if platform doesn't exist
     */
    @Transactional
    public PromptSaverResponse createOrUpdatePrompt(Long sellerId, PromptSaverRequest request) {
        log.info("Creating or updating prompt for seller {} and platform {}", sellerId, request.getPlatformId());
        
        // Validate platform exists
        if (!supportedPlatformRepository.existsById(request.getPlatformId())) {
            throw new IllegalArgumentException("Platform with ID " + request.getPlatformId() + " does not exist");
        }
        
        // Check if prompt already exists for this seller and platform
        Optional<PromptSaver> existingPrompt = promptSaverRepository
                .findBySellerIdAndPlatformId(sellerId, request.getPlatformId());
        
        PromptSaver promptSaver;
        if (existingPrompt.isPresent()) {
            // Update existing prompt
            promptSaver = existingPrompt.get();
            promptSaver.setPrompt(request.getPrompt());
            log.info("Updating existing prompt with ID {}", promptSaver.getId());
        } else {
            // Create new prompt
            promptSaver = new PromptSaver(request.getPrompt(), sellerId, request.getPlatformId());
            log.info("Creating new prompt for seller {} and platform {}", sellerId, request.getPlatformId());
        }
        
        try {
            promptSaver = promptSaverRepository.save(promptSaver);
            return convertToResponse(promptSaver);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when saving prompt: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid data provided for prompt creation", e);
        }
    }
    
    /**
     * Get all prompts for a specific seller
     * @param sellerId the seller ID
     * @return list of prompt responses
     */
    @Transactional(readOnly = true)
    public List<PromptSaverResponse> getPromptsBySeller(Long sellerId) {
        log.info("Fetching all prompts for seller {}", sellerId);
        
        List<PromptSaver> prompts = promptSaverRepository.findBySellerIdWithPlatform(sellerId);
        return prompts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get a specific prompt by seller and platform
     * @param sellerId the seller ID
     * @param platformId the platform ID
     * @return optional prompt response
     */
    @Transactional(readOnly = true)
    public Optional<PromptSaverResponse> getPromptBySellerAndPlatform(Long sellerId, Long platformId) {
        log.info("Fetching prompt for seller {} and platform {}", sellerId, platformId);
        
        return promptSaverRepository.findBySellerIdAndPlatformId(sellerId, platformId)
                .map(this::convertToResponse);
    }
    
    /**
     * Delete a prompt by seller and platform
     * @param sellerId the seller ID
     * @param platformId the platform ID
     * @return true if deleted, false if not found
     */
    @Transactional
    public boolean deletePrompt(Long sellerId, Long platformId) {
        log.info("Deleting prompt for seller {} and platform {}", sellerId, platformId);
        
        if (promptSaverRepository.existsBySellerIdAndPlatformId(sellerId, platformId)) {
            promptSaverRepository.deleteBySellerIdAndPlatformId(sellerId, platformId);
            log.info("Successfully deleted prompt for seller {} and platform {}", sellerId, platformId);
            return true;
        }
        
        log.warn("No prompt found for seller {} and platform {}", sellerId, platformId);
        return false;
    }
    
    /**
     * Get all available platforms
     * @return list of platform information
     */
    @Transactional(readOnly = true)
    public List<PlatformInfo> getAllPlatforms() {
        log.info("Fetching all available platforms");
        
        return supportedPlatformRepository.findAll().stream()
                .map(platform -> new PlatformInfo(platform.getId(), platform.getLabel()))
                .collect(Collectors.toList());
    }
    
    /**
     * Convert PromptSaver entity to response DTO
     * @param promptSaver the entity
     * @return the response DTO
     */
    private PromptSaverResponse convertToResponse(PromptSaver promptSaver) {
        String platformLabel = promptSaver.getPlatform() != null 
                ? promptSaver.getPlatform().getLabel() 
                : null;
        
        return new PromptSaverResponse(
                promptSaver.getId(),
                promptSaver.getPrompt(),
                promptSaver.getSellerId(),
                promptSaver.getPlatformId(),
                platformLabel,
                promptSaver.getCreatedAt()
        );
    }
}
