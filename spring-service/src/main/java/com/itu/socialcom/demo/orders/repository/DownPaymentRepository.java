package com.itu.socialcom.demo.orders.repository;

import com.itu.socialcom.demo.orders.DownPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DownPaymentRepository extends JpaRepository<DownPayment,Long> {
    List<DownPayment> findByIdSeller(Long idSeller);
}
