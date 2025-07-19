package com.itu.socialcom.demo.socialmedia.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing a managed social media page.
 * This entity does not use JPA relationships to maintain manual control over data access.
 */
@Data
@Entity
@Getter
@Setter
@Table(name = "managed_pages")
public class ManagedPage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mp")
    private Long id;
    
    /**
     * Current status of the managed page (e.g., "active", "inactive")
     */
    @Column(name = "d_status", length = 50)
    private String status;
    
    /**
     * Platform-specific identifier for the page (e.g., Facebook page ID)
     */
    @Column(name = "platform_identifier", nullable = false, columnDefinition = "TEXT")
    private String platformIdentifier;
    
    /**
     * Display title/name of the page
     */
    @Column(name = "page_title", nullable = false, columnDefinition = "TEXT")
    private String pageTitle;
    
    /**
     * Associated media URLs or identifiers (profile pictures, cover photos, etc.)
     */
    @Column(name = "associated_media", columnDefinition = "TEXT")
    private String associatedMedia;
    
    /**
     * Direct link to the platform page
     */
    @Column(name = "link_to_platform", nullable = false, columnDefinition = "TEXT")
    private String linkToPlatform;
    
    /**
     * Foreign key reference to supported_platforms_v2 table
     * Note: No JPA relationship annotation to maintain manual control
     */
    @Column(name = "id_sp", nullable = false)
    private Long platformId;
    
    /**
     * Foreign key reference to seller_v2 table
     * Note: No JPA relationship annotation to maintain manual control
     */
    @Column(name = "id_seller", nullable = false)
    private Long sellerId;
    
    // Audit fields for tracking creation and updates
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "active"; // Default status
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if the managed page is currently active
     * @return true if status is "active", false otherwise
     */
    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }
    
    /**
     * Set the page status to active
     */
    public void setActive() {
        this.status = "active";
    }
    
    /**
     * Set the page status to inactive
     */
    public void setInactive() {
        this.status = "inactive";
    }
}