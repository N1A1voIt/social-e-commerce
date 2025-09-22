package com.itu.socialcom.demo.orders.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.itu.socialcom.demo.messages.dtol.VariantWithQuantity;
import com.itu.socialcom.demo.orders.OrderChild;
import com.itu.socialcom.demo.orders.OrderParent;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class MessageOrdering {
    @JsonProperty("variants")
    List<VariantWithQuantity> variants;
    @JsonProperty("idPc")
    String idPc;
    @JsonProperty("customerName")
    String customerName;
    @JsonProperty("shippingAddress")
    String shippingAddress;
    @JsonProperty("customerNumber")
    String customerNumber;
    @JsonProperty("idManagedPages")
    Integer idManagedPages;

    public OrderParent toOrderParent() {
        if (this.variants == null || this.variants.isEmpty()) {
            throw new IllegalArgumentException("Variant list cannot be null or empty.");
        }
        OrderParent orderParent = new OrderParent();
        orderParent.setIdPc(this.idPc);
        orderParent.setDCustomerName(this.customerName);
        orderParent.setShippingAddress(this.shippingAddress);
        orderParent.setCustomerNumber(this.customerNumber);
        orderParent.setCreatedAt(LocalDateTime.now());
        orderParent.setIdManagedPages(idManagedPages);
        double total = 0.0;
        List<OrderChild> childs = this.variants.stream()
                .map(vwq -> {
                    OrderChild child = new OrderChild();
                    child.setQuantity((double) vwq.getQuantity());
                    child.setPrice(vwq.getVariant().getPrice().doubleValue());
                    child.setIdVariant(vwq.getVariant().getIdVariant());
                    child.setIdProduct(vwq.getVariant().getIdProduct());
                    child.setMediaUrl(vwq.getVariant().getMediaUrl());
                    child.setProductName(vwq.getVariant().getTitle());
                    child.setSku(vwq.getVariant().getSku());
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
