from sqlalchemy import Column, Integer, Text, DateTime, ForeignKey, func, UniqueConstraint
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import validates

Base = declarative_base()

class PromptSaver(Base):
    __tablename__ = 'prompt_saver'

    id_prompt = Column(Integer, primary_key=True, autoincrement=True)

    # The actual prompt text - required
    prompt = Column(Text, nullable=False)

    # Foreign key to seller_v2
    id_seller = Column(Integer, ForeignKey('seller_v2.id_seller'), nullable=False)

    # Foreign key to supported_platforms_v2
    id_platform = Column(Integer, ForeignKey('supported_platforms_v2.id_sp'), nullable=False)

    # Timestamp when the record was created
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)

    # Ensure uniqueness of (id_seller, id_platform)
    __table_args__ = (
        UniqueConstraint('id_seller', 'id_platform', name='uq_prompt_saver_seller_platform'),
    )

    # --- Utility methods ---

    def has_prompt(self):
        """Return True if the prompt text is non-empty."""
        return bool(self.prompt and self.prompt.strip())

    @validates('prompt')
    def validate_prompt(self, key, value):
        """Ensure prompt is not empty."""
        if not value or not value.strip():
            raise ValueError("Prompt cannot be empty or whitespace.")
        return value

    def __repr__(self):
        return f"<PromptSaver(id_prompt={self.id_prompt}, id_seller={self.id_seller}, id_platform={self.id_platform})>"
