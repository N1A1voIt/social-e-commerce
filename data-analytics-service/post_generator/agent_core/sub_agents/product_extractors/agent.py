import ast
import json
import re

from google.adk.agents import LlmAgent
from pydantic import BaseModel

from post_generator.agent_core.sub_agents.product_extractors.prompt import PRODUCT_EXTRACTOR_PROMPT
from products.ProductRepository import ProductRepository


import json

def extract_products(categories: str, token: str):
    def clean_and_parse_json(markdown_json_str):
        cleaned = re.sub(r"^```json\s*|\s*```$", "", markdown_json_str.strip(), flags=re.MULTILINE)
        return json.loads(cleaned)
    categoriese = clean_and_parse_json(categories)
    print("dazfbne:"+str(categoriese))
    pr = ProductRepository()
    return pr.find_by_token_and_categories(token, categoriese)


class ExtractedProducts(BaseModel):
    products:list

product_extractor_agent = LlmAgent(
    name="category_extractor_agent",
    model="gemini-2.0-flash-001",
    instruction="You're an agent in charge of extracting products from a list of categoriy ids.",
    description=PRODUCT_EXTRACTOR_PROMPT,
    output_key="extracted_products_v2",
    tools=[extract_products]
)