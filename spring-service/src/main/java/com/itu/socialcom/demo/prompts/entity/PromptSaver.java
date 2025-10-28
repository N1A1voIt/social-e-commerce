package com.itu.socialcom.demo.prompts.entity;

import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.socialmedia.entity.SupportedPlatform;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing saved prompts for AI content generation per platform and seller.
 * Each seller can have one prompt per platform.
 */
@Data
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "prompt_saver", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"id_seller", "id_platform"}))
public class PromptSaver {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prompt")
    private Long id;
    
    /**
     * The prompt text content for AI generation
     */
    @Column(name = "prompt", nullable = false, columnDefinition = "TEXT")
    private String prompt;
    
    /**
     * Seller ID - references seller_v2 table
     */
    @Column(name = "id_seller", nullable = false)
    private Long sellerId;
    
    /**
     * Platform ID - references supported_platforms_v2 table
     */
    @Column(name = "id_platform", nullable = false)
    private Long platformId;
    
    /**
     * Creation timestamp - automatically set on creation
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * JPA relationship to Seller entity
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seller", insertable = false, updatable = false)
    private Seller seller;
    
    /**
     * JPA relationship to SupportedPlatform entity
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_platform", insertable = false, updatable = false)
    private SupportedPlatform platform;
    
    /**
     * Constructor for creating new prompt
     */
    public PromptSaver(String prompt, Long sellerId, Long platformId) {
        this.prompt = prompt;
        this.sellerId = sellerId;
        this.platformId = platformId;
    }
}
