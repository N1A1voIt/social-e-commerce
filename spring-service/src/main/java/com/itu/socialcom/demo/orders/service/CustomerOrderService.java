package com.itu.socialcom.demo.orders.service;

import com.itu.socialcom.demo.orders.OrderChild;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.OrderStatus;
import com.itu.socialcom.demo.orders.dto.CustomerOrderDTO;
import com.itu.socialcom.demo.orders.dto.OrderItemDTO;
import com.itu.socialcom.demo.orders.repository.OrderChildRepository;
import com.itu.socialcom.demo.orders.repository.OrderParentRepository;
import com.itu.socialcom.demo.orders.tempLink.TempLinkRepository;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.authentication.user.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerOrderService {

    private final OrderParentRepository orderParentRepository;
    private final OrderChildRepository orderChildRepository;
    private final SellerRepository sellerRepository;
    private final TempLinkRepository tempLinkRepository;

    public List<CustomerOrderDTO> getCustomerOrders(Long customerId) {
        List<OrderParent> orders = orderParentRepository.findAll().stream()
                .filter(order -> order.getIdCustomer() != null && order.getIdCustomer().equals(customerId.intValue()))
                .collect(Collectors.toList());

        return orders.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private CustomerOrderDTO mapToDTO(OrderParent order) {
        CustomerOrderDTO dto = new CustomerOrderDTO();
        dto.setIdOrderM(order.getIdOrderM());
        dto.setDescription(order.getDescription());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setDTotal(order.getDTotal());
        dto.setDCustomerName(order.getDCustomerName());
        dto.setDStatus(order.getDStatus());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setCustomerNumber(order.getCustomerNumber());
        dto.setIdSeller(order.getIdSeller());

        // Set status label
        if (order.getDStatus() != null) {
            OrderStatus status = OrderStatus.fromCode(order.getDStatus());
            dto.setStatusLabel(status.getLabel());
        }

        // Get seller name
        if (order.getIdSeller() != null) {
            sellerRepository.findById(order.getIdSeller().longValue())
                    .ifPresent(seller -> dto.setSellerName(seller.getUsername()));
        }

        // Get order items
        List<OrderChild> orderChildren = orderChildRepository.findByIdOrderM(order.getIdOrderM());
        dto.setItems(orderChildren.stream()
                .map(this::mapToItemDTO)
                .collect(Collectors.toList()));

        // Get payment link if status is 5 (payment pending)
        if (order.getDStatus() != null && order.getDStatus() == 5) {
            List<com.itu.socialcom.demo.orders.tempLink.TempLink> tempLinks = tempLinkRepository.findByIdOrderM(order.getIdOrderM().intValue());
            if (!tempLinks.isEmpty()) {
                // Get the most recent non-expired and non-used link
                tempLinks.stream()
                        .filter(link -> !link.getUsed() && link.getExpiredAt().isAfter(java.time.LocalDateTime.now()))
                        .findFirst()
                        .ifPresent(link -> dto.setPaymentLink(link.getTempLink()));
            }
        }

        return dto;
    }

    private OrderItemDTO mapToItemDTO(OrderChild child) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setIdOrderDetails(child.getIdOrderDetails());
        dto.setPrice(child.getPrice());
        dto.setQuantity(child.getQuantity());
        dto.setIdVariant(child.getIdVariant());
        dto.setIdProduct(child.getIdProduct());
        dto.setMediaUrl(child.getMediaUrl());
        dto.setSku(child.getSku());
        dto.setProductName(child.getProductName());
        return dto;
    }
}

