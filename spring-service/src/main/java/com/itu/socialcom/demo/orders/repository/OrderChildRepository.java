package com.itu.socialcom.demo.orders.repository;

import com.itu.socialcom.demo.orders.OrderChild;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderChildRepository extends JpaRepository<OrderChild, Long> {
    List<OrderChild> findByIdOrderM(Long idOrderM);
}
