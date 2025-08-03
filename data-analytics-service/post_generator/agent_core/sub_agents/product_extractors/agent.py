from google.adk.agents import LlmAgent

from post_generator.agent_core.sub_agents.product_extractors.prompt import PRODUCT_EXTRACTOR_PROMPT

product_ectractor_agent = LlmAgent(
    name="category_extractor_agent",
    model="gemini-2.0-flash-001",
    instruction="You're an agent in charge of extracting products from a list of categoriy ids.",
    description=PRODUCT_EXTRACTOR_PROMPT,
    output_key="extracted_products",
)