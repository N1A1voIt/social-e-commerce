from typing import Optional, List
from sqlalchemy import and_

from post_generator.agent_core.engine import SessionLocal
from products.VariantCplAgent import VariantCplAgent, VariantCplAgentOut


class VariantCplAgentRepository:
    """
    Repository for accessing v_variant_cpl_agent view.
    This repository provides methods to retrieve variant data with stock information
    for the CPL agent.
    """

    def __init__(self):
        self.Session = SessionLocal

    def find_by_seller_with_stock(self, id_seller: int) -> List[VariantCplAgentOut]:
        """
        Get all variants for a specific seller that have stock (variant_number > 0).
        This is the main method used by the CPL agent to retrieve product variants
        that are available for promotion.

        Args:
            id_seller: The seller ID to filter by

        Returns:
            List of VariantCplAgentOut objects with stock > 0
        """
        session = self.Session()
        try:
            variants = session.query(VariantCplAgent).filter(
                and_(
                    VariantCplAgent.id_seller == id_seller,
                    VariantCplAgent.variant_number > 0
                )
            ).all()

            return [VariantCplAgentOut.model_validate(var) for var in variants]
        finally:
            session.close()

    def find_all_by_seller(self, id_seller: int) -> List[VariantCplAgentOut]:
        """
        Get all variants for a specific seller (including out of stock).

        Args:
            id_seller: The seller ID to filter by

        Returns:
            List of VariantCplAgentOut objects
        """
        session = self.Session()
        try:
            variants = session.query(VariantCplAgent).filter(
                VariantCplAgent.id_seller == id_seller
            ).all()

            return [VariantCplAgentOut.model_validate(var) for var in variants]
        finally:
            session.close()

    def find_by_id(self, variant_id: int) -> Optional[VariantCplAgentOut]:
        """
        Get a specific variant by ID with all information.

        Args:
            variant_id: The variant ID to search for

        Returns:
            VariantCplAgentOut object or None if not found
        """
        session = self.Session()
        try:
            variant = session.query(VariantCplAgent).filter(
                VariantCplAgent.id_variant == variant_id
            ).first()

            return VariantCplAgentOut.model_validate(variant) if variant else None
        finally:
            session.close()

    def find_by_category_with_stock(self, id_seller: int, id_category: int) -> List[VariantCplAgentOut]:
        """
        Get variants for a specific seller and category that have stock.

        Args:
            id_seller: The seller ID to filter by
            id_category: The category ID to filter by

        Returns:
            List of VariantCplAgentOut objects with stock > 0
        """
        session = self.Session()
        try:
            variants = session.query(VariantCplAgent).filter(
                and_(
                    VariantCplAgent.id_seller == id_seller,
                    VariantCplAgent.id_category == id_category,
                    VariantCplAgent.variant_number > 0
                )
            ).all()

            return [VariantCplAgentOut.model_validate(var) for var in variants]
        finally:
            session.close()

    def find_by_stock_status(self, id_seller: int, stock_status: str) -> List[VariantCplAgentOut]:
        """
        Get variants for a specific seller filtered by stock status.

        Args:
            id_seller: The seller ID to filter by
            stock_status: The stock status ('In Stock', 'Low Stock', or 'Out of Stock')

        Returns:
            List of VariantCplAgentOut objects
        """
        session = self.Session()
        try:
            variants = session.query(VariantCplAgent).filter(
                and_(
                    VariantCplAgent.id_seller == id_seller,
                    VariantCplAgent.stock_status == stock_status
                )
            ).all()

            return [VariantCplAgentOut.model_validate(var) for var in variants]
        finally:
            session.close()
