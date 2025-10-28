from sqlalchemy.orm import Session

from post_generator.agent_core.engine import SessionLocal
from prompt_parameter.PromptSaverView import PromptSaverView


class PromptSaverViewRepository:
    def __init__(self):
        self.db_session = SessionLocal

    def find_by_id_seller(self, id_seller: int):
        """
        Retrieve all prompt view records for a given seller.
        """
        session = self.db_session()
        return (
            session.query(PromptSaverView)
            .filter(PromptSaverView.id_seller == id_seller)
            .all()
        )
