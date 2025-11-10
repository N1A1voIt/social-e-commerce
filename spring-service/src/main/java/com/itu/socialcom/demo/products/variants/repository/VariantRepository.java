package com.itu.socialcom.demo.products.variants.repository;

import com.itu.socialcom.demo.products.variants.model.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Variant entity operations.
 * Provides CRUD operations and product-specific variant queries.
 * Requirements addressed:
 * - 2.1: Variant creation and management
 * - 2.6: Variant listing for products
 * - 2.7: Cascade deletion of variants when products are deleted
 */
@Repository
public interface VariantRepository extends JpaRepository<Variant, Long> {
    
    /**
     * Find all variants belonging to a specific product.
     * 
     * @param idProduct the ID of the product
     * @return list of variants for the specified product
     * 
     * Requirement 2.6: WHEN a seller views product variants THEN the system SHALL display all variants for a specific product
     */
    List<Variant> findByIdProduct(Long idProduct);
    
    /**
     * Delete all variants belonging to a specific product.
     * This method is used for cascade deletion when a product is removed.
     * 
     * @param idProduct the ID of the product whose variants should be deleted
     * 
     * Requirement 2.7: WHEN a product is deleted THEN the system SHALL cascade delete all associated variants
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Variant v WHERE v.idProduct = :idProduct")
    void deleteByIdProduct(@Param("idProduct") Long idProduct);
    
    /**
     * Find a specific variant by variant ID and product ID.
     * This method ensures that the variant belongs to the specified product,
     * providing an additional layer of validation for variant operations.
     * 
     * @param idVariant the ID of the variant
     * @param idProduct the ID of the product
     * @return Optional containing the variant if found and belongs to the product, empty otherwise
     * 
     * Requirement 2.1: Support for variant-specific operations with product validation
     */
    Optional<Variant> findByIdVariantAndIdProduct(Long idVariant, Long idProduct);
    
    /**
     * Check if a variant exists for a specific product.
     * Useful for validation without fetching the entire entity.
     * 
     * @param idVariant the ID of the variant
     * @param idProduct the ID of the product
     * @return true if the variant exists and belongs to the product, false otherwise
     * 
     * Requirement 2.1: Variant validation and existence checking
     */
    boolean existsByIdVariantAndIdProduct(Long idVariant, Long idProduct);
    
    /**
     * Count total variants for a specific product.
     * Useful for product statistics and validation.
     * 
     * @param idProduct the ID of the product
     * @return total number of variants for the specified product
     * 
     * Requirement 2.6: Support for variant management and statistics
     */
    long countByIdProduct(Long idProduct);
    
    /**
     * Find variant by seller ID and SKU.
     * Used for CSV import to match variants by SKU.
     *
     * @param sellerId the ID of the seller
     * @param sku the SKU to search for
     * @return Optional containing the variant if found
     */
    Optional<Variant> findByIdSellerAndSku(Long sellerId, String sku);

    /**
     * Find variants by product ID ordered by creation date.
     * Provides consistent ordering for variant listings.
     * 
     * @param idProduct the ID of the product
     * @return list of variants ordered by creation date (newest first)
     * 
     * Requirement 2.6: Organized variant display for better user experience
     */
    @Query("SELECT v FROM Variant v WHERE v.idProduct = :idProduct ORDER BY v.createdAt DESC")
    List<Variant> findByIdProductOrderByCreatedAtDesc(@Param("idProduct") Long idProduct);
    
    /**
     * Find variants by product ID ordered by price.
     * Useful for price-based variant sorting.
     * 
     * @param idProduct the ID of the product
     * @return list of variants ordered by price (ascending)
     * 
     * Requirement 2.6: Support for price-based variant organization
     */
    @Query("SELECT v FROM Variant v WHERE v.idProduct = :idProduct ORDER BY v.price ASC")
    List<Variant> findByIdProductOrderByPriceAsc(@Param("idProduct") Long idProduct);

    List<Variant> findVariantsByIdProduct(Long idProduct);

    List<Variant> findByIdProductIn(Collection<Long> idProducts);

    List<Variant> findByIdSellerAndSkuIn(Long idSeller, Collection<String> skus);

    List<Variant> findByIdSeller(Long idSeller);
}