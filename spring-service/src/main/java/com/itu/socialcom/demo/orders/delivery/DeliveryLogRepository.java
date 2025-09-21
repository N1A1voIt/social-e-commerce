package com.itu.socialcom.demo.orders.delivery;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, Integer> {
    Optional<DeliveryLog> findByIdDdAndIdDelivery(Long idDd, Long idDelivery);
}
