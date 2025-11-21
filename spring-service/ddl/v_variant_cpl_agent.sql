-- View for CPL Agent - Variant data with comprehensive product and stock information
CREATE VIEW v_variant_cpl_agent AS
WITH stock_details AS (
    SELECT DISTINCT ON (id_variant)
        id_variant,
        d_variant_number
    FROM stocks_child
    ORDER BY id_variant, created_at DESC
)
SELECT
    v.id_variant, 
    v.title, 
    v.price, 
    v.created_at, 
    v.updated_at, 
    v.id_product,
    v.sku,
    p.sku_prefix,
    p.name,
    p.id_seller,
    p.media,
    c.val as category,
    c.id_category,
    COALESCE(d_variant_number, 0) as variant_number,
    CASE 
        WHEN COALESCE(d_variant_number, 0) = 0 THEN 'Out of Stock'
        WHEN COALESCE(d_variant_number, 0) >= 10 THEN 'In Stock'
        WHEN COALESCE(d_variant_number, 0) > 0 AND COALESCE(d_variant_number, 0) < 10 THEN 'Low Stock' 
    END as stock_status
FROM variants_v2 v 
LEFT JOIN stock_details ON v.id_variant = stock_details.id_variant 
JOIN products_v2 p on v.id_product = p.id_product 
JOIN category c on p.id_category = c.id_category;
