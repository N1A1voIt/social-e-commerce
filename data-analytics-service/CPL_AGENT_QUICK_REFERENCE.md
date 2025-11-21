# CPL Agent Quick Reference

## Quick Start

### 1. Create the database view
```bash
psql -h localhost -p 5437 -U postgres -d postgres -f spring-service/ddl/v_variant_cpl_agent.sql
```

### 2. Test the endpoint
```bash
curl -X POST http://localhost:8000/cpl-agent \
  -H "Content-Type: application/json" \
  -H "Authorization: YOUR_TOKEN" \
  -d '{"query": "List all my products in stock"}'
```

## API Reference

### Endpoint
`POST /cpl-agent`

### Headers
- `Content-Type: application/json`
- `Authorization: <your-token>`

### Request Body
```json
{
  "query": "Your question or prompt here"
}
```

### Response
Text or JSON based on your query

## Common Queries

### Get product list
```json
{"query": "List all my products that are in stock"}
```

### JSON output
```json
{"query": "Give me JSON of all products with name, SKU, price, and stock"}
```

### Low stock alert
```json
{"query": "Show products with low stock"}
```

### Social media post
```json
{"query": "Create an Instagram post for my best-selling products"}
```

### Category filter
```json
{"query": "Show all electronics in stock"}
```

### Price filter
```json
{"query": "List products under $100 with good stock"}
```

## Key Components

### Database View
**File:** `spring-service/ddl/v_variant_cpl_agent.sql`
- Combines variants, products, stock, and categories
- Calculates stock status automatically

### Entity Model
**File:** `data-analytics-service/products/VariantCplAgent.py`
- `VariantCplAgent`: SQLAlchemy model
- `VariantCplAgentOut`: Pydantic model

### Repository
**File:** `data-analytics-service/products/VariantCplAgentRepository.py`
- `find_by_seller_with_stock(user_id)`: Main method (stock > 0 only)

### Agent
**File:** `data-analytics-service/cpl_agent/agent_core/agent.py`
- `CplAgent`: Main orchestrator
- Sub-agents: Variant Extractor → Variant Formatter

### API
**File:** `data-analytics-service/main.py`
- `/cpl-agent` endpoint with authentication

## Architecture Flow

```
User Request
    ↓
POST /cpl-agent (with auth token)
    ↓
CplAgent orchestrator
    ↓
[1] Variant Extractor Agent
    - Calls: find_by_seller_with_stock(user_id)
    - Returns: All variants with stock > 0
    ↓
[2] Variant Formatter Agent
    - Receives: User prompt + variant data
    - Returns: Formatted response per user's request
    ↓
Response to user
```

## Key Differences: CPL vs Post Generator

| CPL Agent | Post Generator |
|-----------|----------------|
| Works with variants | Works with products |
| Filters by stock | Filters by category |
| Flexible output | Social media focused |
| 2 sub-agents | 4 sub-agents |
| Any prompt format | Fixed format |

## Repository Methods

```python
# Main method - get variants with stock
variants = repo.find_by_seller_with_stock(user_id)

# All variants (including out of stock)
variants = repo.find_all_by_seller(user_id)

# By category with stock
variants = repo.find_by_category_with_stock(user_id, category_id)

# By stock status
variants = repo.find_by_stock_status(user_id, 'Low Stock')

# Single variant
variant = repo.find_by_id(variant_id)
```

## Stock Status Values

- **In Stock**: `variant_number >= 10`
- **Low Stock**: `0 < variant_number < 10`
- **Out of Stock**: `variant_number = 0`

## Testing

Use the test file: `test_cpl_agent.http`

Or with curl:
```bash
# Replace YOUR_TOKEN with actual token
curl -X POST http://localhost:8000/cpl-agent \
  -H "Content-Type: application/json" \
  -H "Authorization: YOUR_TOKEN" \
  -d '{"query": "Show me all products in stock with prices"}'
```

## Tips

1. **Be specific in prompts** - The agent adapts to your request
2. **Mention output format** - JSON, text, social media, etc.
3. **Use filters** - Category, price, stock status in your query
4. **Stock focus** - Agent only returns products with stock > 0

## Error Handling

- **401 Unauthorized**: Check your authorization token
- **No results**: User may have no products with stock
- **Timeout**: Large datasets may take time

## File Locations

```
📁 spring-service/ddl/
  └── v_variant_cpl_agent.sql

📁 data-analytics-service/
  ├── products/
  │   ├── VariantCplAgent.py
  │   └── VariantCplAgentRepository.py
  ├── cpl_agent/
  │   └── agent_core/
  │       ├── agent.py
  │       └── sub_agents/variant_extractor/agent.py
  ├── main.py (API endpoint)
  ├── test_cpl_agent.http
  └── CPL_AGENT_DOCUMENTATION.md
```

## Session State Variables

- `u_output`: User ID (seller ID)
- `extracted_variants`: JSON list of variants with stock
- `formatted_output`: Final formatted response

## Next Steps

1. Run the SQL view creation
2. Test with simple queries
3. Experiment with different prompt formats
4. Integrate into your application
5. Monitor performance with large datasets

## Support

For detailed information, see `CPL_AGENT_DOCUMENTATION.md`
