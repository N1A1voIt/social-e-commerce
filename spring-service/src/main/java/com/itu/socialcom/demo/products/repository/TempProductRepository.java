package com.itu.socialcom.demo.products.repository;

import com.itu.socialcom.demo.products.model.TempProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TempProductRepository extends JpaRepository<TempProduct, Long> {
    TempProduct findByIdSellerAndState(Long idSeller, Boolean state);
}
