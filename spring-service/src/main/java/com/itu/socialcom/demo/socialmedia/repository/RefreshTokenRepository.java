package com.itu.socialcom.demo.socialmedia.repository;

import com.itu.socialcom.demo.socialmedia.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    /**
     * Find all refresh tokens for a managed page ordered by expiration date (newest first)
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.managedPageId = :pageId ORDER BY rt.expirationDate DESC")
    List<RefreshToken> findByManagedPageIdOrderByExpirationDesc(@Param("pageId") Long pageId);
    
    /**
     * Find valid (non-expired) refresh token for a managed page
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.managedPageId = :pageId AND rt.expirationDate > :currentTime ORDER BY rt.expirationDate DESC")
    Optional<RefreshToken> findValidRefreshTokenByPageId(@Param("pageId") Long pageId, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find the most recent refresh token for a managed page (regardless of expiration)
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.managedPageId = :pageId ORDER BY rt.createdAt DESC LIMIT 1")
    Optional<RefreshToken> findLatestRefreshTokenByPageId(@Param("pageId") Long pageId);
    
    /**
     * Find refresh tokens expiring within a specified time threshold
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expirationDate BETWEEN :now AND :threshold ORDER BY rt.expirationDate ASC")
    List<RefreshToken> findTokensExpiringBefore(@Param("threshold") LocalDateTime threshold, @Param("now") LocalDateTime now);
    
    /**
     * Find expired refresh tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expirationDate < :currentTime")
    List<RefreshToken> findExpiredTokens(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find refresh tokens by platform
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.platform = :platform")
    List<RefreshToken> findByPlatform(@Param("platform") String platform);
    
    /**
     * Find valid refresh tokens by platform
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.platform = :platform AND rt.expirationDate > :currentTime")
    List<RefreshToken> findValidTokensByPlatform(@Param("platform") String platform, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Delete all refresh tokens for a managed page
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.managedPageId = :pageId")
    void deleteByManagedPageId(@Param("pageId") Long pageId);
    
    /**
     * Delete expired refresh tokens
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expirationDate < :currentTime")
    void deleteExpiredTokens(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Mark tokens as expired by updating their expiration date (for audit trail)
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.expirationDate = :expiredTime, rt.updatedAt = :updatedAt WHERE rt.managedPageId = :pageId AND rt.expirationDate > :expiredTime")
    void markTokensAsExpired(@Param("pageId") Long pageId, @Param("expiredTime") LocalDateTime expiredTime, @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * Count valid refresh tokens for a managed page
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.managedPageId = :pageId AND rt.expirationDate > :currentTime")
    long countValidTokensByPageId(@Param("pageId") Long pageId, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Check if a managed page has valid refresh tokens
     */
    @Query("SELECT COUNT(rt) > 0 FROM RefreshToken rt WHERE rt.managedPageId = :pageId AND rt.expirationDate > :currentTime")
    boolean hasValidRefreshTokens(@Param("pageId") Long pageId, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find refresh tokens that need attention (expiring within specified days)
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expirationDate BETWEEN :now AND :warningThreshold")
    List<RefreshToken> findTokensNeedingAttention(@Param("now") LocalDateTime now, @Param("warningThreshold") LocalDateTime warningThreshold);
    
    /**
     * Find refresh tokens by managed page and platform
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.managedPageId = :pageId AND rt.platformId = :platformId ORDER BY rt.expirationDate DESC")
    List<RefreshToken> findByPageIdAndPlatformId(@Param("pageId") Long pageId, @Param("platformId") Long platformId);
    
    /**
     * Find pages with expiring refresh tokens (for proactive re-authentication)
     */
    @Query("SELECT DISTINCT rt.managedPageId FROM RefreshToken rt WHERE rt.expirationDate BETWEEN :now AND :threshold")
    List<Long> findPageIdsWithExpiringRefreshTokens(@Param("now") LocalDateTime now, @Param("threshold") LocalDateTime threshold);
    
    /**
     * Cleanup old expired refresh tokens (keep only the most recent expired token for audit)
     */
    @Modifying
    @Query("""
        DELETE FROM RefreshToken rt WHERE rt.managedPageId = :pageId 
        AND rt.expirationDate < :currentTime 
        AND rt.id NOT IN (
            SELECT rt2.id FROM RefreshToken rt2 
            WHERE rt2.managedPageId = :pageId 
            AND rt2.expirationDate < :currentTime 
            ORDER BY rt2.expirationDate DESC 
            LIMIT 1
        )
    """)
    void cleanupOldExpiredTokens(@Param("pageId") Long pageId, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find refresh tokens that can be used for token rotation
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.managedPageId = :pageId AND rt.expirationDate > :currentTime ORDER BY rt.expirationDate DESC LIMIT 1")
    Optional<RefreshToken> findUsableRefreshTokenByPageId(@Param("pageId") Long pageId, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Count total refresh tokens by platform (for monitoring)
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.platform = :platform")
    long countByPlatform(@Param("platform") String platform);
    
    /**
     * Find refresh tokens that will expire soon and need user notification
     */
    @Query("""
        SELECT rt FROM RefreshToken rt 
        WHERE rt.expirationDate BETWEEN :now AND :notificationThreshold 
        AND rt.managedPageId IN (
            SELECT mp.id FROM ManagedPage mp WHERE mp.status = 'active'
        )
    """)
    List<RefreshToken> findTokensNeedingUserNotification(@Param("now") LocalDateTime now, @Param("notificationThreshold") LocalDateTime notificationThreshold);
}