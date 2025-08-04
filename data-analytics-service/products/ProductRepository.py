from typing import Optional, List

from post_generator.agent_core.engine import SessionLocal
from products.Product import Product, ProductOut

from tokens.TokenV2Repository import TokenV2Repository


class ProductRepository:
    def __init__(self):
        self.Session = SessionLocal

    def find_by_token_and_categories(self, user_id: int, list_category: list) -> Optional[List[ProductOut]]:
        # token_repo = TokenV2Repository()
        # user_id = token_repo.find_user_id_by_token(token)
        print("UID:"+str(user_id))
        if not user_id:
            return []

        session = self.Session()
        try:
            products = session.query(Product).filter(
                (Product.id_seller == user_id) &
                (Product.id_category.in_(list_category))
            ).all()

            # Convert to Pydantic before returning
            pydantic_products = [ProductOut.from_orm(prod) for prod in products]
            return pydantic_products

        finally:
            session.close()
