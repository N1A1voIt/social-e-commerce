# Generic Chat Agent - NLP Query Examples

This document provides example natural language queries that can be used with the generic chat agent to access both sales data and product inventory information.

## Sales Analysis Queries

### Sales Summary
- "Show me my sales summary"
- "What are my total sales?"
- "Give me a sales summary"
- "How much revenue did I make?"

### Sales Trends/Story
- "Show me my sales trends"
- "Tell me a story about my sales"
- "How are my sales evolving?"
- "What's the trend in my sales data?"

## Product Inventory Queries

### All Products with Stock
- "Show me all my products"
- "List all my products with stock information"
- "What products do I have?"
- "Give me my complete product inventory"

### Low Stock Products
- "Which products are running low?"
- "Show me low stock products"
- "What needs to be restocked?"
- "Which items have low inventory?"

### Out of Stock Products
- "What products are out of stock?"
- "Show me items that need urgent restocking"
- "Which products have zero inventory?"
- "List out of stock items"

### Stock Status Summary
- "Give me a stock status overview"
- "How many products are in stock?"
- "Show me inventory summary"
- "What's my stock situation?"

### Products by Category
- "Show me products in category 5"
- "What products do I have in category electronics?"
- "List products from a specific category"

### Specific Product Details
- "Show me details for product 123"
- "Get information about product ID 456"
- "What are the details of product 789?"

## Example API Request

```http
POST http://localhost:8000/generic-chat
Content-Type: application/json

{
  "query": "Show me all my low stock products",
  "user_id": 123
}
```

## Tool Functions Available

### Sales Tools
1. `extract_sales_summary(user_id)` - Get overall sales summary
2. `extract_sales_story(user_id)` - Get sales trends and evolution

### Product Inventory Tools
1. `get_all_products_with_stock(user_id)` - Get all products with stock info
2. `get_low_stock_products(user_id)` - Get products with quantity 1-9
3. `get_out_of_stock_products(user_id)` - Get products with quantity 0
4. `get_stock_status_summary(user_id)` - Get counts by stock status
5. `get_products_by_category(user_id, category_id)` - Filter products by category
6. `get_product_details(user_id, product_id)` - Get specific product details

## Stock Status Categories

- **In Stock**: Products with quantity >= 10
- **Low Stock**: Products with quantity between 1 and 9
- **Out of Stock**: Products with quantity = 0

## Response Format

All product tools return structured JSON responses with relevant product information including:
- Product ID
- Name
- Description (where applicable)
- Price
- Category
- Stock status
- Quantity
- SKU prefix
- Media (where applicable)

