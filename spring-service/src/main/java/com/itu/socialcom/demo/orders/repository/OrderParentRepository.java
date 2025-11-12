package com.itu.socialcom.demo.orders.repository;

import com.itu.socialcom.demo.orders.OrderParent;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.util.List;

public interface OrderParentRepository extends JpaRepository<OrderParent, Long> {
    Page<OrderParent> findAllByIdSeller(Integer idSeller, org.springframework.data.domain.Pageable pageable);
    int countByIdSeller(Integer idSeller);

    List<OrderParent> findByIdOrderM(Long idOrderM);
    @Query("SELECT o FROM OrderParent o WHERE o.idSeller = :idSeller AND o.dStatus>=:dStatus AND o.createdAt >= :startDate AND o.createdAt <= :endDate ORDER BY o.createdAt DESC")
    List<OrderParent> findByDStatusGreaterThanEqualAndIdSeller(@Param("dStatus") Integer dStatus,
                                                               @Param("idSeller") Integer idSeller,
                                                               @Param("startDate") java.time.LocalDateTime startDate,
                                                               @Param("endDate") java.time.LocalDateTime endDate);

    @Query(value = "SELECT * FROM order_mother o WHERE o.id_seller = :idSeller " +
           "AND (:status IS NULL OR o.d_status = :status) " +
           "AND (:startDate IS NULL OR o.created_at >= CAST(:startDate AS TIMESTAMP)) " +
           "AND (:endDate IS NULL OR o.created_at <= CAST(:endDate AS TIMESTAMP)) " +
           "ORDER BY o.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM order_mother o WHERE o.id_seller = :idSeller " +
                   "AND (:status IS NULL OR o.d_status = :status) " +
                   "AND (:startDate IS NULL OR o.created_at >= CAST(:startDate AS TIMESTAMP)) " +
                   "AND (:endDate IS NULL OR o.created_at <= CAST(:endDate AS TIMESTAMP))",
           nativeQuery = true)
    Page<OrderParent> findAllByIdSellerWithFilters(@Param("idSeller") Integer idSeller,
                                                    @Param("status") Integer status,
                                                    @Param("startDate") java.time.LocalDateTime startDate,
                                                    @Param("endDate") java.time.LocalDateTime endDate,
                                                    org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM order_mother o WHERE o.id_seller = :idSeller " +
           "AND (:status IS NULL OR o.d_status = :status) " +
           "AND (:startDate IS NULL OR o.created_at >= CAST(:startDate AS TIMESTAMP)) " +
           "AND (:endDate IS NULL OR o.created_at <= CAST(:endDate AS TIMESTAMP))",
           nativeQuery = true)
    int countByIdSellerWithFilters(@Param("idSeller") Integer idSeller,
                                    @Param("status") Integer status,
                                    @Param("startDate") java.time.LocalDateTime startDate,
                                    @Param("endDate") java.time.LocalDateTime endDate);

}
