package com.itu.socialcom.demo.client.cart.service;

import com.itu.socialcom.demo.client.cart.model.Cart;
import com.itu.socialcom.demo.client.cart.model.CartDetails;
import com.itu.socialcom.demo.client.customer.Customer;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for cart operations.
 */
public interface CartService {
    
    /**
     * Get or create an active cart for a customer
     * @param customer the customer
     * @return the active cart
     */
    Cart getOrCreateActiveCart(Customer customer);
    
    /**
     * Get an active cart for a customer if it exists
     * @param customer the customer
     * @return the active cart or null if not found
     */
    Cart getActiveCart(Customer customer);
    
    /**
     * Get a cart by id for a specific customer
     * @param cartId the cart id
     * @param customer the customer
     * @return the cart or null if not found
     */
    Cart getCartById(Long cartId, Customer customer);
    
    /**
     * Get all carts for a customer
     * @param customer the customer
     * @return list of carts
     */
    List<Cart> getCartsByCustomer(Customer customer);
    
    /**
     * Add a product variant to the cart, creating a new cart for the seller if needed
     * @param customer the customer
     * @param productId the product ID
     * @param variantId the variant ID
     * @param quantity the quantity to add
     * @return the updated cart details
     */
    CartDetails addToCart(Customer customer, Long productId, Long variantId, BigDecimal quantity);
    
    /**
     * Get or create an active cart for a specific seller
     * @param customer the customer
     * @param sellerId the seller ID
     * @return the cart (active or newly created for the seller)
     */
    Cart getOrCreateCartForSeller(Customer customer, Long sellerId);
    
    /**
     * Update the quantity of a product variant in the cart
     * @param cart the cart
     * @param variantId the variant id
     * @param quantity the new quantity
     * @return the updated cart details
     */
    CartDetails updateCartItemQuantity(Cart cart, Long variantId, BigDecimal quantity);
    
    /**
     * Remove a product variant from the cart
     * @param cart the cart
     * @param variantId the variant id
     */
    void removeFromCart(Cart cart, Long variantId);
    
    /**
     * Clear all items from the cart
     * @param cart the cart
     */
    void clearCart(Cart cart);
    
    /**
     * Get all items in the cart
     * @param cart the cart
     * @return list of cart details
     */
    List<CartDetails> getCartItems(Cart cart);
    
    /**
     * Calculate the total price of all items in the cart
     * @param cart the cart
     * @return the total price
     */
    BigDecimal calculateCartTotal(Cart cart);
    
    /**
     * Count the number of items in the cart
     * @param cart the cart
     * @return the number of items
     */
    long countCartItems(Cart cart);
    
    /**
     * Deactivate a cart (e.g., after checkout)
     * @param cart the cart
     * @return the deactivated cart
     */
    Cart deactivateCart(Cart cart);
}