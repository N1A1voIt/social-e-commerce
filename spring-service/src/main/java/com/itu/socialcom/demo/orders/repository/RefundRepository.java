package com.itu.socialcom.demo.orders.repository;

import com.itu.socialcom.demo.orders.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findByOrderId(Long orderId);
}
