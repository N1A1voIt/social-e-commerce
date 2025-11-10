from sqlalchemy import Column, Integer, String, Text, Numeric, BigInteger
from sqlalchemy.ext.declarative import declarative_base
from pydantic import BaseModel
from typing import Optional
from decimal import Decimal

Base = declarative_base()


class ProductStockCpl(Base):
    """
    Entity for v_product_stock_cpl view.
    This view provides product information with stock details and status.
    """
    __tablename__ = 'v_product_stock_cpl'

    id_product = Column(BigInteger, primary_key=True)
    description = Column(Text, nullable=True)
    name = Column(Text, nullable=False)
    price = Column(Numeric(18, 2), nullable=False)
    media = Column(Text, nullable=True)
    id_seller = Column(BigInteger, nullable=False)
    id_category = Column(Integer, nullable=False)
    category = Column(String, nullable=True)
    product_number = Column(Integer, nullable=False)
    stock_status = Column(String, nullable=True)
    sku_prefix = Column(String, nullable=True)

    def is_in_stock(self):
        """Check if product is in stock"""
        return self.stock_status == 'In Stock'

    def is_low_stock(self):
        """Check if product has low stock"""
        return self.stock_status == 'Low Stock'

    def is_out_of_stock(self):
        """Check if product is out of stock"""
        return self.stock_status == 'Out of Stock'

    def get_formatted_price(self):
        """Get formatted price string"""
        return f"{self.price:.2f}" if self.price is not None else "0.00"


class ProductStockCplOut(BaseModel):
    """
    Pydantic model for ProductStockCpl output.
    """
    id_product: int
    description: Optional[str]
    name: str
    price: Decimal
    media: Optional[str]
    id_seller: int
    id_category: int
    category: Optional[str]
    product_number: int
    stock_status: Optional[str]
    sku_prefix: Optional[str]

    class Config:
        from_attributes = True

