import datetime
from tokenize import String

from sqlalchemy import Column, Integer, DateTime, Text
from sqlalchemy.orm import declarative_base

Base = declarative_base()
class TokenV2(Base):
    """
    SQLAlchemy model for the 'tokens_v2' table.
    This class handles the mapping of Python objects to database rows.
    """
    # Maps to @Table(name = "tokens_v2")
    __tablename__ = 'tokens_v2'

    # Maps to @Id and @GeneratedValue
    # The 'primary_key=True' automatically sets it as the primary key.
    # The 'autoincrement=True' is the default for Integer primary keys.
    id = Column("id_token", Integer, primary_key=True)

    # Maps to @Column(name = "token", nullable = false, unique = true, length = 2000)
    # String(2000) handles the length constraint, 'unique=True' and 'nullable=False'
    # correspond directly to the Java annotations.
    token = Column("token", Text, unique=True, nullable=False)

    # Maps to @Column(name = "id_seller", nullable = false)
    # The database column is 'id_seller', but the Python attribute is 'user_id' for clarity.
    user_id = Column("id_seller", Integer, nullable=False)

    # Maps to @Column(name = "expired_at", nullable = false)
    # SQLAlchemy's DateTime type is the equivalent of Java's LocalDateTime.
    expiry_date = Column("expired_at", DateTime, nullable=False)

