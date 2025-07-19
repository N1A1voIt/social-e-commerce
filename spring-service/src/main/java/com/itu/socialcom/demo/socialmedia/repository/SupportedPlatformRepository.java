package com.itu.socialcom.demo.socialmedia.repository;

import com.itu.socialcom.demo.socialmedia.entity.SupportedPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportedPlatformRepository extends JpaRepository<SupportedPlatform, Long> {
    
    /**
     * Find platform by label (case-insensitive)
     */
    @Query("SELECT sp FROM SupportedPlatform sp WHERE LOWER(sp.label) = LOWER(:label)")
    Optional<SupportedPlatform> findByLabelIgnoreCase(@Param("label") String label);
    
    /**
     * Find platform by exact label match
     */
    Optional<SupportedPlatform> findByLabel(String label);
    
    /**
     * Check if platform exists by label
     */
    @Query("SELECT COUNT(sp) > 0 FROM SupportedPlatform sp WHERE LOWER(sp.label) = LOWER(:label)")
    boolean existsByLabelIgnoreCase(@Param("label") String label);
    
    /**
     * Find all platforms ordered by label
     */
    @Query("SELECT sp FROM SupportedPlatform sp ORDER BY sp.label ASC")
    List<SupportedPlatform> findAllOrderByLabel();
    
    /**
     * Find platforms by partial label match (for search functionality)
     */
    @Query("SELECT sp FROM SupportedPlatform sp WHERE LOWER(sp.label) LIKE LOWER(CONCAT('%', :labelPart, '%'))")
    List<SupportedPlatform> findByLabelContainingIgnoreCase(@Param("labelPart") String labelPart);
    
    /**
     * Get platform ID by label
     */
    @Query("SELECT sp.id FROM SupportedPlatform sp WHERE LOWER(sp.label) = LOWER(:label)")
    Optional<Long> findIdByLabel(@Param("label") String label);
    
    /**
     * Get platform label by ID
     */
    @Query("SELECT sp.label FROM SupportedPlatform sp WHERE sp.id = :id")
    Optional<String> findLabelById(@Param("id") Long id);
}