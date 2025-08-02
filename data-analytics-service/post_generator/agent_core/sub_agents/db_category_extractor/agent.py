import os

from google.adk.agents import LlmAgent

from post_generator.agent_core.sub_agents.category_extractor.tools.category_extraction_tools import VectorSearchEngine
from post_generator.agent_core.sub_agents.db_category_extractor.prompt import DB_EXTRACTION_PROMPT


def extract_from_db(json:str) :
    vse = VectorSearchEngine()
    return vse.search_from_json(json)

db_extractor_agent = LlmAgent(
    name="db_extractor_agent",
    model="gemini-2.0-flash-001",
    instruction="You're an agent in charge of extracting categories id from a given array from the database.",
    description=DB_EXTRACTION_PROMPT,
    output_key="db_category_output",
    tools=[extract_from_db]
)