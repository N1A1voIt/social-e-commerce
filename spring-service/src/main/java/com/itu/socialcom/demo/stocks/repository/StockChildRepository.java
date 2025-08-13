package com.itu.socialcom.demo.stocks.repository;

import com.itu.socialcom.demo.stocks.StockChild;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockChildRepository extends JpaRepository<StockChild,Long> {
    @Query(value = """
        WITH recent_variants_retriever AS
        ( 
            SELECT id_variant, MAX(created_at) AS max_created_at 
            FROM stocks_child 
            WHERE id_variant IN (?) 
            GROUP BY id_variant 
        )
        SELECT sc.* 
        FROM stocks_child sc 
        JOIN recent_variants_retriever AS sub ON sc.id_variant = sub.id_variant AND sc.created_at = sub.max_created_at ORDER BY sc.created_at
    """,nativeQuery = true)
    List<StockChild> findMostRecentVariantsByVariantIds(List<Long> idVariants);
    @Query(value = """
        WITH recent_products_retriever AS
        ( 
            SELECT id_product, MAX(created_at) AS max_created_at 
            FROM stocks_child 
            WHERE id_product IN (?) 
            GROUP BY id_product 
        )
        SELECT sc.* 
        FROM stocks_child sc 
        JOIN recent_products_retriever AS sub ON sc.id_product = sub.id_product AND sc.created_at = sub.max_created_at ORDER BY sc.created_at
    """,nativeQuery = true)
    List<StockChild> findByLastProductRecords(List<Long> idProducts);

}
