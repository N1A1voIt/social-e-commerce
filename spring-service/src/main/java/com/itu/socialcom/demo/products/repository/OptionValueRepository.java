package com.itu.socialcom.demo.products.repository;

import com.itu.socialcom.demo.products.model.OptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for OptionValue entity operations.
 * Provides CRUD operations and option-specific value queries.
 * 
 * Requirements addressed:
 * - 3.2: Option value creation and management
 * - 3.7: Cascade deletion of option values when options are deleted
 */
@Repository
public interface OptionValueRepository extends JpaRepository<OptionValue, Long> {
    
    /**
     * Find all option values belonging to a specific option.
     * 
     * @param idOption the ID of the option
     * @return list of option values for the specified option
     * 
     * Requirement 3.2: WHEN a seller creates option values THEN the system SHALL store multiple values for each option
     * Requirement 3.5: WHEN a seller views product options THEN the system SHALL display all options and their values for a specific product
     */
    List<OptionValue> findByIdOption(Long idOption);
    
    /**
     * Delete all option values belonging to a specific option.
     * This method is used for cascade deletion when an option is removed.
     * 
     * @param idOption the ID of the option whose values should be deleted
     * 
     * Requirement 3.7: WHEN an option is deleted THEN the system SHALL cascade delete all associated option values
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM OptionValue ov WHERE ov.idOption = :idOption")
    void deleteByIdOption(@Param("idOption") Long idOption);
    
    /**
     * Find option values for multiple options in a single query.
     * This method supports batch operations for efficient data retrieval.
     * 
     * @param optionIds list of option IDs
     * @return list of option values for all specified options
     * 
     * Requirement: Support for batch option value queries for performance optimization
     */
    List<OptionValue> findByIdOptionIn(List<Long> optionIds);
    
    /**
     * Find a specific option value by ID and option ID.
     * This method ensures that the option value belongs to the specified option,
     * providing an additional layer of validation for option value operations.
     * 
     * @param idOv the ID of the option value
     * @param idOption the ID of the option
     * @return Optional containing the option value if found and belongs to the option, empty otherwise
     * 
     * Requirement 3.2: Support for option value-specific operations with option validation
     */
    Optional<OptionValue> findByIdOvAndIdOption(Long idOv, Long idOption);
    
    /**
     * Check if an option value exists for a specific option.
     * Useful for validation without fetching the entire entity.
     * 
     * @param idOv the ID of the option value
     * @param idOption the ID of the option
     * @return true if the option value exists and belongs to the option, false otherwise
     * 
     * Requirement 3.2: Option value validation and existence checking
     */
    boolean existsByIdOvAndIdOption(Long idOv, Long idOption);
    
    /**
     * Count total option values for a specific option.
     * Useful for option statistics and validation.
     * 
     * @param idOption the ID of the option
     * @return total number of option values for the specified option
     * 
     * Requirement 3.2: Support for option value management and statistics
     */
    long countByIdOption(Long idOption);
    
    /**
     * Find option values by option ID ordered by value.
     * Provides consistent alphabetical ordering for option value listings.
     * 
     * @param idOption the ID of the option
     * @return list of option values ordered by value (alphabetical)
     * 
     * Requirement 3.5: Organized option value display for better user experience
     */
    @Query("SELECT ov FROM OptionValue ov WHERE ov.idOption = :idOption ORDER BY ov.value ASC")
    List<OptionValue> findByIdOptionOrderByValueAsc(@Param("idOption") Long idOption);
    
    /**
     * Find option value by option ID and value (case-insensitive).
     * Useful for preventing duplicate option values within the same option.
     * 
     * @param idOption the ID of the option
     * @param value the value to search for (case-insensitive)
     * @return Optional containing the option value if found, empty otherwise
     * 
     * Requirement 3.2: Prevent duplicate option values for the same option
     */
    @Query("SELECT ov FROM OptionValue ov WHERE ov.idOption = :idOption AND LOWER(ov.value) = LOWER(:value)")
    Optional<OptionValue> findByIdOptionAndValueIgnoreCase(@Param("idOption") Long idOption, @Param("value") String value);
    
    /**
     * Find all option values for options belonging to a specific product.
     * This method joins with the Option table to get all option values for a product.
     * 
     * @param idProduct the ID of the product
     * @return list of all option values for options belonging to the specified product
     * 
     * Requirement 3.6: Support for product-level cascade operations
     */
    @Query("SELECT ov FROM OptionValue ov JOIN Option o ON ov.idOption = o.idOption WHERE o.idProduct = :idProduct")
    List<OptionValue> findByProductId(@Param("idProduct") Long idProduct);
    
    /**
     * Delete all option values for options belonging to a specific product.
     * This method is used for cascade deletion when a product is removed.
     * 
     * @param idProduct the ID of the product whose option values should be deleted
     * 
     * Requirement 3.6: WHEN a product is deleted THEN the system SHALL cascade delete all associated options and option values
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM OptionValue ov WHERE ov.idOption IN (SELECT o.idOption FROM Option o WHERE o.idProduct = :idProduct)")
    void deleteByProductId(@Param("idProduct") Long idProduct);
}