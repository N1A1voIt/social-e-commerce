from sqlalchemy.orm import Session

from prompt_parameter import PromptSaver


class PromptSaverRepository:
    """Repository class for handling PromptSaver database operations."""

    def __init__(self, db_session: Session):
        self.db_session = db_session

    def find_by_id_seller(self, id_seller: int):
        """
        Retrieve all PromptSaver records belonging to a specific seller.

        :param id_seller: ID of the seller.
        :return: List of PromptSaver objects.
        """
        return (
            self.db_session.query(PromptSaver)
            .filter(PromptSaver.id_seller == id_seller)
            .all()
        )

    def find_by_seller_and_platform(self, id_seller: int, id_platform: int):
        """
        Retrieve a single PromptSaver record by seller and platform.
        """
        return (
            self.db_session.query(PromptSaver)
            .filter(
                PromptSaver.id_seller == id_seller,
                PromptSaver.id_platform == id_platform
            )
            .first()
        )
