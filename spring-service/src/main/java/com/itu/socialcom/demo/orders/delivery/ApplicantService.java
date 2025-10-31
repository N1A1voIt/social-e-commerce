package com.itu.socialcom.demo.orders.delivery;

import com.itu.socialcom.demo.delivery.deliverydriver.DeliveryDriver;
import com.itu.socialcom.demo.delivery.deliverydriver.DeliveryDriverRepository;
import com.itu.socialcom.demo.delivery.entity.Delivery;
import com.itu.socialcom.demo.delivery.repository.DeliveryRepository;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.repository.OrderParentRepository;
import com.itu.socialcom.demo.whatsapp.service.WhatsAppServiceImpl;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicantService {
    @Autowired
    DeliveryRepository deliveryRepository;
    @Autowired
    OrderParentRepository orderParentRepository;
    @Autowired
    DeliveryDriverRepository deliveryDriverRepository;
    @Autowired
    WhatsAppServiceImpl whatsAppService;
    @Transactional
    public Delivery assignDelivery(Long idDelivery, Long idApplicant) throws Exception {
        Delivery delivery = deliveryRepository.findById(idDelivery)
                .orElseThrow(() -> new Exception("Delivery not found with id: " + idDelivery));
        DeliveryDriver deliveryDriver = deliveryDriverRepository.findById(idApplicant.intValue())
                .orElseThrow(() -> new Exception("Delivery driver not found with id: " + idApplicant));
        OrderParent parent = orderParentRepository.findByIdOrderM(delivery.getOrderMotherId()).get(0);
        parent.setDStatus(31);
        orderParentRepository.save(parent);
        delivery.setDeliveryDriverId(idApplicant);
        delivery.setStatus("CLOSED");
        deliveryRepository.save(delivery);
        whatsAppService.sendMessage(deliveryDriver.getPhoneNumber(),
                "You have been assigned to delivery ID: " + idDelivery + " for order ID: " + delivery.getOrderMotherId());
        return delivery;
    }


}
