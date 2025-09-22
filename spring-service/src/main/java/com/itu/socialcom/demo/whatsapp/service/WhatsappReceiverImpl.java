package com.itu.socialcom.demo.whatsapp.service;

import com.itu.socialcom.demo.delivery.deliverydriver.DeliveryDriver;
import com.itu.socialcom.demo.delivery.deliverydriver.DeliveryDriverRepository;
import com.itu.socialcom.demo.orders.delivery.DeliveryLog;
import com.itu.socialcom.demo.orders.delivery.DeliveryLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WhatsappReceiverImpl implements WhatsappReceiverService{
    @Autowired
    DeliveryDriverRepository deliveryDriverRepository;
    @Autowired
    DeliveryLogRepository deliveryLogRepository;
    @Override
    public DeliveryLog processIncomingMessage(String payload, String from, String message) {
        String[] parts = payload.split("_");
        DeliveryDriver deliveryDriver = deliveryDriverRepository.findByPhoneNumber(from).get(0);
        DeliveryLog deliveryLog = new DeliveryLog();
        deliveryLog.setIdDelivery((long) Integer.parseInt(parts[1]));
        deliveryLog.setContact(from);
        deliveryLog.setIdSeller(Integer.valueOf(parts[3]));
        deliveryLog.setMessage(message);
        deliveryLog.setIdMp(Integer.valueOf(parts[2]).longValue());
        deliveryLog.setIdDd(deliveryDriver.getId());
        deliveryLog = deliveryLogRepository.save(deliveryLog);
        return deliveryLog;
    }
}
