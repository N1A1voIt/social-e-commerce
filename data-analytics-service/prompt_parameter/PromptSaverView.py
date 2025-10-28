from sqlalchemy import Column, Integer, Text, DateTime
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()


class PromptSaverView(Base):
    __tablename__ = 'prompt_saver_view'

    id_prompt = Column(Integer, primary_key=True)
    prompt = Column(Text, nullable=False)
    id_seller = Column(Integer, nullable=False)
    id_platform = Column(Integer, nullable=False)
    platform = Column(Text, nullable=False)
    created_at = Column(DateTime(timezone=True))

    def __repr__(self):
        return f"<PromptSaverView(id_prompt={self.id_prompt}, id_seller={self.id_seller}, platform='{self.platform}')>"
