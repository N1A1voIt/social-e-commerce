package com.itu.socialcom.demo.socialmedia.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing OAuth refresh tokens for social media platforms.
 * This entity does not use JPA relationships to maintain manual control over data access.
 */
@Data
@Entity
@Getter
@Setter
@Table(name = "pat_refresh_tokens")
public class RefreshToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prt")
    private Long id;
    
    /**
     * The actual refresh token string (should be encrypted in production)
     */
    @Column(name = "token", nullable = false, columnDefinition = "TEXT")
    private String refreshToken;
    
    /**
     * When this refresh token expires
     */
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expirationDate;

    
    /**
     * Foreign key reference to managed_pages table
     * Note: No JPA relationship annotation to maintain manual control
     */
    @Column(name = "id_mp", nullable = false)
    private Long managedPageId;
    
    // Audit fields for tracking creation and updates
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "revoked")
    private boolean revoked;

    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        revoked = false;
    }
    
    @PreUpdate
    protected void onUpdate() {

    }
    
    /**
     * Check if the refresh token is expired
     * @return true if token is expired, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationDate);
    }
    
    /**
     * Check if the refresh token will expire within the specified days
     * @param days number of days to check ahead
     * @return true if token will expire within the specified time
     */
    public boolean isExpiringWithin(int days) {
        return LocalDateTime.now().plusDays(days).isAfter(expirationDate);
    }
    
    /**
     * Check if the refresh token is still valid (not expired)
     * @return true if token is valid, false if expired
     */
    public boolean isValid() {
        return !isExpired();
    }
    
    /**
     * Get the remaining time until expiration in days
     * @return days until expiration, negative if already expired
     */
    public long getDaysUntilExpiration() {
        return java.time.Duration.between(LocalDateTime.now(), expirationDate).toDays();
    }
    
    /**
     * Check if the refresh token needs rotation (expires within specified days)
     * @param daysThreshold number of days before expiration to trigger rotation
     * @return true if token should be rotated, false otherwise
     */
    public boolean needsRotation(int daysThreshold) {
        return isExpiringWithin(daysThreshold);
    }
}