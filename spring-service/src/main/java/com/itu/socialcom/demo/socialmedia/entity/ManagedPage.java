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
@Table(name = "managed_pages")
public class ManagedPage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mp")
    private Long id;
    
    @Column(name = "d_status", length = 50)
    private String status;
    
    @Column(name = "platform_identifier", nullable = false)
    private String platformIdentifier;
    
    @Column(name = "page_title", nullable = false)
    private String pageTitle;
    
    @Column(name = "associated_media")
    private String associatedMedia;
    
    @Column(name = "link_to_platform", nullable = false)
    private String linkToPlatform;
    
    @Column(name = "id_sp", nullable = false)
    private Long platformId;
    
    @Column(name = "id_seller", nullable = false)
    private Long sellerId;
    
    // Audit fields (not in original schema but useful for tracking)
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
}