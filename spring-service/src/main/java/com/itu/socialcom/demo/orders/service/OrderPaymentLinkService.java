package com.itu.socialcom.demo.orders.service;

import com.itu.socialcom.demo.orders.OrderParent;

public interface OrderPaymentLinkService {
    public OrderParent askUserToPay(OrderParent orderParent) throws Exception;
    public OrderParent askForUserToPayTheRest(OrderParent orderParent) throws Exception;
}
