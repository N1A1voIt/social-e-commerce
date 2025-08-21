package com.itu.socialcom.demo.orders.repository;

import com.itu.socialcom.demo.orders.OrderParent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderParentRepository extends JpaRepository<OrderParent, Long> {
    List<OrderParent> findAllByIdSeller(Integer idSeller);
}
