# engine.py
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

DB_URL = "postgresql://postgres:postgres@localhost:5437/postgres"
engine = create_engine(DB_URL)
SessionLocal = sessionmaker(bind=engine)


