package com.itu.socialcom.demo.delivery.space.missions;

import com.itu.socialcom.demo.delivery.deliverydriver.DeliveryDriver;
import com.itu.socialcom.demo.delivery.deliverydriver.DeliveryDriverRepository;
import com.itu.socialcom.demo.delivery.entity.Delivery;
import com.itu.socialcom.demo.delivery.service.DeliveryService;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.delivery.DeliveryLog;
import com.itu.socialcom.demo.orders.delivery.DeliveryLogRepository;
import com.itu.socialcom.demo.orders.repository.OrderParentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MissionService {
    @Autowired
    DeliveryLogRepository deliveryLogRepository;
    @Autowired
    DeliveryService deliveryService;
    @Autowired
    OrderParentRepository orderParentRepository;
    @Autowired
    DeliveryDriverRepository deliveryDriverRepository;

    public DeliveryLog applyTo(Long idMission, DeliveryDriver deliveryDriver) {
        Delivery delivery = deliveryService.getDeliveryById(idMission).orElseThrow(()->new IllegalArgumentException("Mission not found"));
        OrderParent orderParent = orderParentRepository.findByIdOrderM(delivery.getOrderMotherId()).get(0);
        if(delivery.getDeliveryDriverId()!=null) {
            throw new IllegalArgumentException("Mission already assigned");
        }
        deliveryLogRepository.findByIdDdAndIdDelivery(deliveryDriver.getId(), idMission).ifPresent(d -> {
            throw new IllegalArgumentException("You have already applied to this mission");
        });
        DeliveryLog deliveryLog = new DeliveryLog();
        deliveryLog.setIdDd(deliveryDriver.getId());
        deliveryLog.setIdDelivery(idMission);
        deliveryLog.setMessage("Apply");
        deliveryLog.setContact(deliveryDriver.getPhoneNumber());
        deliveryLog.setIdMp(orderParent.getIdManagedPages().longValue());
        deliveryLog.setIdSeller(orderParent.getIdSeller());
        deliveryLogRepository.save(deliveryLog);
        return deliveryLog;
    }
}
