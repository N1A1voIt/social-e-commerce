package com.itu.socialcom.demo.socialmedia.repository;

import com.itu.socialcom.demo.socialmedia.entity.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
    
    /**
     * Find all access tokens for a managed page ordered by expiration date (newest first)
     */
    @Query("SELECT at FROM AccessToken at WHERE at.managedPageId = :pageId ORDER BY at.expirationDate DESC")
    List<AccessToken> findByManagedPageIdOrderByExpirationDesc(@Param("pageId") Long pageId);
    
    /**
     * Find valid (non-expired) access token for a managed page
     */
    @Query("SELECT at FROM AccessToken at WHERE at.managedPageId = :pageId AND at.expirationDate > :currentTime ORDER BY at.expirationDate DESC")
    Optional<AccessToken> findValidTokenByPageId(@Param("pageId") Long pageId, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find the most recent access token for a managed page (regardless of expiration)
     */
    @Query("SELECT at FROM AccessToken at WHERE at.managedPageId = :pageId ORDER BY at.createdAt DESC LIMIT 1")
    Optional<AccessToken> findLatestTokenByPageId(@Param("pageId") Long pageId);
    
    /**
     * Find access tokens expiring within a specified time threshold
     */
    @Query("SELECT at FROM AccessToken at WHERE at.expirationDate BETWEEN :now AND :threshold ORDER BY at.expirationDate ASC")
    List<AccessToken> findTokensExpiringBefore(@Param("threshold") LocalDateTime threshold, @Param("now") LocalDateTime now);
    
    /**
     * Find expired access tokens
     */
    @Query("SELECT at FROM AccessToken at WHERE at.expirationDate < :currentTime")
    List<AccessToken> findExpiredTokens(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find access tokens by platform
     */
    @Query("SELECT at FROM AccessToken at WHERE at.platform = :platform")
    List<AccessToken> findByPlatform(@Param("platform") String platform);
    
    /**
     * Find access tokens by platform and expiration status
     */
    @Query("SELECT at FROM AccessToken at WHERE at.platform = :platform AND at.expirationDate > :currentTime")
    List<AccessToken> findValidTokensByPlatform(@Param("platform") String platform, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Delete all access tokens for a managed page
     */
    @Modifying
    @Query("DELETE FROM AccessToken at WHERE at.managedPageId = :pageId")
    void deleteByManagedPageId(@Param("pageId") Long pageId);
    
    /**
     * Delete expired access tokens
     */
    @Modifying
    @Query("DELETE FROM AccessToken at WHERE at.expirationDate < :currentTime")
    void deleteExpiredTokens(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Mark tokens as expired by updating their expiration date (for audit trail)
     */
    @Modifying
    @Query("UPDATE AccessToken at SET at.expirationDate = :expiredTime, at.updatedAt = :updatedAt WHERE at.managedPageId = :pageId AND at.expirationDate > :expiredTime")
    void markTokensAsExpired(@Param("pageId") Long pageId, @Param("expiredTime") LocalDateTime expiredTime, @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * Count valid tokens for a managed page
     */
    @Query("SELECT COUNT(at) FROM AccessToken at WHERE at.managedPageId = :pageId AND at.expirationDate > :currentTime")
    long countValidTokensByPageId(@Param("pageId") Long pageId, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Check if a managed page has valid access tokens
     */
    @Query("SELECT COUNT(at) > 0 FROM AccessToken at WHERE at.managedPageId = :pageId AND at.expirationDate > :currentTime")
    boolean hasValidTokens(@Param("pageId") Long pageId, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find access tokens that need rotation (expiring within specified hours)
     */
    @Query("SELECT at FROM AccessToken at WHERE at.expirationDate BETWEEN :now AND :rotationThreshold")
    List<AccessToken> findTokensNeedingRotation(@Param("now") LocalDateTime now, @Param("rotationThreshold") LocalDateTime rotationThreshold);
    
    /**
     * Find access tokens by managed page and platform
     */
    @Query("SELECT at FROM AccessToken at WHERE at.managedPageId = :pageId AND at.platformId = :platformId ORDER BY at.expirationDate DESC")
    List<AccessToken> findByPageIdAndPlatformId(@Param("pageId") Long pageId, @Param("platformId") Long platformId);
    
    /**
     * Cleanup old expired tokens (keep only the most recent expired token for audit)
     */
    @Modifying
    @Query("""
        DELETE FROM AccessToken at WHERE at.managedPageId = :pageId 
        AND at.expirationDate < :currentTime 
        AND at.id NOT IN (
            SELECT at2.id FROM AccessToken at2 
            WHERE at2.managedPageId = :pageId 
            AND at2.expirationDate < :currentTime 
            ORDER BY at2.expirationDate DESC 
            LIMIT 1
        )
    """)
    void cleanupOldExpiredTokens(@Param("pageId") Long pageId, @Param("currentTime") LocalDateTime currentTime);
}