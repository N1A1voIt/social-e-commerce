from datetime import datetime
from typing import List, Dict, Any, Optional
from sqlalchemy import text
from post_generator.agent_core.engine import SessionLocal


class SalesRepository:
    def __init__(self):
        self.Session = SessionLocal

    def get_sales_summary(
            self,
            start_date: datetime,
            end_date: datetime,
            seller_id: int
    ) -> Dict[str, Any]:
        """
        Get sales summary for a seller within a date range.

        Args:
            start_date: Start date for the summary
            end_date: End date for the summary
            seller_id: The seller's ID

        Returns:
            Dictionary containing sales summary data
        """
        session = self.Session()
        try:
            query = text("""
            SELECT 
                COUNT(*) as total_sales,
                COALESCE(SUM(amount), 0) as total_revenue,
                COALESCE(AVG(amount), 0) as average_sale_amount,
                COALESCE(MIN(amount), 0) as min_sale_amount,
                COALESCE(MAX(amount), 0) as max_sale_amount,
                COUNT(DISTINCT id_order_m) as total_orders,
                COUNT(DISTINCT from_number) as unique_customers
            FROM sales 
            WHERE id_seller = :seller_id
            AND effectued_at BETWEEN :start_date AND :end_date
            """)
            
            result = session.execute(query, {
                'seller_id': seller_id,
                'start_date': start_date,
                'end_date': end_date
            }).mappings().first()
            
            if not result:
                return {}

            return {
                'total_sales': result['total_sales'],
                'total_revenue': float(result['total_revenue']),
                'average_sale_amount': float(result['average_sale_amount']),
                'min_sale_amount': float(result['min_sale_amount']),
                'max_sale_amount': float(result['max_sale_amount']),
                'total_orders': result['total_orders'],
                'unique_customers': result['unique_customers'],
                'period': {
                    'start_date': start_date.isoformat(),
                    'end_date': end_date.isoformat()
                }
            }
            
        except Exception as e:
            print(f"Error getting sales summary: {e}")
            return {}
        finally:
            session.close()

    def get_sales_evolution(
            self,
            start_date: datetime,
            end_date: datetime,
            seller_id: int
    ) -> List[Dict[str, Any]]:
        """
        Get sales evolution/trends for a seller within a date range.

        Args:
            start_date: Start date for the analysis
            end_date: End date for the analysis
            seller_id: The seller's ID

        Returns:
            List of dictionaries containing sales evolution data by month
        """
        session = self.Session()
        try:
            query = text("""
            SELECT 
                DATE_TRUNC('month', effectued_at) as month,
                COUNT(*) as sales_count,
                COALESCE(SUM(amount), 0) as monthly_revenue,
                COALESCE(AVG(amount), 0) as avg_sale_amount,
                COUNT(DISTINCT id_order_m) as orders_count,
                COUNT(DISTINCT from_number) as unique_customers
            FROM sales 
            WHERE id_seller = :seller_id
            AND effectued_at BETWEEN :start_date AND :end_date
            GROUP BY DATE_TRUNC('month', effectued_at)
            ORDER BY month ASC
            """)
            
            results = session.execute(query, {
                'seller_id': seller_id,
                'start_date': start_date,
                'end_date': end_date
            }).mappings().all()
            
            evolution_data = []
            for row in results:
                evolution_data.append({
                    'month': row['month'].isoformat(),
                    'sales_count': row['sales_count'],
                    'monthly_revenue': float(row['monthly_revenue']),
                    'avg_sale_amount': float(row['avg_sale_amount']),
                    'orders_count': row['orders_count'],
                    'unique_customers': row['unique_customers']
                })
            
            return evolution_data
            
        except Exception as e:
            print(f"Error getting sales evolution: {e}")
            return []
        finally:
            session.close()

    def get_top_selling_products(
            self,
            start_date: datetime,
            end_date: datetime,
            seller_id: int,
            limit: int = 10
    ) -> List[Dict[str, Any]]:
        """
        Get top selling products for a seller within a date range.

        Args:
            start_date: Start date for the analysis
            end_date: End date for the analysis
            seller_id: The seller's ID
            limit: Maximum number of products to return

        Returns:
            List of dictionaries containing top selling products data
        """
        session = self.Session()
        try:
            query = text("""
            SELECT 
                id_pc as product_id,
                COUNT(*) as sales_count,
                COALESCE(SUM(amount), 0) as total_revenue
            FROM sales 
            WHERE id_seller = :seller_id
            AND effectued_at BETWEEN :start_date AND :end_date
            GROUP BY id_pc
            ORDER BY sales_count DESC, total_revenue DESC
            LIMIT :limit
            """)
            
            results = session.execute(query, {
                'seller_id': seller_id,
                'start_date': start_date,
                'end_date': end_date,
                'limit': limit
            }).mappings().all()
            
            products_data = []
            for row in results:
                products_data.append({
                    'product_id': row['product_id'],
                    'sales_count': row['sales_count'],
                    'total_revenue': float(row['total_revenue'])
                })

            return products_data
            
        except Exception as e:
            print(f"Error getting top selling products: {e}")
            return []
        finally:
            session.close()

    def get_customer_insights(
            self,
            start_date: datetime,
            end_date: datetime,
            seller_id: int
    ) -> Dict[str, Any]:
        """
        Get customer insights for a seller within a date range.

        Args:
            start_date: Start date for the analysis
            end_date: End date for the analysis
            seller_id: The seller's ID

        Returns:
            Dictionary containing customer insights data
        """
        session = self.Session()
        try:
            # Get top customers by revenue
            query = text("""
            SELECT 
                from_number,
                from_name,
                COUNT(*) as total_purchases,
                COALESCE(SUM(amount), 0) as total_spent,
                COALESCE(AVG(amount), 0) as avg_purchase_amount
            FROM sales 
            WHERE id_seller = :seller_id
            AND effectued_at BETWEEN :start_date AND :end_date
            GROUP BY from_number, from_name
            ORDER BY total_spent DESC
            LIMIT 10
            """)
            
            top_customers = session.execute(query, {
                'seller_id': seller_id,
                'start_date': start_date,
                'end_date': end_date
            }).mappings().all()
            
            return {
                'top_customers': [
                    {
                        'customer_number': row['from_number'],
                        'customer_name': row['from_name'],
                        'total_purchases': row['total_purchases'],
                        'total_spent': float(row['total_spent']),
                        'avg_purchase_amount': float(row['avg_purchase_amount'])
                    }
                    for row in top_customers
                ]
            }
            
        except Exception as e:
            print(f"Error getting customer insights: {e}")
            return {}
        finally:
            session.close()