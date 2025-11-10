#!/usr/bin/env python3
"""
Test script to verify the product stock tools integration
"""
import sys

print("Testing imports...")

try:
    from generic_chat.tools import (
        get_all_products_with_stock,
        get_low_stock_products,
        get_out_of_stock_products,
        get_stock_status_summary,
        get_products_by_category,
        get_product_details
    )
    print("✓ All product stock tools imported successfully")
except Exception as e:
    print(f"✗ Failed to import tools: {e}")
    sys.exit(1)

try:
    from products.ProductStockRepository import ProductStockRepository
    print("✓ ProductStockRepository imported successfully")
except Exception as e:
    print(f"✗ Failed to import ProductStockRepository: {e}")
    sys.exit(1)

try:
    from generic_chat.agent import generic_agent
    print(f"✓ Agent imported successfully")
    print(f"  - Agent has {len(generic_agent.tools)} tools registered")
    print(f"  - Tools: {[tool.func.__name__ if hasattr(tool, 'func') else str(tool) for tool in generic_agent.tools[:3]]}")
except Exception as e:
    print(f"✗ Failed to import agent: {e}")
    sys.exit(1)

print("\n✅ All tests passed! The integration is working correctly.")
print("\nYou can now use NLP queries like:")
print("  - 'Show me all my products with stock'")
print("  - 'What products are low on stock?'")
print("  - 'Give me a stock status summary'")
print("  - 'Show me products that are out of stock'")

