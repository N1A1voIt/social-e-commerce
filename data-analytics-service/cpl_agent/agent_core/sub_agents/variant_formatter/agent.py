from google.adk.agents import LlmAgent

from cpl_agent.agent_core.sub_agents.variant_extractor.agent import variant_extractor_agent
from cpl_agent.agent_core.sub_agents.variant_formatter.prompt import FORMATTER_PROMPT

# Variant Formatter Agent
variant_formatter_agent = LlmAgent(
    name="variant_formatter_agent",
    model="gemini-2.0-flash-001",
    instruction="""You are a content generation agent for social media promotional posts.
        
        CRITICAL INSTRUCTIONS:
        1. You MUST use the {extracted_variants} data from the previous agent (stored in session state)
        2. DO NOT call any tools or functions - only format the provided data
        3. Generate promotional content in the EXACT JSON format specified in the description
        4. DO NOT create fake product data - only use the actual variants provided
        5. Output MUST be valid JSON array with facebook and instagram platform objects
        6. Include ALL available variants that have stock > 0 in your promotional content
        
        Use the title column for the name intead of name.
        
        Your ONLY task is to format the provided variant data into engaging social media posts following the exact structure and rules in the description.
    """,
    # sub_agents=[variant_extractor_agent],
    description=FORMATTER_PROMPT,
    output_key="formatted_output"
)
