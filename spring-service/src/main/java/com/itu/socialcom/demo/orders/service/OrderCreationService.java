package com.itu.socialcom.demo.orders.service;

import com.itu.socialcom.demo.messages.dtol.VariantWithQuantity;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.dto.MessageOrdering;
import com.itu.socialcom.demo.orders.repository.OrderChildRepository;
import com.itu.socialcom.demo.orders.repository.OrderParentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public abstract class OrderCreationService {
   @Autowired
   protected OrderChildRepository orderChildRepository;
   @Autowired
   protected OrderParentRepository orderParentRepository;
   public abstract OrderParent createOrder(OrderParent parent);
   public abstract OrderParent createOrderFromMessage(MessageOrdering messageOrdering);
}
