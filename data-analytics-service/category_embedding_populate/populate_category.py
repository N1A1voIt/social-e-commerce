from sentence_transformers import SentenceTransformer
import psycopg2
from tqdm import tqdm

# Load embedding model (384-dim for MiniLM)
model = SentenceTransformer("all-MiniLM-L6-v2")

# Connect to PostgreSQL
conn = psycopg2.connect("dbname=postgres user=postgres password=postgres host=localhost port=5437")
cur = conn.cursor()

# Fetch rows without embeddings
cur.execute("SELECT id_category, desc_ FROM category WHERE embedding IS NULL")
rows = cur.fetchall()

for row in tqdm(rows):
    id, description = row

    if description:
        embedding = model.encode(description).tolist()
        cur.execute(
            "UPDATE category SET embedding = %s WHERE id_category = %s",
            (embedding, id)
        )

# Commit and close
conn.commit()
cur.close()
conn.close()
