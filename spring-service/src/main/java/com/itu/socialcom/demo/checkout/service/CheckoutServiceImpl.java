package com.itu.socialcom.demo.checkout.service;

import com.itu.socialcom.demo.checkout.dto.CheckoutRequest;
import com.itu.socialcom.demo.client.cart.model.Cart;
import com.itu.socialcom.demo.client.cart.model.CartDetails;
import com.itu.socialcom.demo.client.cart.repository.CartDetailsRepository;
import com.itu.socialcom.demo.client.cart.repository.CartRepository;
import com.itu.socialcom.demo.client.customer.Customer;
import com.itu.socialcom.demo.client.customer.CustomerRepository;
import com.itu.socialcom.demo.orders.OrderChild;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.repository.OrderChildRepository;
import com.itu.socialcom.demo.orders.repository.OrderParentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final CartRepository cartRepository;
    private final CartDetailsRepository cartDetailsRepository;
    private final OrderParentRepository orderParentRepository;
    private final OrderChildRepository orderChildRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public OrderParent checkout(CheckoutRequest checkoutRequest) {
        Customer customer = customerRepository.findById(checkoutRequest.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found."));

        Cart cart = cartRepository.findByCustomerAndIdSellerAndState(customer, checkoutRequest.getSellerId(), true)
                .orElseThrow(() -> new EntityNotFoundException("Active cart not found for the given customer and seller."));

        List<CartDetails> cartDetails = cartDetailsRepository.findByCart(cart);
        if (cartDetails.isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty cart.");
        }

        OrderParent order = new OrderParent();
        order.setCreatedAt(LocalDateTime.now());
        order.setDStatus(1); // Assuming 1 for 'pending'
        order.setIdCustomer(cart.getCustomer().getIdCustomer().intValue());
        order.setIdSeller(cart.getIdSeller().intValue());
        order.setIdCart(cart.getIdCart().intValue());
        order.setDCustomerName(cart.getCustomer().getUsername());
        order.setShippingAddress(checkoutRequest.getShippingAddress());
        order.setCustomerNumber(checkoutRequest.getPhoneNumber());
        order.setDescription("Order from marketplace checkout for customer: " + cart.getCustomer().getUsername());
        // Set required fields for marketplace orders (not from social media)
        // For marketplace orders, id_pc can be the customer ID and id_managed_pages can be null or a default
//        order.setIdPc("CUSTOMER_" + cart.getCustomer().getIdCustomer());
        order.setIdManagedPages(null); // Marketplace orders don't have managed pages

        double totalAmount = 0.0;
        List<OrderChild> orderChildren = new ArrayList<>();
        for (CartDetails detail : cartDetails) {
            OrderChild orderChild = new OrderChild();
            orderChild.setIdProduct(detail.getVariant().getIdProduct());
            orderChild.setIdVariant(detail.getVariant().getIdVariant());
            orderChild.setQuantity(detail.getQuantity().doubleValue());
            orderChild.setPrice(detail.getVariant().getPrice().doubleValue());
            
            // Set optional fields if available
            if (detail.getVariant().getMediaUrl() != null) {
                orderChild.setMediaUrl(detail.getVariant().getMediaUrl());
            }
            if (detail.getVariant().getSku() != null) {
                orderChild.setSku(detail.getVariant().getSku());
            }
            if (detail.getVariant().getTitle() != null) {
                orderChild.setProductName(detail.getVariant().getTitle());
            }
            
            orderChildren.add(orderChild);
            totalAmount += detail.getQuantity().doubleValue() * detail.getVariant().getPrice().doubleValue();
        }

        order.setDTotal(totalAmount);
        OrderParent savedOrder = orderParentRepository.save(order);

        for (OrderChild child : orderChildren) {
            child.setIdOrderM(savedOrder.getIdOrderM());
            orderChildRepository.save(child);
        }

        cart.setState(false); // Deactivate the cart
        cartRepository.save(cart);

        return savedOrder;
    }
}
