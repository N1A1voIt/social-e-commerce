from typing import Optional, List
from sqlalchemy import and_

from post_generator.agent_core.engine import SessionLocal
from products.Variant import VariantCpl, VariantCplOut


class VariantRepository:
    """
    Repository for accessing v_variant_cpl view.
    This view provides variant information with stock details and status.
    """

    def __init__(self):
        self.Session = SessionLocal

    def find_all(self) -> List[VariantCplOut]:
        """
        Get all variants with stock information.

        Returns:
            List of VariantCplOut objects
        """
        session = self.Session()
        try:
            variants = session.query(VariantCpl).all()
            return [VariantCplOut.model_validate(var) for var in variants]
        finally:
            session.close()

    def find_by_id(self, variant_id: int) -> Optional[VariantCplOut]:
        """
        Get a specific variant by ID with stock information.

        Args:
            variant_id: The variant ID to search for

        Returns:
            VariantCplOut object or None if not found
        """
        session = self.Session()
        try:
            variant = session.query(VariantCpl).filter(
                VariantCpl.id_variant == variant_id
            ).first()

            return VariantCplOut.model_validate(variant) if variant else None
        finally:
            session.close()

    def find_by_product_id(self, product_id: int) -> List[VariantCplOut]:
        """
        Get all variants for a specific product with stock information.

        Args:
            product_id: The product ID to filter by

        Returns:
            List of VariantCplOut objects
        """
        session = self.Session()
        try:
            variants = session.query(VariantCpl).filter(
                VariantCpl.id_product == product_id
            ).all()

            return [VariantCplOut.model_validate(var) for var in variants]
        finally:
            session.close()

    def find_by_sku(self, sku: str) -> List[VariantCplOut]:
        """
        Get variants by SKU with stock information.

        Args:
            sku: The SKU to search for (can be partial match)

        Returns:
            List of VariantCplOut objects
        """
        session = self.Session()
        try:
            variants = session.query(VariantCpl).filter(
                VariantCpl.sku.like(f"%{sku}%")
            ).all()

            return [VariantCplOut.model_validate(var) for var in variants]
        finally:
            session.close()

    def find_by_stock_status(self, stock_status: str) -> List[VariantCplOut]:
        """
        Get all variants with a specific stock status.

        Args:
            stock_status: The stock status to filter by ('In Stock', 'Low Stock', 'Out of Stock')

        Returns:
            List of VariantCplOut objects
        """
        session = self.Session()
        try:
            variants = session.query(VariantCpl).filter(
                VariantCpl.stock_status == stock_status
            ).all()

            return [VariantCplOut.model_validate(var) for var in variants]
        finally:
            session.close()

    def find_in_stock_by_product(self, product_id: int) -> List[VariantCplOut]:
        """
        Get all in-stock variants for a specific product.

        Args:
            product_id: The product ID to filter by

        Returns:
            List of VariantCplOut objects with stock
        """
        session = self.Session()
        try:
            variants = session.query(VariantCpl).filter(
                and_(
                    VariantCpl.id_product == product_id,
                    VariantCpl.stock_status == 'In Stock'
                )
            ).all()

            return [VariantCplOut.model_validate(var) for var in variants]
        finally:
            session.close()
