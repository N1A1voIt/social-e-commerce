package com.itu.socialcom.demo.client.cart.repository;

import com.itu.socialcom.demo.client.cart.model.Cart;
import com.itu.socialcom.demo.client.cart.model.CartDetails;
import com.itu.socialcom.demo.products.variants.model.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CartDetails entity.
 */
@Repository
public interface CartDetailsRepository extends JpaRepository<CartDetails, Long> {
    
    /**
     * Find all cart details for a specific cart
     * @param cart the cart
     * @return list of cart details
     */
    List<CartDetails> findByCart(Cart cart);
    
    /**
     * Find cart detail by cart and variant
     * @param cart the cart
     * @param variant the variant
     * @return optional containing the cart detail if found
     */
    Optional<CartDetails> findByCartAndVariant(Cart cart, Variant variant);
    
    /**
     * Delete all cart details for a specific cart
     * @param cart the cart
     */
    void deleteByCart(Cart cart);
    
    /**
     * Delete cart detail by cart and variant
     * @param cart the cart
     * @param variant the variant
     */
    void deleteByCartAndVariant(Cart cart, Variant variant);
    
    /**
     * Update quantity for a specific cart detail
     * @param idCd the cart detail id
     * @param quantity the new quantity
     * @return number of rows affected
     */
    @Modifying
    @Query("UPDATE CartDetails cd SET cd.quantity = :quantity WHERE cd.idCd = :idCd")
    int updateQuantity(@Param("idCd") Long idCd, @Param("quantity") BigDecimal quantity);
    
    /**
     * Count the number of items in a cart
     * @param cart the cart
     * @return the number of items
     */
    long countByCart(Cart cart);
    
    /**
     * Calculate the total price of all items in a cart
     * @param idCart the cart id
     * @return the total price
     */
    @Query("SELECT SUM(cd.quantity * v.price) FROM CartDetails cd JOIN cd.variant v WHERE cd.cart.idCart = :idCart")
    BigDecimal calculateCartTotal(@Param("idCart") Long idCart);
}