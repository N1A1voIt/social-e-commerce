package com.itu.socialcom.demo.products.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing a product option in the social commerce platform.
 * This entity does not use JPA relationships to maintain manual control over data access.
 */
@Data
@Entity
@Getter
@Setter
@Table(name = "options_v2")
public class Option {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_option")
    private Long idOption;
    
    /**
     * Option label - required field (e.g., "Color", "Size", "Material")
     */
    @Column(name = "label", nullable = false, columnDefinition = "TEXT")
    private String label;
    
    /**
     * Foreign key reference to products_v2 table
     * Note: No JPA relationship annotation to maintain manual control
     */
    @Column(name = "id_product", nullable = false)
    private Long idProduct;
    
    /**
     * Default constructor
     */
    public Option() {}
    
    /**
     * Constructor with required fields
     */
    public Option(String label, Long idProduct) {
        this.label = label;
        this.idProduct = idProduct;
    }
    
    /**
     * Check if this option has a valid label
     * @return true if label is not null and not empty, false otherwise
     */
    public boolean hasValidLabel() {
        return label != null && !label.trim().isEmpty();
    }
    
    /**
     * Get normalized label (trimmed and lowercase)
     * @return normalized label for comparison purposes
     */
    public String getNormalizedLabel() {
        return label != null ? label.trim().toLowerCase() : "";
    }
    
    /**
     * Check if this option label matches another label (case-insensitive)
     * @param otherLabel the label to compare with
     * @return true if labels match (case-insensitive), false otherwise
     */
    public boolean labelMatches(String otherLabel) {
        if (label == null && otherLabel == null) return true;
        if (label == null || otherLabel == null) return false;
        return label.trim().equalsIgnoreCase(otherLabel.trim());
    }
}