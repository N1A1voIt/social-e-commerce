package com.itu.socialcom.demo.orders.repository;

import com.itu.socialcom.demo.orders.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    @Query("SELECT p FROM Payment p WHERE p.salesId IN " +
           "(SELECT s.idSale FROM Sales s WHERE s.idOrderM = :orderId)")
    List<Payment> findPaymentsByOrderId(@Param("orderId") Long orderId);
}
