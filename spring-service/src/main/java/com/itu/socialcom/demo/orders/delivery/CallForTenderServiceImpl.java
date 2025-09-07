package com.itu.socialcom.demo.orders.delivery;

import com.itu.socialcom.demo.delivery.entity.AmountDistance;
import com.itu.socialcom.demo.delivery.entity.Delivery;
import com.itu.socialcom.demo.delivery.repository.AmountDistanceRepository;
import com.itu.socialcom.demo.delivery.repository.DeliveryRepository;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.dto.CallForTendersRequest;
import com.itu.socialcom.demo.orders.repository.OrderParentRepository;
import com.itu.socialcom.demo.shipping.entity.ShippingPoint;
import com.itu.socialcom.demo.shipping.service.ShippingPointService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CallForTenderServiceImpl implements CallForTenderService{
    @Autowired
    DeliveryRepository deliveryRepository;
    @Autowired
    AmountDistanceRepository amountDistanceRepository;
    @Autowired
    ShippingPointService shippingPointService;
    @Autowired
    OrderParentRepository orderParentRepository;
    @Transactional
    @Override
    public Delivery transfromToDelivery(CallForTendersRequest request) {
        OrderParent orderParent = request.getOrderParent();
        orderParent.setDStatus(25);
        orderParentRepository.save(orderParent);
        Delivery delivery = new Delivery();
        delivery.setPhoneNumber(request.getOrderParent().getCustomerNumber());
        delivery.setOrderMotherId(request.getOrderParent().getIdOrderM());
        delivery.setShippingPointId(request.getShippingPointId());
        delivery.setStatus("CALL_FOR_TENDERED");
        delivery.setStartedAt(LocalDateTime.now());
        AmountDistance amountDistance = amountDistanceRepository.findByManagedPageId(request.getOrderParent().getIdManagedPages().longValue()).get(0);
        ShippingPoint shippingPoint = shippingPointService.getShippingPointById(request.getShippingPointId()).orElse(null);

        if(amountDistance!=null){
            delivery.setAmount(BigDecimal.valueOf(amountDistance.getPricePerDistance().doubleValue() * shippingPoint.getDistance().doubleValue()));
            delivery.setDistance(shippingPoint.getDistance().doubleValue());
        }
//        delivery.setAmount();
        deliveryRepository.save(delivery);
        return delivery;
    }
}
