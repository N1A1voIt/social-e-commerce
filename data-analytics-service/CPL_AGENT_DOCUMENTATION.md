# CPL Agent Implementation

## Overview

The CPL (Customer Product List) Agent is a flexible AI agent that processes user queries about product variants with available stock. Unlike the `post_generator` agent which focuses on categories, the CPL agent works directly with product variants that have stock > 0, providing more granular control over product-based queries.

## Architecture

### Database Layer

#### View: `v_variant_cpl_agent`

A comprehensive SQL view that combines variant, product, stock, and category information:

```sql
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
```

**Fields:**
- `id_variant`: Unique variant identifier
- `title`: Variant title/name
- `price`: Variant price
- `sku`: Variant SKU
- `sku_prefix`: Product SKU prefix
- `name`: Product name
- `id_seller`: Seller/user ID
- `category`: Category name
- `id_category`: Category ID
- `variant_number`: Current stock quantity
- `stock_status`: Calculated status ('In Stock', 'Low Stock', 'Out of Stock')

### Python Entities

#### `VariantCplAgent` (SQLAlchemy Model)

Located in: `products/VariantCplAgent.py`

Maps to the `v_variant_cpl_agent` view with helper methods:
- `is_in_stock()`: Check if variant has good stock
- `is_low_stock()`: Check if variant is running low
- `is_out_of_stock()`: Check if variant is out of stock
- `has_stock()`: Check if variant has any stock
- `get_formatted_price()`: Get formatted price string

#### `VariantCplAgentOut` (Pydantic Model)

Pydantic model for serialization/deserialization with all variant fields.

### Repository Layer

#### `VariantCplAgentRepository`

Located in: `products/VariantCplAgentRepository.py`

**Key Methods:**

- `find_by_seller_with_stock(id_seller: int)`: **Primary method** - Gets all variants for a seller where `variant_number > 0`
- `find_all_by_seller(id_seller: int)`: Gets all variants including out of stock
- `find_by_id(variant_id: int)`: Get specific variant by ID
- `find_by_category_with_stock(id_seller: int, id_category: int)`: Filter by category
- `find_by_stock_status(id_seller: int, stock_status: str)`: Filter by stock status

## Agent Architecture

### Main Agent: `CplAgent`

Located in: `cpl_agent/agent_core/agent.py`

The root agent orchestrates two sub-agents in sequence:

1. **Variant Extractor Agent** - Retrieves variants with stock
2. **Variant Formatter Agent** - Formats data based on user prompt

```
User Request → CplAgent → [1] Variant Extractor → [2] Variant Formatter → Response
                            (Get data)              (Format per prompt)
```

### Sub-Agents

#### 1. Variant Extractor Agent

Located in: `cpl_agent/agent_core/sub_agents/variant_extractor/agent.py`

**Tool:** `extract_variants(user_id: int)`

- Retrieves all variants with stock > 0 for the given user
- Returns JSON serialized list of variants
- Stores result in session state under key `extracted_variants`

#### 2. Variant Formatter Agent

Located in: `cpl_agent/agent_core/sub_agents/variant_extractor/agent.py`

**Purpose:** Format extracted variants according to user's request

**Capabilities:**
- Social media post generation
- Product listings
- JSON formatted data
- Inventory summaries
- Price comparisons
- Custom formatted outputs

The formatter is flexible and adapts to various user requests based on the provided variants data.

## API Endpoint

### `POST /cpl-agent`

**Authentication:** Required (Bearer token in Authorization header)

**Request Body:**
```json
{
  "query": "Your prompt/question here"
}
```

**Example Requests:**

1. **Promotional Content:**
```json
{
  "query": "Create a promotional post for all my products that are in stock"
}
```

2. **JSON Data:**
```json
{
  "query": "Give me a JSON list of all my products with stock, including name, SKU, price, and stock status"
}
```

3. **Category Filter:**
```json
{
  "query": "Show me all electronics products I have in stock with low stock warnings"
}
```

