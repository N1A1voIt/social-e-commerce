from typing import List, Optional
from datetime import datetime
from sqlalchemy import func, cast, Date, Integer
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
            # Build query to get daily aggregated sales
            query = session.query(
                cast(OrderMother.created_at, Date).label('date'),
                func.sum(OrderMother.d_total).label('total_sales'),
                func.count(OrderMother.id_order_m).label('order_count')
            ).filter(
                OrderMother.created_at >= start_date,
                OrderMother.created_at <= end_date,
                cast(OrderMother.d_status, Integer) > status_threshold
            )

            # If user_id is provided, filter by seller through managed_pages
            if user_id is not None:
                # Assuming managed_pages has id_seller field
                # You may need to adjust this join based on your actual schema
                query = query.filter(OrderMother.id_managed_pages == user_id)

            # Group by date and order by date
            query = query.group_by(cast(OrderMother.created_at, Date)).order_by('date')

            results = query.all()

            # Convert results to SalesEvolution objects
            sales_evolution = [
                SalesEvolution(
                    date=str(row.date),
                    total_sales=float(row.total_sales or 0),
                    order_count=int(row.order_count)
                )
                for row in results
            ]

            return sales_evolution

        finally:
            session.close()

    def get_sales_summary(
        self,
        start_date: datetime,
        end_date: datetime,
        user_id: Optional[int] = None,
        status_threshold: int = 25
    ) -> dict:
        """
        Get sales summary between two dates.

        Returns:
            Dictionary with total_sales, total_orders, average_order_value
        """
        session = self.Session()
        try:
            query = session.query(
                func.sum(OrderMother.d_total).label('total_sales'),
                func.count(OrderMother.id_order_m).label('total_orders'),
                func.avg(OrderMother.d_total).label('avg_order_value')
            ).filter(
                OrderMother.created_at >= start_date,
                OrderMother.created_at <= end_date,
                cast(OrderMother.d_status, Integer) > status_threshold
            )

            if user_id is not None:
                query = query.filter(OrderMother.id_managed_pages == user_id)

            result = query.one()

            return {
                'total_sales': float(result.total_sales or 0),
                'total_orders': int(result.total_orders or 0),
                'average_order_value': float(result.avg_order_value or 0)
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
