from sqlalchemy import Column, Integer, String, Text, Numeric, BigInteger, DateTime, func
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import validates

Base = declarative_base()


class Product(Base):
    __tablename__ = 'products_v2'

    id_product = Column(BigInteger, primary_key=True, autoincrement=True)

    # Product name - required
    name = Column(Text, nullable=False)

    # Product description - optional
    description = Column(Text, nullable=True)

    # Product price - required
    price = Column(Numeric(18, 2), nullable=False)

    # Media URLs or identifiers
    media = Column(Text, nullable=True)

    # Foreign key to seller_v2 (manual control)
    id_seller = Column(BigInteger, nullable=False)

    # Foreign key to category
    id_category = Column(Integer, nullable=False)

    # Audit fields
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())

    def has_description(self):
        return bool(self.description and self.description.strip())

    def has_media(self):
        return bool(self.media and self.media.strip())

    def get_formatted_price(self):
        return f"{self.price:.2f}" if self.price is not None else "0.00"
