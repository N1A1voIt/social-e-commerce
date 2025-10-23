package com.itu.socialcom.demo.stocks.repository;

import com.itu.socialcom.demo.stocks.StockChild;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StockChildRepository extends JpaRepository<StockChild,Long> {
    @Query(value = """
        WITH recent_variants_retriever AS
        ( 
            SELECT id_variant, MAX(created_at) AS max_created_at 
            FROM stocks_child 
            WHERE id_variant IN (?) 
            GROUP BY id_variant 
        )
        SELECT sc.id_st_ch, sc.price, sc.action_at, coalesce(sc.input,0) as input, coalesce(sc.output,0) as output, coalesce(sc.d_product_number,0) as d_product_number, coalesce(sc.d_variant_number,0) as d_variant_number, sc.product_name, sc.variant_name, sc.id_product, sc.id_variant, sc.id_mv,sc.created_at
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
        SELECT sc.id_st_ch, sc.price, sc.action_at, coalesce(sc.input,0) as input, coalesce(sc.output,0) as output, coalesce(sc.d_product_number,0) as d_product_number, coalesce(sc.d_variant_number,0) as d_variant_number, sc.product_name, sc.variant_name, sc.id_product, sc.id_variant, sc.id_mv,sc.created_at
        FROM stocks_child sc 
        JOIN recent_products_retriever AS sub ON sc.id_product = sub.id_product AND sc.created_at = sub.max_created_at ORDER BY sc.created_at
    """,nativeQuery = true)
    List<StockChild> findByLastProductRecords(List<Long> idProducts);
    // Method to get the single most recent record before a given date for a variant.
    @Query(value = """
    SELECT sc.* FROM stocks_child sc 
    WHERE sc.id_variant = ?1 AND sc.created_at < ?2 
    ORDER BY sc.created_at DESC 
    LIMIT 1
""", nativeQuery = true)
    Optional<StockChild> findLastRecordForVariantBeforeDate(Long variantId, LocalDateTime date);

    // Method to get all records for a variant after a given date.
    @Query(value = """
        SELECT *
        FROM stocks_child
        WHERE id_variant = ?1 AND created_at >= ?2
        ORDER BY created_at
    """, nativeQuery = true)
    List<StockChild> findRecordsForVariantFromDate(Long variantId, LocalDateTime date);

    List<StockChild> findByIdMv(Long idMv);

    List<StockChild> findByActionAtAfterOrderByIdProductAscActionAtAsc(LocalDateTime actionAtAfter);

    List<StockChild> findByIdProductAndActionAtAfterOrderByActionAtAsc(Long idProduct, LocalDateTime actionAtAfter);
}