4. **Social Media:**
```json
{
  "query": "Create an Instagram post showcasing my top 5 products with the best stock levels"
}
```

**Response:**
Plain text or JSON (depending on user's query) containing the formatted output.

## Key Differences from Post Generator

| Feature | Post Generator | CPL Agent |
|---------|---------------|-----------|
| **Focus** | Category-based | Variant-based |
| **Data Source** | Products by category | Variants with stock |
| **Filtering** | Category IDs | Stock availability |
| **Flexibility** | Fixed social media format | Adapts to any prompt |
| **Use Case** | Social media posts | General product queries |
| **Sub-agents** | 4 (category extraction, DB query, product extraction, formatting) | 2 (variant extraction, formatting) |

## Usage Flow

1. **User Authentication**
   - Client sends request with bearer token
   - System validates token and retrieves `user_id`

2. **Session Creation**
   - New session created with `user_id` in state
   - Session ID generated for tracking

3. **Agent Execution**
   - **Step 1:** Variant Extractor retrieves all variants with stock > 0
   - **Step 2:** Variant Formatter processes user's prompt with the variant data

4. **Response**
   - Formatted output returned to client
   - Can be text, JSON, HTML, etc. based on request

## File Structure

```
data-analytics-service/
├── cpl_agent/
│   ├── __init__.py
│   └── agent_core/
│       ├── __init__.py
│       ├── agent.py                    # Main CplAgent class
│       └── sub_agents/
│           ├── __init__.py
│           └── variant_extractor/
│               ├── __init__.py
│               ├── agent.py            # Extractor & Formatter agents
│               └── prompt.py           # Prompt templates
├── products/
│   ├── VariantCplAgent.py             # Entity model
│   └── VariantCplAgentRepository.py   # Data access layer
├── main.py                             # API endpoint
└── test_cpl_agent.http                # Test requests

spring-service/ddl/
└── v_variant_cpl_agent.sql            # Database view
```

## Setup Instructions

### 1. Database Setup

Run the SQL view creation:
```bash
psql -h localhost -p 5437 -U postgres -d postgres -f spring-service/ddl/v_variant_cpl_agent.sql
```

### 2. Verify Installation

Check if view exists:
```sql
SELECT * FROM v_variant_cpl_agent LIMIT 5;
```

### 3. Test the Agent

Use the test file `test_cpl_agent.http` or:

```bash
curl -X POST http://localhost:8000/cpl-agent \
  -H "Content-Type: application/json" \
  -H "Authorization: YOUR_TOKEN" \
  -d '{"query": "List all my products in stock"}'
```

## Example Use Cases

1. **Inventory Management**
   - "Show me all products with low stock"
   - "What's my total inventory value?"

2. **Content Generation**
   - "Create a Facebook post for new arrivals"
   - "Generate promotional content for clearance items"

3. **Data Export**
   - "Export all products in JSON format"
   - "Create a CSV of products under $100"

4. **Business Intelligence**
   - "Which categories have the most stock?"
   - "Compare prices across my product line"

## Extension Points

The CPL agent is designed to be easily extensible:

1. **Add more sub-agents** - Insert additional processing steps
2. **Custom filters** - Extend repository with more query methods
3. **Output formats** - Add format-specific formatter agents
4. **Business logic** - Add helper methods to entity classes

## Troubleshooting

**No variants returned:**
- Check if user has products with stock > 0
- Verify `user_id` in token is correct
- Check database view has data

**Agent timeout:**
- Large inventories may take longer
- Consider adding pagination for large datasets

**Format issues:**
- Be specific in prompts about desired output format
- The formatter agent needs clear instructions

## Dependencies

- SQLAlchemy (database ORM)
- Pydantic (data validation)
- Google ADK (agent framework)
- FastAPI (web framework)

## Notes

- The agent only returns variants with `variant_number > 0` by default
- Stock status is calculated in the view based on quantity thresholds
- Session state maintains user context across sub-agent calls
- All database connections are properly closed after queries
