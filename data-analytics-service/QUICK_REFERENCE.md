# Quick Reference - Product Stock NLP Queries

## Test the Integration

```bash
python test_product_stock_integration.py
```

## Common NLP Queries

| What You Want | Example Query |
|---------------|---------------|
| All products | "Show me all my products" |
| Low stock items | "What products are running low?" |
| Out of stock | "What's out of stock?" |
| Stock summary | "Give me a stock overview" |
| By category | "Show products in category 5" |
| Specific product | "Show me product 123" |
| Sales summary | "What are my total sales?" |
| Sales trends | "Show me sales trends" |

## API Endpoint

```
POST http://localhost:8000/generic-chat
Content-Type: application/json

{
  "query": "your natural language query here",
  "user_id": 123
}
```

## Tool Functions (8 Total)

### Sales (2)
- `extract_sales_summary`
- `extract_sales_story`

### Products (6)
- `get_all_products_with_stock`
- `get_low_stock_products`
- `get_out_of_stock_products`
- `get_stock_status_summary`
- `get_products_by_category`
- `get_product_details`

## Stock Status Categories

- 🟢 **In Stock**: qty >= 10
- 🟡 **Low Stock**: 1 <= qty <= 9
- 🔴 **Out of Stock**: qty = 0

## Files Modified

1. `generic_chat/tools.py` - Added 6 product tools
2. `generic_chat/agent.py` - Registered tools with agent
3. `products/ProductStock.py` - Entity for view
4. `products/ProductStockRepository.py` - Data access layer

