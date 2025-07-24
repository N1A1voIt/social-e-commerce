package com.itu.socialcom.demo.products.repository;

import com.itu.socialcom.demo.products.model.VariantOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for VariantOptionValue entity operations.
 * Provides CRUD operations for the junction table between variants and option values.
 * 
 * Requirements addressed:
 * - 2.1: Variant creation with option value associations
 * - 2.7: Cascade deletion of variant option values when variants are deleted
 * - 3.7: Cascade deletion of variant option values when option values are deleted
 */
@Repository
public interface VariantOptionValueRepository extends JpaRepository<VariantOptionValue, Long> {
    
    /**
     * Find all option values associated with a specific variant.
     * 
     * @param idVariant the ID of the variant
     * @return list of variant option values for the specified variant
     * 
     * Requirement 2.1: WHEN a seller creates a variant THEN the system SHALL associate it with specific option values
     * Requirement 2.6: WHEN a seller views product variants THEN the system SHALL display all variants with their option values
     */
    List<VariantOptionValue> findByIdVariant(Long idVariant);
    
    /**
     * Find all variants associated with a specific option value.
     * 
     * @param idOv the ID of the option value
     * @return list of variant option values for the specified option value
     * 
     * Requirement: Support for finding which variants use a specific option value
     */
    List<VariantOptionValue> findByIdOv(String idOv);
    
    /**
     * Delete all variant option values for a specific variant.
     * This method is used for cascade deletion when a variant is removed.
     * 
     * @param idVariant the ID of the variant whose option value associations should be deleted
     * 
     * Requirement 2.7: WHEN a variant is deleted THEN the system SHALL cascade delete all associated variant option values
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VariantOptionValue vov WHERE vov.idVariant = :idVariant")
    void deleteByIdVariant(@Param("idVariant") Long idVariant);
    
    /**
     * Delete all variant option values for a specific option value.
     * This method is used for cascade deletion when an option value is removed.
     * 
     * @param idOv the ID of the option value whose variant associations should be deleted
     * 
     * Requirement 3.7: WHEN an option value is deleted THEN the system SHALL cascade delete all associated variant option values
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VariantOptionValue vov WHERE vov.idOv = :idOv")
    void deleteByIdOv(@Param("idOv") String idOv);
    
    /**
     * Find a specific variant option value by variant ID and option value ID.
     * This method checks if a specific association exists.
     * 
     * @param idVariant the ID of the variant
     * @param idOv the ID of the option value
     * @return Optional containing the variant option value if found, empty otherwise
     * 
     * Requirement: Support for checking specific variant-option value associations
     */
    Optional<VariantOptionValue> findByIdVariantAndIdOv(Long idVariant, String idOv);
    
    /**
     * Check if a variant option value association exists.
     * Useful for validation without fetching the entire entity.
     * 
     * @param idVariant the ID of the variant
     * @param idOv the ID of the option value
     * @return true if the association exists, false otherwise
     * 
     * Requirement: Validation for variant-option value associations
     */
    boolean existsByIdVariantAndIdOv(Long idVariant, String idOv);
    
    /**
     * Count total option values associated with a specific variant.
     * Useful for variant statistics and validation.
     * 
     * @param idVariant the ID of the variant
     * @return total number of option values associated with the variant
     * 
     * Requirement: Support for variant option value statistics
     */
    long countByIdVariant(Long idVariant);
    
    /**
     * Count total variants associated with a specific option value.
     * Useful for option value statistics and validation.
     * 
     * @param idOv the ID of the option value
     * @return total number of variants associated with the option value
     * 
     * Requirement: Support for option value usage statistics
     */
    long countByIdOv(String idOv);
    
    /**
     * Find variant option values for multiple variants in a single query.
     * This method supports batch operations for efficient data retrieval.
     * 
     * @param variantIds list of variant IDs
     * @return list of variant option values for all specified variants
     * 
     * Requirement: Support for batch variant option value queries for performance optimization
     */
    List<VariantOptionValue> findByIdVariantIn(List<Long> variantIds);
    
    /**
     * Find variant option values for multiple option values in a single query.
     * This method supports batch operations for efficient data retrieval.
     * 
     * @param optionValueIds list of option value IDs
     * @return list of variant option values for all specified option values
     * 
     * Requirement: Support for batch option value queries for performance optimization
     */
    List<VariantOptionValue> findByIdOvIn(List<String> optionValueIds);
    
    /**
     * Delete all variant option values for variants belonging to a specific product.
     * This method is used for cascade deletion when a product is removed.
     * 
     * @param idProduct the ID of the product whose variant option values should be deleted
     * 
     * Requirement 2.7: WHEN a product is deleted THEN the system SHALL cascade delete all associated variants and their option values
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VariantOptionValue vov WHERE vov.idVariant IN (SELECT v.idVariant FROM Variant v WHERE v.idProduct = :idProduct)")
    void deleteByProductId(@Param("idProduct") Long idProduct);
    
    /**
     * Delete all variant option values for option values belonging to a specific option.
     * This method is used for cascade deletion when an option is removed.
     * 
     * @param idOption the ID of the option whose variant option values should be deleted
     * 
     * Requirement 3.6: WHEN an option is deleted THEN the system SHALL cascade delete all associated option values and their variant associations
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VariantOptionValue vov WHERE vov.idOv IN (SELECT ov.idOv FROM OptionValue ov WHERE ov.idOption = :idOption)")
    void deleteByOptionId(@Param("idOption") Long idOption);
}