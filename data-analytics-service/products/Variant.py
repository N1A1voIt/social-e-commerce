from sqlalchemy import Column, Integer, String, Text, Numeric, BigInteger, DateTime
from sqlalchemy.ext.declarative import declarative_base
from pydantic import BaseModel
from typing import Optional
from decimal import Decimal
from datetime import datetime

Base = declarative_base()


class VariantCpl(Base):
    """
    Entity for v_variant_cpl view.
    This view provides variant information with stock details and status.
    """
    __tablename__ = 'v_variant_cpl'

    id_variant = Column(BigInteger, primary_key=True)
    title = Column(Text, nullable=True)
    price = Column(Numeric(18, 2), nullable=False)
    created_at = Column(DateTime(timezone=True))
    updated_at = Column(DateTime(timezone=True))
    id_product = Column(BigInteger, nullable=False)
    sku = Column(Text, nullable=True)
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

    def get_formatted_price(self):
        """Get formatted price string"""
        return f"{self.price:.2f}" if self.price is not None else "0.00"


class VariantCplOut(BaseModel):
    """
    Pydantic model for VariantCpl output.
    """
    id_variant: int
    title: Optional[str]
    price: Decimal
    created_at: datetime
    updated_at: datetime
    id_product: int
    sku: Optional[str]
    variant_number: Decimal
    stock_status: Optional[str]

    class Config:
        from_attributes = True
