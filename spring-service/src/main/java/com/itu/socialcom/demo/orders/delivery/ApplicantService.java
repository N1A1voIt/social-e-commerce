package com.itu.socialcom.demo.orders.delivery;

import com.itu.socialcom.demo.delivery.entity.Delivery;
import com.itu.socialcom.demo.delivery.repository.DeliveryRepository;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.repository.OrderParentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicantService {
    @Autowired
    DeliveryRepository deliveryRepository;
    @Autowired
    OrderParentRepository orderParentRepository;
    public Delivery assignDelivery(Long idDelivery, Long idApplicant) throws Exception {
        Delivery delivery = deliveryRepository.findById(idDelivery)
                .orElseThrow(() -> new Exception("Delivery not found with id: " + idDelivery));
        OrderParent parent = orderParentRepository.findByIdOrderM(delivery.getOrderMotherId()).get(0);
        parent.setDStatus(31);
        orderParentRepository.save(parent);
        delivery.setDeliveryDriverId(idApplicant);
        delivery.setStatus("CLOSED");
        deliveryRepository.save(delivery);
        return delivery;
    }
}
