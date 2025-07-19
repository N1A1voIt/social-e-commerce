package com.itu.socialcom.demo.socialmedia.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing OAuth access tokens for social media platforms.
 * This entity does not use JPA relationships to maintain manual control over data access.
 */
@Data
@Entity
@Getter
@Setter
@Table(name = "pat_access_tokens")
public class AccessToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * The actual access token string (should be encrypted in production)
     */
    @Column(name = "access_tokens", unique = true, columnDefinition = "TEXT")
    private String accessToken;
    
    /**
     * When this access token expires
     */
    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;
    
    /**
     * Platform identifier (e.g., "facebook", "instagram", "x")
     */
    @Column(name = "platform", nullable = false, length = 250)
    private String platform;
    
    /**
     * Foreign key reference to supported_platforms_v2 table
     * Note: No JPA relationship annotation to maintain manual control
     */
    @Column(name = "id_sp", nullable = false)
    private Long platformId;
    
    /**
     * Foreign key reference to managed_pages table
     * Note: No JPA relationship annotation to maintain manual control
     */
    @Column(name = "id_mp", nullable = false)
    private Long managedPageId;
    
    // Audit fields for tracking creation and updates
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if the access token is expired
     * @return true if token is expired, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationDate);
    }
    
    /**
     * Check if the access token will expire within the specified minutes
     * @param minutes number of minutes to check ahead
     * @return true if token will expire within the specified time
     */
    public boolean isExpiringWithin(int minutes) {
        return LocalDateTime.now().plusMinutes(minutes).isAfter(expirationDate);
    }
    
    /**
     * Check if the access token is still valid (not expired)
     * @return true if token is valid, false if expired
     */
    public boolean isValid() {
        return !isExpired();
    }
    
    /**
     * Get the remaining time until expiration in minutes
     * @return minutes until expiration, negative if already expired
     */
    public long getMinutesUntilExpiration() {
        return java.time.Duration.between(LocalDateTime.now(), expirationDate).toMinutes();
    }
}