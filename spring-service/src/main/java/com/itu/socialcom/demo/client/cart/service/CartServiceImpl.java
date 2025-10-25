package com.itu.socialcom.demo.client.cart.service;

import com.itu.socialcom.demo.client.cart.model.Cart;
import com.itu.socialcom.demo.client.cart.model.CartDetails;
import com.itu.socialcom.demo.client.cart.repository.CartDetailsRepository;
import com.itu.socialcom.demo.client.cart.repository.CartRepository;
import com.itu.socialcom.demo.client.customer.Customer;
import com.itu.socialcom.demo.products.model.Product;
import com.itu.socialcom.demo.products.repository.ProductRepository;
import com.itu.socialcom.demo.products.variants.model.Variant;
import com.itu.socialcom.demo.products.variants.model.VariantInStock;
import com.itu.socialcom.demo.products.variants.repository.VariantInStockRepository;
import com.itu.socialcom.demo.products.variants.repository.VariantRepository;
import com.itu.socialcom.demo.stocks.InsufficientStockException;
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
    private final VariantInStockRepository variantInStockRepository;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository,
                          CartDetailsRepository cartDetailsRepository,
                          ProductRepository productRepository,
                          VariantRepository variantRepository,
                          VariantInStockRepository variantInStockRepository) {
        this.cartRepository = cartRepository;
        this.cartDetailsRepository = cartDetailsRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.variantInStockRepository = variantInStockRepository;
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

        // Check stock availability
        VariantInStock variantStock = variantInStockRepository.findById(variantId)
                .orElseThrow(() -> new EntityNotFoundException("Stock information not found for variant with id: " + variantId));

        // Get the available stock quantity
        Double availableStock = variantStock.getVariantNumber();
        if (availableStock == null) {
            availableStock = 0.0;
        }

        // Check if the item is already in the cart
        Optional<CartDetails> existingItem = cartDetailsRepository.findByCartAndVariant(cart, variant);

        if (existingItem.isPresent()) {
            // Update quantity if item already exists
            CartDetails cartDetails = existingItem.get();
            BigDecimal newTotalQuantity = cartDetails.getQuantity().add(quantity);

            // Check if the new total quantity exceeds available stock
            if (newTotalQuantity.doubleValue() > availableStock) {
                throw new InsufficientStockException(
                    "Cannot add " + quantity + " items to cart. Only " + 
                    availableStock + " items available in stock. You already have " + 
                    cartDetails.getQuantity() + " in your cart."
                );
            }

            cartDetails.setQuantity(newTotalQuantity);
            return cartDetailsRepository.save(cartDetails);
        } else {
            // Check if the requested quantity exceeds available stock
            if (quantity.doubleValue() > availableStock) {
                throw new InsufficientStockException(
                    "Cannot add " + quantity + " items to cart. Only " + 
                    availableStock + " items available in stock."
                );
            }

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
            cartDetailsRepository.delete(cartDetails);
            return null;
        } else {
            // Check stock availability
            VariantInStock variantStock = variantInStockRepository.findById(variantId)
                    .orElseThrow(() -> new EntityNotFoundException("Stock information not found for variant with id: " + variantId));

            // Get the available stock quantity
            Double availableStock = variantStock.getVariantNumber();
            if (availableStock == null) {
                availableStock = 0.0;
            }

            // Check if the requested quantity exceeds available stock
            if (quantity.doubleValue() > availableStock) {
                throw new InsufficientStockException(
                    "Cannot update cart to " + quantity + " items. Only " + 
                    availableStock + " items available in stock."
                );
            }

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
