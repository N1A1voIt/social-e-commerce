package com.itu.socialcom.demo.products.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing the junction table between variants and option values.
 * This entity maps which option values are associated with each variant.
 * This entity does not use JPA relationships to maintain manual control over data access.
 */
@Data
@Entity
@Getter
@Setter
@Table(name = "variant_option_values_v2")
public class VariantOptionValue {
    
    /**
     * Primary key - auto-generated serial ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * Foreign key reference to options_values_v2 table
     * Note: No JPA relationship annotation to maintain manual control
     */
    @Column(name = "id_ov", length = 50, nullable = false)
    private Long idOv;
    
    /**
     * Foreign key reference to variants_v2 table
     * Note: No JPA relationship annotation to maintain manual control
     */
    @Column(name = "id_variant", nullable = false)
    private Long idVariant;
    
    /**
     * Default constructor
     */
    public VariantOptionValue() {}
    
    /**
     * Constructor with required fields
     */
    public VariantOptionValue(Long idOv, Long idVariant) {
        this.idOv = idOv;
        this.idVariant = idVariant;
    }
    
    /**
     * Check if this variant option value has valid references
     * @return true if both idOv and idVariant are not null, false otherwise
     */

    /**
     * Create a composite key string for uniqueness checking
     * @return composite key in format "variantId_optionValueId"
     */
    public String getCompositeKey() {
        if (idVariant == null || idOv == null) {
            throw new IllegalStateException("Cannot create composite key without variant ID and option value ID");
        }
        return idVariant + "_" + idOv;
    }
    
    /**
     * Check if this variant option value matches another by composite key
     * @param other the other VariantOptionValue to compare with
     * @return true if they have the same variant and option value IDs, false otherwise
     */
    public boolean matches(VariantOptionValue other) {
        if (other == null) return false;
        return this.idVariant != null && this.idVariant.equals(other.idVariant) &&
               this.idOv != null && this.idOv.equals(other.idOv);
    }
}