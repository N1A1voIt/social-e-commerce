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
@Table(name = "pat_refresh_tokens")
public class RefreshToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pat")
    private Long id;
    
    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;
    
    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;
    
    @Column(name = "platform", length = 250)
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
}