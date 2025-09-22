package com.itu.socialcom.demo.orders;

import lombok.Data;

import java.util.List;

@Data
public class OrdersToDisplay {
    List<OrderParent> orders;
    int totalOrders;
}
