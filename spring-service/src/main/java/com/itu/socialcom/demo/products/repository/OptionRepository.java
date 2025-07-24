package com.itu.socialcom.demo.products.repository;

import com.itu.socialcom.demo.products.model.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Option entity operations.
 * Provides CRUD operations and product-specific option queries.
 * 
 * Requirements addressed:
 * - 3.1: Option creation and management
 * - 3.6: Cascade deletion of options when products are deleted
 */
@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {
    
    /**
     * Find all options belonging to a specific product.
     * 
     * @param idProduct the ID of the product
     * @return list of options for the specified product
     * 
     * Requirement 3.1: WHEN a seller creates a product option THEN the system SHALL store the option with label and product association
     * Requirement 3.5: WHEN a seller views product options THEN the system SHALL display all options and their values for a specific product
     */
    List<Option> findByIdProduct(Long idProduct);
    
    /**
     * Delete all options belonging to a specific product.
     * This method is used for cascade deletion when a product is removed.
     * 
     * @param idProduct the ID of the product whose options should be deleted
     * 
     * Requirement 3.6: WHEN a product is deleted THEN the system SHALL cascade delete all associated options and option values
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Option o WHERE o.idProduct = :idProduct")
    void deleteByIdProduct(@Param("idProduct") Long idProduct);
    
    /**
     * Find a specific option by option ID and product ID.
     * This method ensures that the option belongs to the specified product,
     * providing an additional layer of validation for option operations.
     * 
     * @param idOption the ID of the option
     * @param idProduct the ID of the product
     * @return Optional containing the option if found and belongs to the product, empty otherwise
     * 
     * Requirement 3.1: Support for option-specific operations with product validation
     */
    Optional<Option> findByIdOptionAndIdProduct(Long idOption, Long idProduct);
    
    /**
     * Check if an option exists for a specific product.
     * Useful for validation without fetching the entire entity.
     * 
     * @param idOption the ID of the option
     * @param idProduct the ID of the product
     * @return true if the option exists and belongs to the product, false otherwise
     * 
     * Requirement 3.1: Option validation and existence checking
     */
    boolean existsByIdOptionAndIdProduct(Long idOption, Long idProduct);
    
    /**
     * Count total options for a specific product.
     * Useful for product statistics and validation.
     * 
     * @param idProduct the ID of the product
     * @return total number of options for the specified product
     * 
     * Requirement 3.1: Support for option management and statistics
     */
    long countByIdProduct(Long idProduct);
    
    /**
     * Find options by product ID ordered by label.
     * Provides consistent alphabetical ordering for option listings.
     * 
     * @param idProduct the ID of the product
     * @return list of options ordered by label (alphabetical)
     * 
     * Requirement 3.5: Organized option display for better user experience
     */
    @Query("SELECT o FROM Option o WHERE o.idProduct = :idProduct ORDER BY o.label ASC")
    List<Option> findByIdProductOrderByLabelAsc(@Param("idProduct") Long idProduct);
    
    /**
     * Find options by product ID and label (case-insensitive).
     * Useful for preventing duplicate option labels within the same product.
     * 
     * @param idProduct the ID of the product
     * @param label the label to search for (case-insensitive)
     * @return Optional containing the option if found, empty otherwise
     * 
     * Requirement 3.1: Prevent duplicate option labels for the same product
     */
    @Query("SELECT o FROM Option o WHERE o.idProduct = :idProduct AND LOWER(o.label) = LOWER(:label)")
    Optional<Option> findByIdProductAndLabelIgnoreCase(@Param("idProduct") Long idProduct, @Param("label") String label);
}