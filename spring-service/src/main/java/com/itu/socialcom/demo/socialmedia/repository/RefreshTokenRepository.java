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
     * Find valid (non-expired and non-revoked) refresh token for a managed page
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.managedPageId = :pageId AND rt.expirationDate > :currentTime AND rt.revoked = false ORDER BY rt.expirationDate DESC")
    Optional<RefreshToken> findValidRefreshTokenByPageId(@Param("pageId") Long pageId, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find the most recent refresh token for a managed page (regardless of expiration)
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.managedPageId = :pageId ORDER BY rt.createdAt DESC LIMIT 1")
    Optional<RefreshToken> findLatestRefreshTokenByPageId(@Param("pageId") Long pageId);
    
    /**
     * Find refresh tokens expiring within a specified time threshold
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expirationDate BETWEEN :now AND :threshold AND rt.revoked = false ORDER BY rt.expirationDate ASC")
    List<RefreshToken> findTokensExpiringBefore(@Param("threshold") LocalDateTime threshold, @Param("now") LocalDateTime now);
    
    /**
     * Find expired refresh tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expirationDate < :currentTime")
    List<RefreshToken> findExpiredTokens(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find refresh tokens that can be used for token rotation
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.managedPageId = :pageId AND rt.expirationDate > :currentTime AND rt.revoked = false ORDER BY rt.expirationDate DESC LIMIT 1")
    Optional<RefreshToken> findUsableRefreshTokenByPageId(@Param("pageId") Long pageId, @Param("currentTime") LocalDateTime currentTime);
    
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
     * Mark tokens as revoked (for audit trail)
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.managedPageId = :pageId AND rt.revoked = false")
    void revokeTokensByPageId(@Param("pageId") Long pageId);
    
    /**
     * Mark specific token as revoked
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.id = :tokenId")
    void revokeToken(@Param("tokenId") Long tokenId);
    
    /**
     * Count valid tokens for a managed page
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.managedPageId = :pageId AND rt.expirationDate > :currentTime AND rt.revoked = false")
    long countValidTokensByPageId(@Param("pageId") Long pageId, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Check if a managed page has valid refresh tokens
     */
    @Query("SELECT COUNT(rt) > 0 FROM RefreshToken rt WHERE rt.managedPageId = :pageId AND rt.expirationDate > :currentTime AND rt.revoked = false")
    boolean hasValidTokens(@Param("pageId") Long pageId, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find refresh tokens that need rotation (expiring within specified hours)
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expirationDate BETWEEN :now AND :rotationThreshold AND rt.revoked = false")
    List<RefreshToken> findTokensNeedingRotation(@Param("now") LocalDateTime now, @Param("rotationThreshold") LocalDateTime rotationThreshold);
    
    /**
     * Find pages with expiring refresh tokens (for proactive re-authentication)
     */
    @Query("SELECT DISTINCT rt.managedPageId FROM RefreshToken rt WHERE rt.expirationDate BETWEEN :now AND :threshold AND rt.revoked = false")
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
     * Find non-revoked refresh tokens by token string (for validation)
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.refreshToken = :token AND rt.revoked = false")
    Optional<RefreshToken> findByTokenAndNotRevoked(@Param("token") String token);
}