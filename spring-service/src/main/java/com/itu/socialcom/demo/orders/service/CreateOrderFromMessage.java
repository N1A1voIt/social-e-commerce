package com.itu.socialcom.demo.orders.service;

import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.messages.dtol.VariantWithQuantity;
import com.itu.socialcom.demo.orders.OrderChild;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.dto.MessageOrdering;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service("createOrderFromMessage")
@Primary
public class CreateOrderFromMessage extends OrderCreationService {
    @Override
    @Transactional
    public OrderParent createOrder(OrderParent parent, Seller seller) {
        double totalPrice = 0.0;
        for (OrderChild child: parent.getChilds()) {
            totalPrice += child.getPrice() * child.getQuantity();
        }
        parent.setIdSeller(seller.getId().intValue());
        parent.setDTotal(totalPrice);
        parent.setDStatus(1);
        parent.setCreatedAt(LocalDateTime.now());
        super.orderParentRepository.save(parent);
        for (OrderChild child : parent.getChilds()) {
            child.setIdOrderM(parent.getIdOrderM());
            super.orderChildRepository.save(child);
        }
        return parent;
    }

    @Override
    @Transactional
    public OrderParent createOrderFromMessage(MessageOrdering messageOrdering, Seller seller) {
        OrderParent orderParent = messageOrdering.toOrderParent();
        return this.createOrder(orderParent,seller);
    }
}
