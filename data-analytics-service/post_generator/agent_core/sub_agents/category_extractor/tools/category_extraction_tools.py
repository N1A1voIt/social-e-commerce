import numpy as np
import json
from sentence_transformers import SentenceTransformer
from sqlalchemy import create_engine, Column, Integer, Text, Float
from sqlalchemy.orm import sessionmaker, declarative_base
from pgvector.sqlalchemy import Vector

from post_generator.agent_core.engine import SessionLocal, engine  # Import engine as well
from utils.model_provider import ModelProvider

Base = declarative_base()


class Category(Base):
    __tablename__ = "category"

    id_category = Column(Integer, primary_key=True)
    val = Column(Text)
    desc_ = Column(Text)
    embedding = Column(Vector(384))


class VectorSearchEngine:
    def __init__(self):
        # Fix: Use the actual engine, not SessionLocal
        self.engine = engine  # This should be the SQLAlchemy engine
        self.Session = SessionLocal  # This should be the session factory
        self.model = ModelProvider.get_model()
        self.doc_ids = []
        self.doc_texts = []
        self.doc_embeddings = None
        self.doc_embeddings_norm = None
        self._load_embeddings_from_db()

    def _load_embeddings_from_db(self):
        # Fix: Call SessionLocal() to create a session instance
        session = self.Session()
        try:
            docs = session.query(Category).all()

            self.doc_ids = [doc.id_category for doc in docs]  # Fixed: use id_category
            self.doc_texts = [doc.val for doc in docs]

            # Handle case where there might be no embeddings
            if docs and docs[0].embedding is not None:
                self.doc_embeddings = np.array([doc.embedding for doc in docs])
                # Normalize for cosine similarity
                norms = np.linalg.norm(self.doc_embeddings, axis=1, keepdims=True)
                self.doc_embeddings_norm = self.doc_embeddings / norms
            else:
                self.doc_embeddings = np.array([])
                self.doc_embeddings_norm = np.array([])

        finally:
            session.close()

    def encode_queries(self, texts):
        embeddings = self.model.encode(texts, convert_to_numpy=True)
        norms = np.linalg.norm(embeddings, axis=1, keepdims=True)
        return embeddings / norms

    def search(self, query_texts, top_k=2):
        # Handle case where no embeddings are loaded
        if len(self.doc_embeddings) == 0:
            return [{"query": query, "matches": []} for query in query_texts]

        query_embeddings_norm = self.encode_queries(query_texts)
        similarity_matrix = np.dot(query_embeddings_norm, self.doc_embeddings_norm.T)

        results = []
        for i, query in enumerate(query_texts):
            top_indices = similarity_matrix[i].argsort()[::-1][:top_k]
            matches = [
                {
                    "doc_id": self.doc_ids[idx],
                    "val": self.doc_texts[idx],
                    "score": float(similarity_matrix[i][idx])  # Convert to Python float for JSON serialization
                }
                for idx in top_indices
            ]
            results.append({"query": query, "matches": matches})
        return results

    def search_from_json(self, json_str):
        # Fix: Handle the JSON parsing more robustly
        try:
            # Clean the JSON string
            categories = json_str.replace("```json", "").replace("```", "").strip()
            json_categories = json.loads(categories)
            categories_list = json_categories
            return self.search(categories_list)
        except (json.JSONDecodeError, KeyError) as e:
            raise ValueError(f"Invalid JSON format or missing 'categories' key: {e}")