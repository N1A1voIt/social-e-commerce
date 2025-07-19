package com.itu.socialcom.demo.socialmedia.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing supported social media platforms.
 * This entity does not use JPA relationships to maintain manual control over data access.
 */
@Data
@Entity
@Getter
@Setter
@Table(name = "supported_platforms_v2")
public class SupportedPlatform {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sp")
    private Long id;
    
    /**
     * Platform label/identifier (e.g., "facebook", "instagram", "x", "thread")
     */
    @Column(name = "label", length = 250)
    private String label;
    
    /**
     * Get the platform type enum from the label
     * @return PlatformType enum value
     */
    public PlatformType getPlatformType() {
        return PlatformType.fromLabel(this.label);
    }
    
    /**
     * Check if this platform matches the given type
     * @param type the platform type to check
     * @return true if matches, false otherwise
     */
    public boolean isPlatform(PlatformType type) {
        return type.getLabel().equalsIgnoreCase(this.label);
    }
    
    /**
     * Enum for supported platform types
     */
    public enum PlatformType {
        FACEBOOK("facebook"),
        INSTAGRAM("instagram"),
        X("x"),
        THREAD("thread");
        
        private final String label;
        
        PlatformType(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
        
        public static PlatformType fromLabel(String label) {
            for (PlatformType type : values()) {
                if (type.label.equalsIgnoreCase(label)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown platform label: " + label);
        }
        
        /**
         * Check if the platform supports OAuth 2.0
         * @return true if OAuth 2.0 is supported
         */
        public boolean supportsOAuth2() {
            return this != THREAD; // Assuming Thread might use different auth
        }
        
        /**
         * Get the display name for the platform
         * @return user-friendly display name
         */
        public String getDisplayName() {
            return switch (this) {
                case FACEBOOK -> "Facebook";
                case INSTAGRAM -> "Instagram";
                case X -> "X (Twitter)";
                case THREAD -> "Threads";
            };
        }
    }
}