package com.itu.socialcom.demo.delivery.repository;

import com.itu.socialcom.demo.delivery.entity.AmountDistance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for AmountDistance entity
 */
@Repository
public interface AmountDistanceRepository extends JpaRepository<AmountDistance, Long> {

    /**
     * Find amount distance configurations for a specific user
     */
    List<AmountDistance> findByUserId(Long userId);

    /**
     * Find amount distance configurations for a specific managed page
     */
    List<AmountDistance> findByManagedPageId(Long managedPageId);

    /**
     * Find amount distance configuration for a specific user
     */
    Optional<AmountDistance> findTopByUserIdOrderByIdDesc(Long userId);

    /**
     * Find amount distance configuration for a specific managed page
     */
    Optional<AmountDistance> findTopByManagedPageIdOrderByIdDesc(Long managedPageId);

    /**
     * Find default amount distance configuration (when no user or managed page specific configuration exists)
     */
    @Query("SELECT ad FROM AmountDistance ad WHERE ad.userId IS NULL AND ad.managedPageId IS NULL ORDER BY ad.id DESC")
    Optional<AmountDistance> findDefaultConfiguration();
}
