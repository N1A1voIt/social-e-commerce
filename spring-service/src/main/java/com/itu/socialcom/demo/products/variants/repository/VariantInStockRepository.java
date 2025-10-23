package com.itu.socialcom.demo.products.variants.repository;

import com.itu.socialcom.demo.products.variants.model.VariantInStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VariantInStockRepository extends JpaRepository<VariantInStock, Long> {
    List<VariantInStock> findVariantInStockByIdProduct(Long idProduct);
    List<VariantInStock> findByIdProductAndIdVariantIn(Long idProduct, List<Long> variantIds);
}
