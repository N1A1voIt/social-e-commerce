package com.itu.socialcom.demo.socialmedia.repository;

import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ManagedPageRepository extends JpaRepository<ManagedPage, Long> {
    
    /**
     * Find all managed pages for a specific seller
     */
    @Query("SELECT mp FROM ManagedPage mp WHERE mp.sellerId = :sellerId ORDER BY mp.createdAt DESC")
    List<ManagedPage> findBySellerIdCustom(@Param("sellerId") Long sellerId);
    
    /**
     * Find managed pages by seller and platform
     */
    @Query("SELECT mp FROM ManagedPage mp WHERE mp.sellerId = :sellerId AND mp.platformId = :platformId")
    List<ManagedPage> findBySellerAndPlatform(@Param("sellerId") Long sellerId, @Param("platformId") Long platformId);
    
    /**
     * Find a managed page by platform identifier and platform
     */
    @Query("SELECT mp FROM ManagedPage mp WHERE mp.platformIdentifier = :platformIdentifier AND mp.platformId = :platformId")
    Optional<ManagedPage> findByPlatformIdentifierAndPlatform(@Param("platformIdentifier") String platformIdentifier, @Param("platformId") Long platformId);
    
    /**
     * Find managed pages by status
     */
    @Query("SELECT mp FROM ManagedPage mp WHERE mp.sellerId = :sellerId AND mp.status = :status")
    List<ManagedPage> findBySellerAndStatus(@Param("sellerId") Long sellerId, @Param("status") String status);
    
    /**
     * Find active managed pages for a seller
     */
    @Query("SELECT mp FROM ManagedPage mp WHERE mp.sellerId = :sellerId AND mp.status = 'active'")
    List<ManagedPage> findActivePagesBySeller(@Param("sellerId") Long sellerId);
    
    /**
     * Find inactive managed pages for a seller
     */
    @Query("SELECT mp FROM ManagedPage mp WHERE mp.sellerId = :sellerId AND mp.status = 'inactive'")
    List<ManagedPage> findInactivePagesBySeller(@Param("sellerId") Long sellerId);
    
    /**
     * Update page status
     */
    @Modifying
    @Query("UPDATE ManagedPage mp SET mp.status = :status, mp.updatedAt = :updatedAt WHERE mp.id = :pageId")
    void updatePageStatus(@Param("pageId") Long pageId, @Param("status") String status, @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * Update page status for multiple pages
     */
    @Modifying
    @Query("UPDATE ManagedPage mp SET mp.status = :status, mp.updatedAt = :updatedAt WHERE mp.id IN :pageIds")
    void updatePageStatusBatch(@Param("pageIds") List<Long> pageIds, @Param("status") String status, @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * Check if a page exists for a seller with the given platform identifier
     */
    @Query("SELECT COUNT(mp) > 0 FROM ManagedPage mp WHERE mp.sellerId = :sellerId AND mp.platformIdentifier = :platformIdentifier AND mp.platformId = :platformId")
    boolean existsBySellerAndPlatformIdentifier(@Param("sellerId") Long sellerId, @Param("platformIdentifier") String platformIdentifier, @Param("platformId") Long platformId);
    
    /**
     * Count managed pages by seller and status
     */
    @Query("SELECT COUNT(mp) FROM ManagedPage mp WHERE mp.sellerId = :sellerId AND mp.status = :status")
    long countBySellerAndStatus(@Param("sellerId") Long sellerId, @Param("status") String status);
    
    /**
     * Find pages that need token refresh (pages with status active but potentially expired tokens)
     */
    @Query("SELECT mp FROM ManagedPage mp WHERE mp.status = 'active' AND mp.updatedAt < :threshold")
    List<ManagedPage> findPagesNeedingTokenRefresh(@Param("threshold") LocalDateTime threshold);
}