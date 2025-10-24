package com.itu.socialcom.demo.client.cart.service;

import com.itu.socialcom.demo.client.cart.model.Cart;
import com.itu.socialcom.demo.client.cart.model.CartDetails;
import com.itu.socialcom.demo.client.cart.repository.CartDetailsRepository;
import com.itu.socialcom.demo.client.cart.repository.CartRepository;
import com.itu.socialcom.demo.client.customer.Customer;
import com.itu.socialcom.demo.products.model.Product;
import com.itu.socialcom.demo.products.repository.ProductRepository;
import com.itu.socialcom.demo.products.variants.model.Variant;
import com.itu.socialcom.demo.products.variants.repository.VariantRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the CartService interface.
 */
@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartDetailsRepository cartDetailsRepository;
    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, 
                          CartDetailsRepository cartDetailsRepository,
                          ProductRepository productRepository,
                          VariantRepository variantRepository) {
        this.cartRepository = cartRepository;
        this.cartDetailsRepository = cartDetailsRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    @Override
    public Cart getOrCreateActiveCart(Customer customer) {
        Optional<Cart> activeCart = cartRepository.findFirstByCustomerAndStateOrderByCreatedAtDesc(customer, true);
        return activeCart.orElseGet(() -> {
            Cart newCart = new Cart(customer);
            return cartRepository.save(newCart);
        });
    }

    @Override
    public Cart getActiveCart(Customer customer) {
        return cartRepository.findFirstByCustomerAndStateOrderByCreatedAtDesc(customer, true).orElse(null);
    }

    @Override
    public Cart getCartById(Long cartId, Customer customer) {
        return cartRepository.findByIdCartAndCustomer(cartId, customer).orElse(null);
    }

    @Override
    public List<Cart> getCartsByCustomer(Customer customer) {
        return cartRepository.findByCustomer(customer);
    }

    @Override
    @Transactional
    public CartDetails addToCart(Cart cart, Long productId, Long variantId, BigDecimal quantity) {
        if (!cart.isActive()) {
            throw new IllegalStateException("Cannot add items to an inactive cart");
        }
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));
        
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new EntityNotFoundException("Variant not found with id: " + variantId));
        
        // Check if the variant belongs to the product
        if (!variant.getIdProduct().equals(product.getIdProduct())) {
            throw new IllegalArgumentException("Variant does not belong to the specified product");
        }
        
        // Check if the item is already in the cart
        Optional<CartDetails> existingItem = cartDetailsRepository.findByCartAndVariant(cart, variant);
        
        if (existingItem.isPresent()) {
            // Update quantity if item already exists
            CartDetails cartDetails = existingItem.get();
            cartDetails.setQuantity(cartDetails.getQuantity().add(quantity));
            return cartDetailsRepository.save(cartDetails);
        } else {
            // Add new item to cart
            CartDetails cartDetails = new CartDetails(cart, product, variant, quantity);
            return cartDetailsRepository.save(cartDetails);
        }
    }

    @Override
    @Transactional
    public CartDetails updateCartItemQuantity(Cart cart, Long variantId, BigDecimal quantity) {
        if (!cart.isActive()) {
            throw new IllegalStateException("Cannot update items in an inactive cart");
        }
        
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new EntityNotFoundException("Variant not found with id: " + variantId));
        
        CartDetails cartDetails = cartDetailsRepository.findByCartAndVariant(cart, variant)
                .orElseThrow(() -> new EntityNotFoundException("Item not found in cart"));
        
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            // Remove item if quantity is zero or negative
            cartDetailsRepository.delete(cartDetails);
            return null;
        } else {
            // Update quantity
            cartDetails.setQuantity(quantity);
            return cartDetailsRepository.save(cartDetails);
        }
    }

    @Override
    @Transactional
    public void removeFromCart(Cart cart, Long variantId) {
        if (!cart.isActive()) {
            throw new IllegalStateException("Cannot remove items from an inactive cart");
        }
        
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new EntityNotFoundException("Variant not found with id: " + variantId));
        
        cartDetailsRepository.deleteByCartAndVariant(cart, variant);
    }

    @Override
    @Transactional
    public void clearCart(Cart cart) {
        if (!cart.isActive()) {
            throw new IllegalStateException("Cannot clear an inactive cart");
        }
        
        cartDetailsRepository.deleteByCart(cart);
    }

    @Override
    public List<CartDetails> getCartItems(Cart cart) {
        return cartDetailsRepository.findByCart(cart);
    }

    @Override
    public BigDecimal calculateCartTotal(Cart cart) {
        BigDecimal total = cartDetailsRepository.calculateCartTotal(cart.getIdCart());
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public long countCartItems(Cart cart) {
        return cartDetailsRepository.countByCart(cart);
    }

    @Override
    @Transactional
    public Cart deactivateCart(Cart cart) {
        cart.setState(false);
        return cartRepository.save(cart);
    }
}