import json
import re
from datetime import datetime

from nlp_analyzer.db.order_repository import OrderRepository
from nlp_analyzer.db.sales_repository import SalesRepository
from products.ProductStockRepository import ProductStockRepository

def extract_sales_story(user_id: int):
    """Extract sales story/trends using actual sales data."""
    range = clean_and_parse_json("{ \"start_date\": \"1990-01-01\", \"end_date\": \"3000-01-01\" }")
    start_date = range.get("start_date", "1990-01-01")
    end_date = range.get("end_date", "3000-01-01")
    if isinstance(start_date, str):
        start_date = datetime.fromisoformat(start_date)
    if isinstance(end_date, str):
        end_date = datetime.fromisoformat(end_date)
    sales_repository = SalesRepository()
    sales_evolution = sales_repository.get_sales_evolution(start_date, end_date, user_id)
    return sales_evolution

def clean_and_parse_json(markdown_json_str):
    cleaned = re.sub(r"^```json\s*|\s*```$", "", markdown_json_str.strip(), flags=re.MULTILINE)
    return json.loads(cleaned)

def extract_sales_summary(user_id: int):
    """Extract sales summary using actual sales data."""
    range = clean_and_parse_json("{ \"start_date\": \"1990-01-01\", \"end_date\": \"3000-01-01\" }")
    start_date = range.get("start_date", "1990-01-01")
    end_date = range.get("end_date", "3000-01-01")
    if isinstance(start_date, str):
        start_date = datetime.fromisoformat(start_date)
    if isinstance(end_date, str):
        end_date = datetime.fromisoformat(end_date)

    print(f"start_date: {start_date} end_date: {end_date} and id: {user_id}")
    sales_repository = SalesRepository()
    sales_summary = sales_repository.get_sales_summary(start_date, end_date, user_id)
    print(sales_summary)
    return sales_summary


def extract_orders_summary(user_id: int):
    """Extract order-level summary (counts, status breakdown, top customers)."""
    range = clean_and_parse_json("{ \"start_date\": \"1990-01-01\", \"end_date\": \"3000-01-01\" }")
    start_date = range.get("start_date", "1990-01-01")
    end_date = range.get("end_date", "3000-01-01")
    if isinstance(start_date, str):
        start_date = datetime.fromisoformat(start_date)
    if isinstance(end_date, str):
        end_date = datetime.fromisoformat(end_date)

    repo = OrderRepository()
    orders = repo.get_orders_by_date_range(start_date, end_date, user_id)

    total_orders = len(orders)
    status_counts = {}
    customers = {}
    orders_by_day = {}

    for o in orders:
        status = o.d_status or "UNKNOWN"
        status_counts[status] = status_counts.get(status, 0) + 1

        cust = o.d_customer_name or f"customer_{o.id_customer or 'unknown'}"
        customers[cust] = customers.get(cust, {"orders": 0, "total_spent": 0.0})
        customers[cust]["orders"] += 1
        customers[cust]["total_spent"] += float(o.d_total or 0)

        date_key = str(o.created_at.date())
        orders_by_day[date_key] = orders_by_day.get(date_key, 0) + 1

    top_customers = sorted(
        [ {"customer": k, "orders": v["orders"], "total_spent": v["total_spent"]} for k, v in customers.items() ],
        key=lambda x: x["orders"],
        reverse=True
    )[:5]

    return {
        "total_orders": total_orders,
        "status_counts": status_counts,
        "top_customers": top_customers,
        "orders_by_day": orders_by_day
    }


def extract_orders_trends(user_id: int):
    """Return orders trends (daily order counts) using order repository aggregation."""
    range = clean_and_parse_json("{ \"start_date\": \"1990-01-01\", \"end_date\": \"3000-01-01\" }")
    start_date = range.get("start_date", "1990-01-01")
    end_date = range.get("end_date", "3000-01-01")
    if isinstance(start_date, str):
        start_date = datetime.fromisoformat(start_date)
    if isinstance(end_date, str):
        end_date = datetime.fromisoformat(end_date)

    repo = OrderRepository()
    # Reuse the sales evolution aggregation present in OrderRepository but focus on order_count
    evolution = repo.get_sales_evolution(start_date, end_date, user_id)
    # Map to simple date/order_count structure
    return [ {"date": e.date, "order_count": e.order_count} for e in evolution ]


