package com.itu.socialcom.demo.client.cart.repository;

import com.itu.socialcom.demo.client.cart.model.Cart;
import com.itu.socialcom.demo.client.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Cart entity.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    /**
     * Find all carts for a specific customer
     * @param customer the customer
     * @return list of carts
     */
    List<Cart> findByCustomer(Customer customer);
    
    /**
     * Find all active carts for a specific customer
     * @param customer the customer
     * @param state the state of the cart (true for active)
     * @return list of active carts
     */
    List<Cart> findByCustomerAndState(Customer customer, Boolean state);
    
    /**
     * Find the active cart for a specific customer
     * @param customer the customer
     * @param state the state of the cart (true for active)
     * @return optional containing the active cart if found
     */
    Optional<Cart> findFirstByCustomerAndStateOrderByCreatedAtDesc(Customer customer, Boolean state);
    
    /**
     * Find cart by id and customer
     * @param idCart the cart id
     * @param customer the customer
     * @return optional containing the cart if found
     */
    Optional<Cart> findByIdCartAndCustomer(Long idCart, Customer customer);
    
    /**
     * Find a cart by customer, seller, and state
     * @param customer the customer
     * @param sellerId the seller id
     * @param state the state of the cart (true for active)
     * @return optional containing the cart if found
     */
    Optional<Cart> findByCustomerAndIdSellerAndState(Customer customer, Long sellerId, Boolean state);
    
    /**
     * Check if a customer has an active cart
     * @param idCustomer the customer id
     * @return true if the customer has an active cart, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cart c WHERE c.customer.idCustomer = :idCustomer AND c.state = true")
    boolean hasActiveCart(@Param("idCustomer") Long idCustomer);
}