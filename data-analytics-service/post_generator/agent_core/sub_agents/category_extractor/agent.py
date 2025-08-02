import os

from google.adk.agents import LlmAgent

from post_generator.agent_core.sub_agents.category_extractor.prompt import CATEGORY_EXTRACTION_PROMPT

category_extractor_agent = LlmAgent(
    name="category_extractor_agent",
    model="gemini-2.0-flash-001",
    instruction="You're an agent in charge of extracting categories from a given text.",
    description=CATEGORY_EXTRACTION_PROMPT,
    output_key="extracted_categories",
)