from sqlalchemy import Column, Integer, String, Text, Numeric, BigInteger, DateTime
from sqlalchemy.ext.declarative import declarative_base
from pydantic import BaseModel
from typing import Optional
from decimal import Decimal
from datetime import datetime

Base = declarative_base()


class VariantCplAgent(Base):
    """
    Entity for v_variant_cpl_agent view.
    This view provides comprehensive variant information with stock details, 
    product information, and category data for the CPL agent.
    """
    __tablename__ = 'v_variant_cpl_agent'

    id_variant = Column(BigInteger, primary_key=True)
    title = Column(Text, nullable=True)
    price = Column(Numeric(18, 2), nullable=False)
    created_at = Column(DateTime(timezone=True))
    updated_at = Column(DateTime(timezone=True))
    id_product = Column(BigInteger, nullable=False)
    sku = Column(Text, nullable=True)
    sku_prefix = Column(Text, nullable=True)
    name = Column(Text, nullable=True)  # Product name
    id_seller = Column(BigInteger, nullable=False)
    media = Column(Text, nullable=True)  # Product media URL
    category = Column(Text, nullable=True)
    id_category = Column(BigInteger, nullable=True)
    variant_number = Column(Numeric, nullable=False)
    stock_status = Column(String, nullable=True)

    def is_in_stock(self):
        """Check if variant is in stock"""
        return self.stock_status == 'In Stock'

    def is_low_stock(self):
        """Check if variant has low stock"""
        return self.stock_status == 'Low Stock'

    def is_out_of_stock(self):
        """Check if variant is out of stock"""
        return self.stock_status == 'Out of Stock'
    
    def has_stock(self):
        """Check if variant has any stock (not out of stock)"""
        return self.variant_number > 0

    def get_formatted_price(self):
        """Get formatted price string"""
        return f"{self.price:.2f}" if self.price is not None else "0.00"


class VariantCplAgentOut(BaseModel):
    """
    Pydantic model for VariantCplAgent output.
    Includes comprehensive variant, product, and stock information.
    """
    id_variant: int
    title: Optional[str]
    price: Decimal
    created_at: datetime
    updated_at: datetime
    id_product: int
    sku: Optional[str]
    sku_prefix: Optional[str]
    name: Optional[str]  # Product name
    id_seller: int
    media: Optional[str]  # Product media URL
    category: Optional[str]
    id_category: Optional[int]
    variant_number: Decimal
    stock_status: Optional[str]

    class Config:
        from_attributes = True
