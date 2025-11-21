package com.itu.socialcom.demo.products.repository;

import com.itu.socialcom.demo.products.model.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface StockMovementRepositoryCustom {
    
    Page<StockMovement> findByMultipleCriteria(
        Long idSeller,
        String search,
        String movementType,
        Long productId,
        Long variantId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );
    
    List<StockMovement> findAllByMultipleCriteria(
        Long idSeller,
        String search,
        String movementType,
        Long productId,
        Long variantId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );
}