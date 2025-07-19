package com.itu.socialcom.demo.socialmedia.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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
    
    @Column(name = "access_tokens", unique = true)
    private String accessToken;
    
    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;
    
    @Column(name = "platform", nullable = false, length = 250)
    private String platform;
    
    @Column(name = "id_sp", nullable = false)
    private Long platformId;
    
    @Column(name = "id_mp", nullable = false)
    private Long managedPageId;
    
    // Audit fields
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
}