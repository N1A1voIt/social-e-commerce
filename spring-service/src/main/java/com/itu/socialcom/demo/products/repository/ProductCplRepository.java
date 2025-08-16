package com.itu.socialcom.demo.products.repository;

import com.itu.socialcom.demo.products.model.ProductCPL;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCplRepository extends JpaRepository<ProductCPL,Long> {
    List<ProductCPL> findByIdSeller(Long idSeller);
}
