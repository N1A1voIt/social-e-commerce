package com.itu.socialcom.demo.whatsapp.service;

import com.itu.socialcom.demo.orders.delivery.DeliveryLog;

public interface WhatsappReceiverService {
    DeliveryLog processIncomingMessage(String payload, String from, String message);
}