# Product Stock Tools

def get_all_products_with_stock(user_id: int):
    """
    Get all products for a seller with their current stock information.

    Args:
        user_id: The seller's user ID

    Returns:
        List of products with stock details including stock status, quantity, and price
    """
    repo = ProductStockRepository()
    products = repo.find_by_seller(user_id)

    if not products:
        return {"message": "No products found for this seller", "products": []}

    return {
        "total_products": len(products),
        "products": [
            {
                "id": p.id_product,
                "name": p.name,
                "description": p.description,
                "price": float(p.price),
                "category": p.category,
                "stock_status": p.stock_status,
                "quantity": p.product_number,
                "sku": p.sku_prefix
            }
            for p in products
        ]
    }


def get_low_stock_products(user_id: int):
    """
    Get all products with low stock for a seller.
    Low stock means quantity is between 1 and 9.

    Args:
        user_id: The seller's user ID

    Returns:
        List of products with low stock that need restocking
    """
    repo = ProductStockRepository()
    products = repo.find_low_stock_by_seller(user_id)

    if not products:
        return {"message": "No low stock products found", "products": []}

    return {
        "total_low_stock": len(products),
        "products": [
            {
                "id": p.id_product,
                "name": p.name,
                "quantity": p.product_number,
                "price": float(p.price),
                "category": p.category,
                "sku": p.sku_prefix
            }
            for p in products
        ]
    }


def get_out_of_stock_products(user_id: int):
    """
    Get all products that are out of stock for a seller.
    Out of stock means quantity is 0.

    Args:
        user_id: The seller's user ID

    Returns:
        List of products that are completely out of stock
    """
    repo = ProductStockRepository()
    products = repo.find_out_of_stock_by_seller(user_id)

    if not products:
        return {"message": "No out of stock products", "products": []}

    return {
        "total_out_of_stock": len(products),
        "products": [
            {
                "id": p.id_product,
                "name": p.name,
                "price": float(p.price),
                "category": p.category,
                "sku": p.sku_prefix
            }
            for p in products
        ]
    }


def get_stock_status_summary(user_id: int):
    """
    Get a summary of products by stock status for a seller.
    Shows how many products are in stock, low stock, or out of stock.

    Args:
        user_id: The seller's user ID

    Returns:
        Dictionary with counts for each stock status category
    """
    repo = ProductStockRepository()
    counts = repo.count_by_stock_status(user_id)

    return {
        "in_stock": counts.get("In Stock", 0),
        "low_stock": counts.get("Low Stock", 0),
        "out_of_stock": counts.get("Out of Stock", 0),
        "total": sum(counts.values())
    }


def get_products_by_category(user_id: int, category_id: int):
    """
    Get all products in a specific category for a seller with stock information.

    Args:
        user_id: The seller's user ID
        category_id: The category ID to filter by

    Returns:
        List of products in the specified category with stock details
    """
    repo = ProductStockRepository()
    # First get all seller's products, then filter by category
    all_products = repo.find_by_seller(user_id)
    products = [p for p in all_products if p.id_category == category_id]

    if not products:
        return {"message": f"No products found in category {category_id}", "products": []}

    return {
        "category_id": category_id,
        "category_name": products[0].category if products else None,
        "total_products": len(products),
        "products": [
            {
                "id": p.id_product,
                "name": p.name,
                "price": float(p.price),
                "stock_status": p.stock_status,
                "quantity": p.product_number,
                "sku": p.sku_prefix
            }
            for p in products
        ]
    }


def get_product_details(user_id: int, product_id: int):
    """
    Get detailed information about a specific product including stock status.

    Args:
        user_id: The seller's user ID
        product_id: The product ID to retrieve

    Returns:
        Detailed information about the product
    """
    repo = ProductStockRepository()
    product = repo.find_by_id(product_id)

    if not product:
        return {"error": f"Product with ID {product_id} not found"}

    # Verify the product belongs to this seller
    if product.id_seller != user_id:
        return {"error": "Product does not belong to this seller"}

    return {
        "id": product.id_product,
        "name": product.name,
        "description": product.description,
        "price": float(product.price),
        "category_id": product.id_category,
        "category": product.category,
        "stock_status": product.stock_status,
        "quantity": product.product_number,
        "sku": product.sku_prefix,
        "media": product.media
    }

