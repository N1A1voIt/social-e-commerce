package com.itu.socialcom.demo.products.repository;

import com.itu.socialcom.demo.products.model.ProductCPL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCplRepository extends JpaRepository<ProductCPL,Long> {
}
