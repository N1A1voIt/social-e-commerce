package com.itu.socialcom.demo.products.repository;

import com.itu.socialcom.demo.products.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Repository interface for Product entity operations.
 * Provides basic CRUD operations and seller-specific queries.
 * 
 * Requirements addressed:
 * - 1.6: Seller-specific product retrieval
 * - 4.1: Product listing by seller
 * - 4.2: Name-based search functionality
 * - 4.3: Price range filtering
 * - 6.1: Seller ownership validation
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Find all products belonging to a specific seller with pagination support.
     * 
     * @param sellerId the ID of the seller
     * @param pageable pagination information
     * @return paginated list of products for the seller
     * 
     * Requirement 1.6: WHEN a seller views their products THEN the system SHALL display all products associated with their seller ID
     * Requirement 4.1: WHEN a seller requests their products THEN the system SHALL return all products associated with their seller ID
     */
    Page<Product> findByIdSeller(Integer sellerId, Pageable pageable);
    
    /**
     * Find products by seller ID with case-insensitive name search.
     * 
     * @param sellerId the ID of the seller
     * @param name the name to search for (case-insensitive, partial match)
     * @param pageable pagination information
     * @return paginated list of matching products
     * 
     * Requirement 4.2: WHEN a seller searches products by name THEN the system SHALL return matching products using case-insensitive search
     */
    Page<Product> findByIdSellerAndNameContainingIgnoreCase(Integer sellerId, String name, Pageable pageable);
    
    /**
     * Find products by seller ID within a specific price range.
     * 
     * @param sellerId the ID of the seller
     * @param minPrice minimum price (inclusive)
     * @param maxPrice maximum price (inclusive)
     * @param pageable pagination information
     * @return paginated list of products within the price range
     * 
     * Requirement 4.3: WHEN a seller filters products by price range THEN the system SHALL return products within the specified range
     */
    Page<Product> findByIdSellerAndPriceBetween(Integer sellerId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    /**
     * Find a specific product by ID and seller ID for ownership validation.
     * This method ensures that only the product owner can access/modify the product.
     * 
     * @param productId the ID of the product
     * @param sellerId the ID of the seller
     * @return Optional containing the product if found and owned by the seller, empty otherwise
     * 
     * Requirement 6.1: WHEN any product operation is performed THEN the system SHALL validate seller ownership
     */
    Optional<Product> findByIdProductAndIdSeller(Long productId, Integer sellerId);
    
    /**
     * Check if a product exists and is owned by the specified seller.
     * Useful for quick ownership validation without fetching the entire entity.
     * 
     * @param productId the ID of the product
     * @param sellerId the ID of the seller
     * @return true if the product exists and is owned by the seller, false otherwisepublic
     * 
     * Requirement 6.1: WHEN any product operation is performed THEN the system SHALL validate seller ownership
     */
    boolean existsByIdProductAndIdSeller(Long productId, Integer sellerId);
    
    /**
     * Count total products for a specific seller.
     * Useful for dashboard statistics and pagination calculations.
     * 
     * @param sellerId the ID of the seller
     * @return total number of products owned by the seller
     * 
     * Requirement 1.6: Support for seller-specific product management
     */
    long countByIdSeller(Integer sellerId);
    
    /**
     * Find products by seller ID with combined name search and price range filtering.
     * This method supports complex search scenarios where both name and price filters are applied.
     * 
     * @param sellerId the ID of the seller
     * @param name the name to search for (case-insensitive, partial match)
     * @param minPrice minimum price (inclusive)
     * @param maxPrice maximum price (inclusive)
     * @param pageable pagination information
     * @return paginated list of products matching all criteria
     * 
     * Requirements 4.2, 4.3: Combined search and filtering functionality
     */
    @Query("SELECT p FROM Product p WHERE p.idSeller = :sellerId " +
           "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findBySellerWithNameAndPriceRange(
        @Param("sellerId") Integer sellerId,
        @Param("name") String name,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable
    );
}