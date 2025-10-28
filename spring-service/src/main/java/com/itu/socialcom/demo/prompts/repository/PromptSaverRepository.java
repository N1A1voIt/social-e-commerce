package com.itu.socialcom.demo.prompts.repository;

import com.itu.socialcom.demo.prompts.entity.PromptSaver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PromptSaver entity
 */
@Repository
public interface PromptSaverRepository extends JpaRepository<PromptSaver, Long> {
    
    /**
     * Find all prompts for a specific seller
     * @param sellerId the seller ID
     * @return list of prompts for the seller
     */
    List<PromptSaver> findBySellerId(Long sellerId);
    
    /**
     * Find a specific prompt by seller ID and platform ID
     * @param sellerId the seller ID
     * @param platformId the platform ID
     * @return optional prompt
     */
    Optional<PromptSaver> findBySellerIdAndPlatformId(Long sellerId, Long platformId);
    
    /**
     * Check if a prompt exists for seller and platform
     * @param sellerId the seller ID
     * @param platformId the platform ID
     * @return true if exists, false otherwise
     */
    boolean existsBySellerIdAndPlatformId(Long sellerId, Long platformId);
    
    /**
     * Delete prompt by seller ID and platform ID
     * @param sellerId the seller ID
     * @param platformId the platform ID
     */
    void deleteBySellerIdAndPlatformId(Long sellerId, Long platformId);
    
    /**
     * Find prompts with platform details for a seller
     * @param sellerId the seller ID
     * @return list of prompt data with platform information
     */
    @Query("SELECT ps FROM PromptSaver ps JOIN FETCH ps.platform WHERE ps.sellerId = :sellerId")
    List<PromptSaver> findBySellerIdWithPlatform(@Param("sellerId") Long sellerId);
}
