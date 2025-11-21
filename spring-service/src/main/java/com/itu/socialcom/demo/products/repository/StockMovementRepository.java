package com.itu.socialcom.demo.products.repository;

import com.itu.socialcom.demo.products.model.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long>, StockMovementRepositoryCustom {
    
    Page<StockMovement> findByIdSellerOrderByActionAtDesc(Long idSeller, Pageable pageable);
    
    Page<StockMovement> findByIdSellerAndIdProductOrderByActionAtDesc(Long idSeller, Long idProduct, Pageable pageable);
    
    Page<StockMovement> findByIdSellerAndIdVariantOrderByActionAtDesc(Long idSeller, Long idVariant, Pageable pageable);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.idSeller = :idSeller " +
           "AND sm.actionAt BETWEEN :startDate AND :endDate " +
           "ORDER BY sm.actionAt DESC")
    Page<StockMovement> findByIdSellerAndActionAtBetween(
        @Param("idSeller") Long idSeller, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate, 
        Pageable pageable
    );
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.idSeller = :idSeller " +
           "AND sm.movementType = :movementType " +
           "ORDER BY sm.actionAt DESC")
    Page<StockMovement> findByIdSellerAndMovementType(
        @Param("idSeller") Long idSeller, 
        @Param("movementType") String movementType, 
        Pageable pageable
    );
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.idSeller = :idSeller " +
           "AND (UPPER(sm.productName) LIKE UPPER(CONCAT('%', :searchTerm, '%')) " +
           "OR UPPER(sm.variantName) LIKE UPPER(CONCAT('%', :searchTerm, '%')) " +
           "OR UPPER(sm.skuPrefix) LIKE UPPER(CONCAT('%', :searchTerm, '%')) " +
           "OR UPPER(sm.variantSku) LIKE UPPER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY sm.actionAt DESC")
    Page<StockMovement> findByIdSellerAndSearchTerm(
        @Param("idSeller") Long idSeller, 
        @Param("searchTerm") String searchTerm, 
        Pageable pageable
    );
}