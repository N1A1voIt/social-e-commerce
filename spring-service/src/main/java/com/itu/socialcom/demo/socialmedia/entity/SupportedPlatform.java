package com.itu.socialcom.demo.socialmedia.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

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
    
    @Column(name = "label", length = 250)
    private String label;
    
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
    }
}