package com.itu.socialcom.demo.client.cart.controller;

import com.itu.socialcom.demo.client.cart.dto.AddToCartRequest;
import com.itu.socialcom.demo.client.cart.dto.CartDTO;
import com.itu.socialcom.demo.client.cart.dto.UpdateCartItemRequest;
import com.itu.socialcom.demo.client.cart.mapper.CartMapper;
import com.itu.socialcom.demo.client.cart.model.Cart;
import com.itu.socialcom.demo.client.cart.model.CartDetails;
import com.itu.socialcom.demo.client.cart.service.CartService;
import com.itu.socialcom.demo.client.customer.Customer;
import com.itu.socialcom.demo.client.customer.CustomerService;
import com.itu.socialcom.demo.client.customertoken.CustomerTokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for cart operations.
 */
@RestController
@RequestMapping("/api/customer/cart")
public class CartController {

    private final CartService cartService;
    private final CustomerService customerService;
    private final CustomerTokenService customerTokenService;
    private final CartMapper cartMapper;

    @Autowired
    public CartController(CartService cartService, 
                         CustomerService customerService, 
                         CustomerTokenService customerTokenService,
                         CartMapper cartMapper) {
        this.cartService = cartService;
        this.customerService = customerService;
        this.customerTokenService = customerTokenService;
        this.cartMapper = cartMapper;
    }
    /**
     * Get the current customer's active carts
     * @return the cart DTOs
     */
    @GetMapping
    public ResponseEntity<List<CartDTO>> getActiveCarts(@RequestHeader("Authorization") String authHeader) {
        Customer customer = getCurrentCustomer(authHeader);
        
        // Get all active carts for the customer (grouped by seller)
        List<Cart> carts = cartService.getCartsByCustomer(customer);
                
        if (carts.isEmpty()) {
            // Return empty list if no active carts
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        // Convert each cart to DTO
        List<CartDTO> cartDTOs = carts.stream()
                .map(cart -> {
                    List<CartDetails> cartItems = cartService.getCartItems(cart);
                    BigDecimal totalPrice = cartService.calculateCartTotal(cart);
                    return cartMapper.toCartDTO(cart, cartItems, totalPrice);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(cartDTOs);
    }

    /**
     * Get a specific cart by ID
     * @param cartId the cart ID
     * @return the cart DTO
     */
    @GetMapping("/{cartId}")
    public ResponseEntity<CartDTO> getCartById(@PathVariable Long cartId, @RequestHeader("Authorization") String authHeader) {
        Customer customer = getCurrentCustomer(authHeader);
        Cart cart = cartService.getCartById(cartId, customer);

        if (cart == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found");
        }

        List<CartDetails> cartItems = cartService.getCartItems(cart);
        BigDecimal totalPrice = cartService.calculateCartTotal(cart);

        return ResponseEntity.ok(cartMapper.toCartDTO(cart, cartItems, totalPrice));
    }

    /**
     * Add an item to the cart
     * @param request the add to cart request
     * @return the updated cart DTO
     */
    @PostMapping("/items")
    public ResponseEntity<CartDTO> addToCart(@Valid @RequestBody AddToCartRequest request, @RequestHeader("Authorization") String authHeader) {
        Customer customer = getCurrentCustomer(authHeader);
        
        // This will automatically handle getting or creating a cart for the product's seller
        CartDetails cartDetails = cartService.addToCart(customer, request.getProductId(), request.getVariantId(), request.getQuantity());
        
        // Get the cart that the item was added to
        Cart cart = cartDetails.getCart();
        List<CartDetails> cartItems = cartService.getCartItems(cart);
        BigDecimal totalPrice = cartService.calculateCartTotal(cart);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartMapper.toCartDTO(cart, cartItems, totalPrice));
    }

    /**
     * Update the quantity of an item in the cart
     * @param request the update cart item request
     * @return the updated cart DTO
     */
    @PutMapping("/items")
    public ResponseEntity<CartDTO> updateCartItem(@Valid @RequestBody UpdateCartItemRequest request, @RequestHeader("Authorization") String authHeader) {
        Customer customer = getCurrentCustomer(authHeader);
        Cart cart = cartService.getOrCreateActiveCart(customer);

        cartService.updateCartItemQuantity(cart, request.getVariantId(), request.getQuantity());

        List<CartDetails> cartItems = cartService.getCartItems(cart);
        BigDecimal totalPrice = cartService.calculateCartTotal(cart);

        return ResponseEntity.ok(cartMapper.toCartDTO(cart, cartItems, totalPrice));
    }

    /**
     * Remove an item from the cart
     * @param variantId the variant ID to remove
     * @return the updated cart DTO
     */
    @DeleteMapping("/items/{variantId}")
    public ResponseEntity<CartDTO> removeFromCart(@PathVariable Long variantId, @RequestHeader("Authorization") String authHeader) {
        Customer customer = getCurrentCustomer(authHeader);
        Cart cart = cartService.getOrCreateActiveCart(customer);

        cartService.removeFromCart(cart, variantId);

        List<CartDetails> cartItems = cartService.getCartItems(cart);
        BigDecimal totalPrice = cartService.calculateCartTotal(cart);

        return ResponseEntity.ok(cartMapper.toCartDTO(cart, cartItems, totalPrice));
    }

    /**
     * Clear all items from the cart
     * @return the empty cart DTO
     */
    @DeleteMapping("/items")
    public ResponseEntity<CartDTO> clearCart(@RequestHeader("Authorization") String authHeader) {
        Customer customer = getCurrentCustomer(authHeader);
        Cart cart = cartService.getOrCreateActiveCart(customer);

        cartService.clearCart(cart);

        List<CartDetails> cartItems = cartService.getCartItems(cart);
        BigDecimal totalPrice = cartService.calculateCartTotal(cart);

        return ResponseEntity.ok(cartMapper.toCartDTO(cart, cartItems, totalPrice));
    }

    /**
     * Get the current authenticated customer
     * @param authHeader the Authorization header
     * @return the customer entity
     */
    private Customer getCurrentCustomer(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing Authorization header");
        }

        String token = authHeader.substring(7);

        return customerTokenService.findCustomerByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token"));
    }
}
