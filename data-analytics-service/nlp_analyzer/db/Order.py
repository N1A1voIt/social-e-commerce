from sqlalchemy import Column, Integer, String, Text, TIMESTAMP, Numeric, ForeignKey
from sqlalchemy.ext.declarative import declarative_base
from pydantic import BaseModel
from datetime import datetime
from typing import Optional

Base = declarative_base()


class OrderMother(Base):
    """
    SQLAlchemy model for order_mother table
    """
    __tablename__ = 'order_mother'

    id_order_m = Column(Integer, primary_key=True, autoincrement=True)
    description = Column(Text, nullable=True)
    created_at = Column(TIMESTAMP, nullable=False)
    d_total = Column(Numeric(18, 2), nullable=True)
    d_customer_name = Column(Text, nullable=True)
    d_status = Column(String(50), nullable=True)
    shipping_address = Column(Text, nullable=True)
    customer_number = Column(String(50), nullable=True)
    id_pc = Column(Text, nullable=False)
    id_cart = Column(Integer, ForeignKey('cart.id_cart'), nullable=True)
    id_customer = Column(Integer, ForeignKey('customer.id_customer'), nullable=True)
    id_managed_pages = Column(Integer, ForeignKey('managed_pages.id_mp'), nullable=False)


class OrderMotherOut(BaseModel):
    """
    Pydantic model for output
    """
    id_order_m: int
    description: Optional[str] = None
    created_at: datetime
    d_total: Optional[float] = None
    d_customer_name: Optional[str] = None
    d_status: Optional[str] = None
    shipping_address: Optional[str] = None
    customer_number: Optional[str] = None
    id_pc: str
    id_cart: Optional[int] = None
    id_customer: Optional[int] = None
    id_managed_pages: int

    class Config:
        from_attributes = True


class SalesEvolution(BaseModel):
    """
    Model for sales evolution data point
    """
    date: str
    total_sales: float
    order_count: int

