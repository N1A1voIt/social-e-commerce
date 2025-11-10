from typing import Optional, List
from sqlalchemy import func

from post_generator.agent_core.engine import SessionLocal
from products.ProductStock import ProductStockCpl, ProductStockCplOut


class ProductStockRepository:
    """
    Repository for accessing v_product_stock_cpl view.
    This view provides product information with stock details and status.
    """

    def __init__(self):
        self.Session = SessionLocal

    def find_all(self) -> List[ProductStockCplOut]:
        """
        Get all products with stock information.

        Returns:
            List of ProductStockCplOut objects
        """
        session = self.Session()
        try:
            products = session.query(ProductStockCpl).all()
            return [ProductStockCplOut.model_validate(prod) for prod in products]
        finally:
            session.close()

    def find_by_id(self, product_id: int) -> Optional[ProductStockCplOut]:
        """
        Get a specific product by ID with stock information.

        Args:
            product_id: The product ID to search for

        Returns:
            ProductStockCplOut object or None if not found
        """
        session = self.Session()
        try:
            product = session.query(ProductStockCpl).filter(
                ProductStockCpl.id_product == product_id
            ).first()

            return ProductStockCplOut.model_validate(product) if product else None
        finally:
            session.close()

    def find_by_seller(self, seller_id: int) -> List[ProductStockCplOut]:
        """
        Get all products for a specific seller with stock information.

        Args:
            seller_id: The seller ID to filter by

        Returns:
            List of ProductStockCplOut objects
        """
        session = self.Session()
        try:
            products = session.query(ProductStockCpl).filter(
                ProductStockCpl.id_seller == seller_id
            ).all()

            return [ProductStockCplOut.model_validate(prod) for prod in products]
        finally:
            session.close()

    def find_by_category(self, category_id: int) -> List[ProductStockCplOut]:
        """
        Get all products in a specific category with stock information.

        Args:
            category_id: The category ID to filter by

        Returns:
            List of ProductStockCplOut objects
        """
        session = self.Session()
        try:
            products = session.query(ProductStockCpl).filter(
                ProductStockCpl.id_category == category_id
            ).all()

            return [ProductStockCplOut.model_validate(prod) for prod in products]
        finally:
            session.close()

    def find_by_stock_status(self, stock_status: str) -> List[ProductStockCplOut]:
        """
        Get all products with a specific stock status.

        Args:
            stock_status: The stock status to filter by ('In Stock', 'Low Stock', 'Out of Stock')

        Returns:
            List of ProductStockCplOut objects
        """
        session = self.Session()
        try:
            products = session.query(ProductStockCpl).filter(
                ProductStockCpl.stock_status == stock_status
            ).all()

            return [ProductStockCplOut.model_validate(prod) for prod in products]
        finally:
            session.close()

    def find_by_seller_and_categories(
        self,
        seller_id: int,
        category_ids: List[int]
    ) -> List[ProductStockCplOut]:
        """
        Get products for a specific seller filtered by categories with stock information.

        Args:
            seller_id: The seller ID to filter by
            category_ids: List of category IDs to filter by

        Returns:
            List of ProductStockCplOut objects
        """
        session = self.Session()
        try:
            products = session.query(ProductStockCpl).filter(
                (ProductStockCpl.id_seller == seller_id) &
                (ProductStockCpl.id_category.in_(category_ids))
            ).all()

            return [ProductStockCplOut.model_validate(prod) for prod in products]
        finally:
            session.close()

    def find_low_stock_by_seller(self, seller_id: int) -> List[ProductStockCplOut]:
        """
        Get all low stock products for a specific seller.

        Args:
            seller_id: The seller ID to filter by

        Returns:
            List of ProductStockCplOut objects with low stock
        """
        session = self.Session()
        try:
            products = session.query(ProductStockCpl).filter(
                (ProductStockCpl.id_seller == seller_id) &
                (ProductStockCpl.stock_status == 'Low Stock')
            ).all()

            return [ProductStockCplOut.model_validate(prod) for prod in products]
        finally:
            session.close()

    def find_out_of_stock_by_seller(self, seller_id: int) -> List[ProductStockCplOut]:
        """
        Get all out of stock products for a specific seller.

        Args:
            seller_id: The seller ID to filter by

        Returns:
            List of ProductStockCplOut objects that are out of stock
        """
        session = self.Session()
        try:
            products = session.query(ProductStockCpl).filter(
                (ProductStockCpl.id_seller == seller_id) &
                (ProductStockCpl.stock_status == 'Out of Stock')
            ).all()

            return [ProductStockCplOut.model_validate(prod) for prod in products]
        finally:
            session.close()

    def count_by_stock_status(self, seller_id: Optional[int] = None) -> dict:
        """
        Count products by stock status, optionally filtered by seller.

        Args:
            seller_id: Optional seller ID to filter by

        Returns:
            Dictionary with stock status as keys and counts as values
        """
        session = self.Session()
        try:
            query = session.query(
                ProductStockCpl.stock_status,
                func.count(ProductStockCpl.id_product)
            )

            if seller_id:
                query = query.filter(ProductStockCpl.id_seller == seller_id)

            results = query.group_by(ProductStockCpl.stock_status).all()

            return {status: count for status, count in results}
        finally:
            session.close()

