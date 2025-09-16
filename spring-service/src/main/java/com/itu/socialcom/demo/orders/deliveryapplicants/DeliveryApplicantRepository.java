package com.itu.socialcom.demo.orders.deliveryapplicants;

import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryApplicantRepository extends JpaRepository<DeliveryApplicant, Integer> {
    List<DeliveryApplicant> findBydStatusAndIdDelivery(String dStatus,Long idDelivery);
//    List<DeliveryApplicant> findByDStatus(String dStatus);
}
