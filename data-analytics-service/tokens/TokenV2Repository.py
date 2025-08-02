from typing import Optional

from post_generator.agent_core.engine import SessionLocal
from tokens.TokenV2 import TokenV2


class TokenV2Repository:
    def __init__(self):
        self.Session = SessionLocal

    def find_user_id_by_token(self,token: str) -> Optional[int]:
        found_token = self.Session.query(TokenV2).filter(TokenV2.token == token).first()
        if found_token:
            return found_token.user_id
        return None