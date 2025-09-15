package com.itu.socialcom.demo.delivery.deliverydriver;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.relational.core.sql.In;

import java.util.List;

public interface DeliveryDriverRepository extends JpaRepository<DeliveryDriver, Integer> {
    List<DeliveryDriver> findByMinRangeGreaterThanAndMaxRangeLessThan(Double minRangeIsGreaterThan, Double maxRangeIsLessThan);
    @Query(value = "SELECT * FROM delivery_driver_v2 WHERE min_range <= :price AND max_range >= :price ", nativeQuery = true)
    List<DeliveryDriver> findByPriceInRange(@Param("price") Double price);

    List<DeliveryDriver> findByPhoneNumber(String phoneNumber);
}
