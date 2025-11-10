# Product Stock View Entity and Repository

This module provides access to the `v_product_stock_cpl` database view which combines product information with stock details.

## View SQL

```sql
CREATE VIEW v_product_stock_cpl AS     
    WITH stock_details AS (         
        SELECT DISTINCT ON (id_product)             
            id_product,             
            d_product_number,             
            action_at         
        FROM stocks_child         
        ORDER BY id_product, created_at DESC     
    )     
    SELECT         
        p.id_product,
        description,
        name,
        price,
        media,
        id_seller,
        c.id_category,         
        c.val as category,
        COALESCE(d_product_number,0) as product_number,         
        CASE 
            WHEN COALESCE(d_product_number,0) = 0 THEN 'Out of Stock'                 
            WHEN COALESCE(d_product_number,0) >= 10 THEN 'In Stock'                 
            WHEN COALESCE(d_product_number,0) > 0 AND COALESCE(d_product_number,0) < 10 THEN 'Low Stock' 
        END as stock_status,
        p.sku_prefix         
    FROM products_v2 p         
    LEFT JOIN stock_details s ON p.id_product = s.id_product         
    JOIN category c on c.id_category = p.id_category;
```

## Files

### ProductStock.py

Contains the SQLAlchemy entity `ProductStockCpl` and Pydantic model `ProductStockCplOut` for the view.

**Entity Features:**
- Maps to `v_product_stock_cpl` view
- Provides helper methods like `is_in_stock()`, `is_low_stock()`, `is_out_of_stock()`
- Includes `get_formatted_price()` for display purposes

### ProductStockRepository.py

Provides repository methods to query the view:

**Available Methods:**

1. `find_all()` - Get all products with stock information
2. `find_by_id(product_id)` - Get a specific product by ID
3. `find_by_seller(seller_id)` - Get all products for a seller
4. `find_by_category(category_id)` - Get all products in a category
5. `find_by_stock_status(stock_status)` - Filter by stock status
6. `find_by_seller_and_categories(seller_id, category_ids)` - Filter by seller and categories
7. `find_low_stock_by_seller(seller_id)` - Get low stock products for a seller
8. `find_out_of_stock_by_seller(seller_id)` - Get out of stock products for a seller
9. `count_by_stock_status(seller_id)` - Count products by stock status

## Usage Example

```python
from products.ProductStockRepository import ProductStockRepository

# Create repository instance
repo = ProductStockRepository()

# Get all products with stock info
all_products = repo.find_all()

# Get products for a specific seller
seller_products = repo.find_by_seller(seller_id=123)

# Get low stock products for a seller
low_stock = repo.find_low_stock_by_seller(seller_id=123)

# Get stock status counts
stock_counts = repo.count_by_stock_status(seller_id=123)
# Returns: {'In Stock': 50, 'Low Stock': 10, 'Out of Stock': 5}

# Check individual product stock status
product = repo.find_by_id(product_id=456)
if product:
    print(f"Product: {product.name}")
    print(f"Stock Status: {product.stock_status}")
    print(f"Quantity: {product.product_number}")
    print(f"Price: {product.get_formatted_price()}")
```

## Stock Status Values

- **In Stock**: product_number >= 10
- **Low Stock**: 0 < product_number < 10  
- **Out of Stock**: product_number = 0

