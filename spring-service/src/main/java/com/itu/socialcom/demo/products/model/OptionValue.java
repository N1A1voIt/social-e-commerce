package com.itu.socialcom.demo.products.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing a product option value in the social commerce platform.
 * This entity does not use JPA relationships to maintain manual control over data access.
 */
@Data
@Entity
@Getter
@Setter
@Table(name = "options_values_v2")
public class OptionValue {
    
    /**
     * Primary key - using VARCHAR(50) as per database schema
     */
    @Id
    @Column(name = "id_ov", length = 50)
    private String idOv;
    
    /**
     * Option value - required field (e.g., "Red", "Large", "Cotton")
     */
    @Column(name = "value_", nullable = false, columnDefinition = "TEXT")
    private String value;
    
    /**
     * Foreign key reference to options_v2 table
     * Note: No JPA relationship annotation to maintain manual control
     */
    @Column(name = "id_option", nullable = false)
    private Long idOption;
    
    /**
     * Default constructor
     */
    public OptionValue() {}
    
    /**
     * Constructor with required fields
     */
    public OptionValue(String idOv, String value, Long idOption) {
        this.idOv = idOv;
        this.value = value;
        this.idOption = idOption;
    }
    
    /**
     * Constructor without ID (for cases where ID will be generated)
     */
    public OptionValue(String value, Long idOption) {
        this.value = value;
        this.idOption = idOption;
    }
    
    /**
     * Check if this option value has a valid value
     * @return true if value is not null and not empty, false otherwise
     */
    public boolean hasValidValue() {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Get normalized value (trimmed and lowercase)
     * @return normalized value for comparison purposes
     */
    public String getNormalizedValue() {
        return value != null ? value.trim().toLowerCase() : "";
    }
    
    /**
     * Check if this option value matches another value (case-insensitive)
     * @param otherValue the value to compare with
     * @return true if values match (case-insensitive), false otherwise
     */
    public boolean valueMatches(String otherValue) {
        if (value == null && otherValue == null) return true;
        if (value == null || otherValue == null) return false;
        return value.trim().equalsIgnoreCase(otherValue.trim());
    }
    
    /**
     * Generate a unique ID for this option value based on option ID and value
     * This can be used when creating new option values
     * @return generated unique ID
     */
    public String generateId() {
        if (idOption == null || value == null) {
            throw new IllegalStateException("Cannot generate ID without option ID and value");
        }
        // Create a simple ID format: optionId_normalizedValue
        String normalizedValue = value.trim().toLowerCase().replaceAll("[^a-z0-9]", "_");
        return idOption + "_" + normalizedValue;
    }
    
    /**
     * Set the ID using the auto-generation logic
     */
    public void setGeneratedId() {
        this.idOv = generateId();
    }
}