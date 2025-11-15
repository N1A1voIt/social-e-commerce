package com.itu.socialcom.demo.client.cart.mapper;

import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.authentication.user.SellerRepository;
import com.itu.socialcom.demo.client.cart.dto.CartDTO;
import com.itu.socialcom.demo.client.cart.dto.CartItemDTO;
import com.itu.socialcom.demo.client.cart.model.Cart;
import com.itu.socialcom.demo.client.cart.model.CartDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between cart entities and DTOs.
 */
@Component
public class CartMapper {
    
    @Autowired
    private SellerRepository sellerRepository;
    
    /**
     * Convert a Cart entity and its details to a CartDTO
     * @param cart the cart entity
     * @param cartDetails the list of cart details
     * @param totalPrice the total price of all items in the cart
     * @return the cart DTO
     */
    public CartDTO toCartDTO(Cart cart, List<CartDetails> cartDetails, BigDecimal totalPrice) {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setCartId(cart.getIdCart());
        cartDTO.setCustomerId(cart.getCustomer().getIdCustomer());
        cartDTO.setCreatedAt(cart.getCreatedAt());
        cartDTO.setActive(cart.isActive());
        cartDTO.setIdSeller(cart.getIdSeller());
        
        // Fetch seller name from database
        String sellerName = sellerRepository.findById(cart.getIdSeller())
                .map(Seller::getUsername)
                .orElse("Unknown Seller");
        cartDTO.setSellerName(sellerName);
        
        List<CartItemDTO> itemDTOs = cartDetails.stream()
                .map(this::toCartItemDTO)
                .collect(Collectors.toList());
        
        cartDTO.setItems(itemDTOs);
        cartDTO.setItemCount(cartDetails.size());
        cartDTO.setTotalPrice(totalPrice);
        
        return cartDTO;
    }
    
    /**
     * Convert a CartDetails entity to a CartItemDTO
     * @param cartDetails the cart details entity
     * @return the cart item DTO
     */
    public CartItemDTO toCartItemDTO(CartDetails cartDetails) {
        CartItemDTO itemDTO = new CartItemDTO();
        itemDTO.setProductId(cartDetails.getProduct().getIdProduct());
        itemDTO.setProductName(cartDetails.getProduct().getName());
        itemDTO.setProductMedia(cartDetails.getProduct().getMedia());
        itemDTO.setVariantId(cartDetails.getVariant().getIdVariant());
        itemDTO.setVariantTitle(cartDetails.getVariant().getTitle());
        itemDTO.setPrice(cartDetails.getVariant().getPrice());
        itemDTO.setQuantity(cartDetails.getQuantity());
        itemDTO.setTotalPrice(cartDetails.getTotalPrice());
        
        return itemDTO;
    }
}