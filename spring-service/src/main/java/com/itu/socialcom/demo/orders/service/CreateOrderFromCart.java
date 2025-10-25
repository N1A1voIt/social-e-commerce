package com.itu.socialcom.demo.orders.service;

import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.client.cart.model.Cart;
import com.itu.socialcom.demo.client.cart.model.CartDetails;
import com.itu.socialcom.demo.client.cart.service.CartService;
import com.itu.socialcom.demo.client.customer.Customer;
import com.itu.socialcom.demo.orders.OrderChild;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.products.variants.model.VariantInStock;
import com.itu.socialcom.demo.products.variants.repository.VariantInStockRepository;
import com.itu.socialcom.demo.stocks.InsufficientStockException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service("createOrderFromCart")
public class CreateOrderFromCart extends OrderCreationService {
    
    @Autowired
    private CartService cartService;
    @Autowired
    private VariantInStockRepository variantInStockRepository;
    
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
    public OrderParent createOrderFromMessage(com.itu.socialcom.demo.orders.dto.MessageOrdering messageOrdering, Seller seller) {
        throw new UnsupportedOperationException("This service is designed for cart-based order creation");
    }
    
    /**
     * Validate stock availability for all cart items before creating order
     * @param cartItems the cart items to validate
     * @throws InsufficientStockException if any item has insufficient stock
     */
    private void validateStockAvailability(List<CartDetails> cartItems) {
        for (CartDetails cartItem : cartItems) {
            VariantInStock variantStock = variantInStockRepository.findById(cartItem.getVariant().getIdVariant())
                    .orElseThrow(() -> new EntityNotFoundException("Stock information not found for variant with id: " + cartItem.getVariant().getIdVariant()));

            Double availableStock = variantStock.getVariantNumber();
            if (availableStock == null) {
                availableStock = 0.0;
            }

            if (cartItem.getQuantity().doubleValue() > availableStock) {
                throw new InsufficientStockException(
                    "Cannot create order: Insufficient stock for " + cartItem.getVariant().getTitle() + 
                    ". Requested: " + cartItem.getQuantity() + ", Available: " + availableStock
                );
            }
        }
    }

    /**
     * Create an order from a cart for a customer
     * @param cart the cart to convert to order
     * @param customer the customer placing the order
     * @param seller the seller (can be null for customer orders)
     * @param shippingAddress the shipping address
     * @param customerNumber the customer's phone number
     * @return the created order
     */
    @Transactional
    public OrderParent createOrderFromCart(Cart cart, Customer customer, Seller seller, 
                                          String shippingAddress, String customerNumber) {
        if (cart == null || !cart.isActive()) {
            throw new IllegalArgumentException("Cart must be active and not null");
        }
        
        List<CartDetails> cartItems = cartService.getCartItems(cart);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot create order from empty cart");
        }
        
        // Validate stock availability before creating order
        validateStockAvailability(cartItems);
        
        // Create OrderParent
        OrderParent orderParent = new OrderParent();
        orderParent.setIdCart(cart.getIdCart().intValue());
        orderParent.setIdCustomer(customer.getIdCustomer().intValue());
        orderParent.setDCustomerName(customer.getUsername() );
        orderParent.setShippingAddress(shippingAddress);
        orderParent.setCustomerNumber(customerNumber);
        orderParent.setCreatedAt(LocalDateTime.now());
        orderParent.setDStatus(1); // Order status: 1 = pending
        orderParent.setDescription("Order created from cart #" + cart.getIdCart());
        
        // Convert cart items to order children
        List<OrderChild> orderChildren = cartItems.stream()
                .map(cartItem -> {
                    OrderChild child = new OrderChild();
                    child.setQuantity(cartItem.getQuantity().doubleValue());
                    child.setPrice(cartItem.getVariant().getPrice().doubleValue());
                    child.setIdVariant(cartItem.getVariant().getIdVariant());
                    child.setIdProduct(cartItem.getVariant().getIdProduct());
                    child.setMediaUrl(cartItem.getVariant().getMediaUrl());
                    child.setProductName(cartItem.getVariant().getTitle());
                    child.setSku(cartItem.getVariant().getSku());
                    return child;
                })
                .toList();
        
        // Calculate total
        double total = orderChildren.stream()
                .mapToDouble(child -> child.getPrice() * child.getQuantity())
                .sum();
        
        orderParent.setDTotal(total);
        orderParent.setChilds(orderChildren);
        
        // Set seller from cart
//        orderParent.setIdSeller(cart.getSeller().getId().intValue());
        
        // Save the order
        return this.createOrder(orderParent, seller);
    }
    
    /**
     * Create an order from a cart for a customer (simplified version)
     * @param cart the cart to convert to order
     * @param customer the customer placing the order
     * @param shippingAddress the shipping address
     * @param customerNumber the customer's phone number
     * @return the created order
     */
    @Transactional
    public OrderParent createOrderFromCart(Cart cart, Customer customer, 
                                          String shippingAddress, String customerNumber) {
        return createOrderFromCart(cart, customer, null, shippingAddress, customerNumber);
    }
}
