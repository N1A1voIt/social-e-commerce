package com.itu.socialcom.demo.sales;

import com.itu.socialcom.demo.orders.OrderParent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesRepository extends JpaRepository<Sales, Integer> {

//    @Query("SELECT s FROM Sales s JOIN OrderParent o ON s.idOrderM = o.idOrderM WHERE o.idSeller = :idSeller ORDER BY s.effectuatedAt DESC")
//    Page<Sales> findAllByIdSeller(@Param("idSeller") Integer idSeller, Pageable pageable);

    @Query("SELECT COUNT(s) FROM Sales s JOIN OrderParent o ON s.idOrderM = o.idOrderM WHERE o.idSeller = :idSeller")
    int countByIdSeller(@Param("idSeller") Integer idSeller);

    Page<Sales> findByIdSeller(Integer idSeller, Pageable pageable);
}
