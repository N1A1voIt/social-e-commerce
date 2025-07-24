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
     * Find all access tokens for a refresh token ordered by expiration date (newest first)
     */
    @Query("SELECT at FROM AccessToken at WHERE at.idRefreshToken = :refreshTokenId ORDER BY at.expirationDate DESC")
    List<AccessToken> findByRefreshTokenIdOrderByExpirationDesc(@Param("refreshTokenId") Long refreshTokenId);
    
    /**
     * Find valid (non-expired) access token for a refresh token
     */
    @Query("SELECT at FROM AccessToken at WHERE at.idRefreshToken = :refreshTokenId AND at.expirationDate > :currentTime ORDER BY at.expirationDate DESC")
    Optional<AccessToken> findValidTokenByRefreshTokenId(@Param("refreshTokenId") Long refreshTokenId, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find the most recent access token for a refresh token (regardless of expiration)
     */
    @Query("SELECT at FROM AccessToken at WHERE at.idRefreshToken = :refreshTokenId ORDER BY at.createdAt DESC LIMIT 1")
    Optional<AccessToken> findLatestTokenByRefreshTokenId(@Param("refreshTokenId") Long refreshTokenId);
    
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
     * Delete all access tokens for a refresh token
     */
    @Modifying
    @Query("DELETE FROM AccessToken at WHERE at.idRefreshToken = :refreshTokenId")
    void deleteByRefreshTokenId(@Param("refreshTokenId") Long refreshTokenId);
    
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
    @Query("UPDATE AccessToken at SET at.expirationDate = :expiredTime WHERE at.idRefreshToken = :refreshTokenId AND at.expirationDate > :expiredTime")
    void markTokensAsExpired(@Param("refreshTokenId") Long refreshTokenId, @Param("expiredTime") LocalDateTime expiredTime);
    
    /**
     * Count valid tokens for a refresh token
     */
    @Query("SELECT COUNT(at) FROM AccessToken at WHERE at.idRefreshToken = :refreshTokenId AND at.expirationDate > :currentTime")
    long countValidTokensByRefreshTokenId(@Param("refreshTokenId") Long refreshTokenId, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Check if a refresh token has valid access tokens
     */
    @Query("SELECT COUNT(at) > 0 FROM AccessToken at WHERE at.idRefreshToken = :refreshTokenId AND at.expirationDate > :currentTime")
    boolean hasValidTokens(@Param("refreshTokenId") Long refreshTokenId, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find access tokens that need rotation (expiring within specified hours)
     */
    @Query("SELECT at FROM AccessToken at WHERE at.expirationDate BETWEEN :now AND :rotationThreshold")
    List<AccessToken> findTokensNeedingRotation(@Param("now") LocalDateTime now, @Param("rotationThreshold") LocalDateTime rotationThreshold);
    
    /**
     * Cleanup old expired tokens (keep only the most recent expired token for audit)
     */
    @Modifying
    @Query("""
        DELETE FROM AccessToken at WHERE at.idRefreshToken = :refreshTokenId 
        AND at.expirationDate < :currentTime 
        AND at.id NOT IN (
            SELECT at2.id FROM AccessToken at2 
            WHERE at2.idRefreshToken = :refreshTokenId 
            AND at2.expirationDate < :currentTime 
            ORDER BY at2.expirationDate DESC 
            LIMIT 1
        )
    """)
    void cleanupOldExpiredTokens(@Param("refreshTokenId") Long refreshTokenId, @Param("currentTime") LocalDateTime currentTime);
}