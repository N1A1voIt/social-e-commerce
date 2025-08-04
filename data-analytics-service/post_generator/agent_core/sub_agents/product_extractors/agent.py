import ast
import json
import re

from google.adk.agents import LlmAgent
from pydantic import BaseModel

from post_generator.agent_core.sub_agents.product_extractors.prompt import PRODUCT_EXTRACTOR_PROMPT
from products.ProductRepository import ProductRepository


import json

def extract_products(db_category_output: str):
    def clean_and_parse_json(markdown_json_str):
        cleaned = re.sub(r"^```json\s*|\s*```$", "", markdown_json_str.strip(), flags=re.MULTILINE)
        return json.loads(cleaned)
    categoriese = clean_and_parse_json(db_category_output)
    print("dazfbne:"+str(categoriese))
    pr = ProductRepository()
    u_output = 1
    products = pr.find_by_token_and_categories(u_output, categoriese)
    products_dict = [p.model_dump() for p in products]
    return json.dumps(products_dict, default=str)



product_extractor_agent = LlmAgent (
    name="product_extractor_agent",
    model="gemini-2.0-flash-001",
    instruction="You're an agent in charge of extracting products from a list of category ids and a user_output.",
    description=PRODUCT_EXTRACTOR_PROMPT,
    output_key="extracted_products_v2",
    tools=[extract_products]
)