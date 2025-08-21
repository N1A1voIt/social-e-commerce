package com.itu.socialcom.demo.orders.repository;

import com.itu.socialcom.demo.orders.OrderParent;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.List;

public interface OrderParentRepository extends JpaRepository<OrderParent, Long> {
    Page<OrderParent> findAllByIdSeller(Integer idSeller, org.springframework.data.domain.Pageable pageable);
    int countByIdSeller(Integer idSeller);
}
