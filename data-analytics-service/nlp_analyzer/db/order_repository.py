from typing import List, Optional
from datetime import datetime
from sqlalchemy import func, cast, Date, Integer, text
from post_generator.agent_core.engine import SessionLocal
from nlp_analyzer.db.Order import OrderMother, OrderMotherOut, SalesEvolution


class OrderRepository:
    """
    Repository for order_mother table operations
    """

    def __init__(self):
        self.Session = SessionLocal

    def get_sales_evolution(
        self,
        start_date: datetime,
        end_date: datetime,
        user_id: Optional[int] = None,
        status_threshold: int = 25
    ) -> List[SalesEvolution]:
        """
        Get sales evolution between two dates for orders with status > threshold.

        Sales are grouped by date and include:
        - Total sales amount per day
        - Number of orders per day

        Args:
            start_date: Start date for the period
            end_date: End date for the period
            user_id: Optional seller ID to filter by specific seller
            status_threshold: Minimum status value (default: 25)

        Returns:
            List of SalesEvolution objects containing date, total_sales, and order_count
        """
        session = self.Session()
        try:
            # Query the view v_order_mother_cpl and aggregate per day
            sql = """
            SELECT
                CAST(created_at AS DATE) AS date,
                SUM(d_total) AS total_sales,
                COUNT(id_order_m) AS order_count
            FROM v_order_mother_cpl
            WHERE created_at >= :start_date
              AND created_at <= :end_date
              AND CAST(d_status AS INTEGER) > :status_threshold
            """

            params = {"start_date": start_date, "end_date": end_date, "status_threshold": status_threshold}
            if user_id is not None:
                # keep same user filtering key used in get_sales_summary (id_seller)
                sql += "\n AND id_seller = :user_id"
                params["user_id"] = user_id

            sql += "\n GROUP BY CAST(created_at AS DATE) ORDER BY date"

            rows = session.execute(text(sql), params).mappings().all()

            sales_evolution = [
                SalesEvolution(
                    date=str(row['date']),
                    total_sales=float(row['total_sales'] or 0),
                    order_count=int(row['order_count'] or 0)
                )
                for row in rows
            ]

            return sales_evolution

        finally:
            session.close()

    def get_sales_summary(
        self,
        start_date: datetime,
        end_date: datetime,
        user_id: Optional[int] = None,
        status_threshold: int = 11
    ) -> dict:
        """
        Get sales summary between two dates.

        Uses the materialized view / view `v_order_mother_cpl` instead of directly
        querying the `order_mother` table so we can leverage joined data from
        `managed_pages` (id_sp, page_title, etc.).

        Returns:
            Dictionary with total_sales, total_orders, average_order_value
        """
        session = self.Session()
        try:
            sql = """
            SELECT
                SUM(d_total) AS total_sales,
                COUNT(id_order_m) AS total_orders,
                AVG(d_total) AS avg_order_value
            FROM v_order_mother_cpl
            WHERE created_at >= :start_date
              AND created_at <= :end_date
              AND CAST(d_status AS INTEGER) > :status_threshold
            """

            params = {"start_date": start_date, "end_date": end_date, "status_threshold": status_threshold}
            if user_id is not None:
                sql += "\n AND id_seller = :user_id"
                params["user_id"] = user_id

            row = session.execute(text(sql), params).mappings().one()

            return {
                'total_sales': float(row['total_sales'] or 0),
                'total_orders': int(row['total_orders'] or 0),
                'average_order_value': float(row['avg_order_value'] or 0)
            }

        finally:
            session.close()

    def get_orders_by_date_range(
        self,
        start_date: datetime,
        end_date: datetime,
        user_id: Optional[int] = None,
        status_threshold: int = 25
    ) -> List[OrderMotherOut]:
        """
        Get all orders within a date range with status > threshold.

        Args:
            start_date: Start date for the period
            end_date: End date for the period
            user_id: Optional seller ID to filter by specific seller
            status_threshold: Minimum status value (default: 25)

        Returns:
            List of OrderMotherOut objects
        """
        session = self.Session()
        try:
            query = session.query(OrderMother).filter(
                OrderMother.created_at >= start_date,
                OrderMother.created_at <= end_date,
                cast(OrderMother.d_status, Integer) > status_threshold
            )

            if user_id is not None:
                query = query.filter(OrderMother.id_managed_pages == user_id)

            orders = query.order_by(OrderMother.created_at).all()

            # Convert to Pydantic models
            return [OrderMotherOut.from_orm(order) for order in orders]

        finally:
            session.close()

    def get_order_by_id(self, order_id: int) -> Optional[OrderMotherOut]:
        """
        Get a single order by ID.

        Args:
            order_id: The order ID

        Returns:
            OrderMotherOut object or None if not found
        """
        session = self.Session()
        try:
            order = session.query(OrderMother).filter(
                OrderMother.id_order_m == order_id
            ).first()

            if order:
                return OrderMotherOut.from_orm(order)
            return None

        finally:
            session.close()
