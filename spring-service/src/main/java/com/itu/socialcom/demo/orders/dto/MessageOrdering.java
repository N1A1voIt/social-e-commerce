package com.itu.socialcom.demo.orders.dto;

import com.itu.socialcom.demo.messages.dtol.VariantWithQuantity;
import com.itu.socialcom.demo.orders.OrderChild;
import com.itu.socialcom.demo.orders.OrderParent;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class MessageOrdering {
    List<VariantWithQuantity> variant;
    String idPc;
    String customerName;
    String customerAddress;
    String customerPhone;

    public OrderParent toOrderParent() {
        if (this.variant == null || this.variant.isEmpty()) {
            throw new IllegalArgumentException("Variant list cannot be null or empty.");
        }
        OrderParent orderParent = new OrderParent();
        orderParent.setIdPc(this.idPc);
        orderParent.setDCustomerName(this.customerName);
        orderParent.setShippingAddress(this.customerAddress);
        orderParent.setCustomerNumber(this.customerPhone);
        orderParent.setCreatedAt(LocalDateTime.now());
        double total = 0.0;
        List<OrderChild> childs = this.variant.stream()
                .map(vwq -> {
                    OrderChild child = new OrderChild();
                    child.setQuantity((double) vwq.getQuantity());
                    child.setPrice(vwq.getVariant().getPrice().doubleValue());
                    child.setIdVariant(vwq.getVariant().getIdVariant());
                    child.setIdProduct(vwq.getVariant().getIdProduct());
                    return child;
                })
                .collect(Collectors.toList());
        total = childs.stream()
                .mapToDouble(child -> child.getPrice() * child.getQuantity())
                .sum();
        orderParent.setDTotal(total);
        orderParent.setChilds(childs);
        orderParent.setDStatus(1);
        orderParent.setDescription("Order for " + this.customerName);
        return orderParent;
    }
}
