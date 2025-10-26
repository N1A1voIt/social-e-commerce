package com.itu.socialcom.demo.orders.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CustomerOrderDTO {
    private Long idOrderM;
    private String description;
    private LocalDateTime createdAt;
    private Double dTotal;
    private String dCustomerName;
    private Integer dStatus;
    private String statusLabel;
    private String shippingAddress;
    private String customerNumber;
    private Integer idSeller;
    private String sellerName;
    private List<OrderItemDTO> items;
}

