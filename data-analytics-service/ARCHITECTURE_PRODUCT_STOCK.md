# Product Stock Integration Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         User Request                             │
│              "Show me my low stock products"                     │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    FastAPI Endpoint                              │
│                POST /generic-chat                                │
│                  { query, user_id }                              │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Generic Chat Agent                             │
│                  (LlmAgent - Gemini 2.0)                        │
│                                                                  │
│  ┌────────────────────────────────────────────────────────┐   │
│  │              Tool Selection (NLP)                       │   │
│  │  Analyzes query and selects appropriate tool           │   │
│  └────────────────────────────────────────────────────────┘   │
│                                                                  │
│  Available Tools (8):                                           │
│  ┌─────────────────────┬──────────────────────────────┐       │
│  │  Sales Tools (2)    │  Product Tools (6)           │       │
│  │  ─────────────────  │  ──────────────────────────  │       │
│  │  • sales_summary    │  • all_products_with_stock   │       │
│  │  • sales_story      │  • low_stock_products        │       │
│  │                     │  • out_of_stock_products     │       │
│  │                     │  • stock_status_summary      │       │
│  │                     │  • products_by_category      │       │
│  │                     │  • product_details           │       │
│  └─────────────────────┴──────────────────────────────┘       │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Tool Functions Layer                          │
│                  (generic_chat/tools.py)                        │
│                                                                  │
│  get_low_stock_products(user_id)                                │
│         │                                                        │
│         ▼                                                        │
│  ┌──────────────────────────────────────────────────┐          │
│  │  Creates ProductStockRepository instance          │          │
│  │  Calls: repo.find_low_stock_by_seller(user_id)   │          │
│  │  Formats response as JSON                         │          │
│  └──────────────────────────────────────────────────┘          │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Repository Layer                                │
│           (products/ProductStockRepository.py)                   │
│                                                                  │
│  Methods:                                                        │
│  • find_all()                                                    │
│  • find_by_id(product_id)                                       │
│  • find_by_seller(seller_id)                                    │
│  • find_by_category(category_id)                                │
│  • find_by_stock_status(status)                                 │
│  • find_by_seller_and_categories(seller_id, category_ids)       │
│  • find_low_stock_by_seller(seller_id)     ← CALLED            │
│  • find_out_of_stock_by_seller(seller_id)                       │
│  • count_by_stock_status(seller_id)                             │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Entity Layer                                  │
│              (products/ProductStock.py)                         │
│                                                                  │
│  ProductStockCpl (SQLAlchemy Model)                             │
│  • Maps to v_product_stock_cpl view                             │
│  • Provides helper methods                                      │
│                                                                  │
│  ProductStockCplOut (Pydantic Model)                            │
│  • Serialization/validation                                     │
│  • JSON output format                                           │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Database View                                 │
│                 v_product_stock_cpl                             │
│                                                                  │
│  SELECT p.id_product, name, price, description,                 │
│         c.val as category, product_number,                      │
│         CASE                                                     │
│           WHEN qty = 0 THEN 'Out of Stock'                      │
│           WHEN qty >= 10 THEN 'In Stock'                        │
│           WHEN qty BETWEEN 1 AND 9 THEN 'Low Stock'             │
│         END as stock_status, ...                                │
│  FROM products_v2 p                                             │
│  LEFT JOIN stock_details s ON p.id_product = s.id_product       │
│  JOIN category c ON c.id_category = p.id_category               │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    PostgreSQL Database                           │
│                                                                  │
│  Tables:                                                         │
│  • products_v2                                                   │
│  • stocks_child                                                  │
│  • category                                                      │
│  • managed_pages                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow Example

**Query**: "Show me my low stock products"

1. **User** → POST request to `/generic-chat` with query
2. **FastAPI** → Passes to generic_agent
3. **LlmAgent** → Analyzes NLP, selects `get_low_stock_products` tool
4. **Tool Function** → Calls `ProductStockRepository.find_low_stock_by_seller(user_id)`
5. **Repository** → Queries `ProductStockCpl` entity
6. **SQLAlchemy** → Executes SELECT on `v_product_stock_cpl` view
7. **PostgreSQL** → Returns rows where seller=user_id AND stock_status='Low Stock'
8. **Repository** → Converts to `ProductStockCplOut` Pydantic models
9. **Tool Function** → Formats as JSON response
10. **Agent** → Returns response
11. **FastAPI** → Sends JSON to user

## Key Components

- **Agent**: Gemini 2.0 Flash LLM for NLP understanding
- **Tools**: 8 callable functions (2 sales + 6 product)
- **Repository**: Data access with 9 query methods
- **Entity**: ORM mapping to database view
- **View**: Pre-computed stock status logic

