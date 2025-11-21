import json
from google.adk.agents import LlmAgent
from products.VariantCplAgentRepository import VariantCplAgentRepository
from cpl_agent.agent_core.sub_agents.variant_extractor.prompt import VARIANT_EXTRACTOR_PROMPT


def extract_variants(user_id: int):
    """
    Extract variants based on user ID that have stock available.

    Args:
        user_id: The seller user ID to filter variants by
    
    Returns:
        JSON string containing list of variants with stock > 0
    """
    repo = VariantCplAgentRepository()
    
    print(f'Extracting variants for User ID: {user_id}')
    
    if user_id is None:
        raise ValueError("User ID not found")
    
    # Get all variants with stock for this seller
    variants = repo.find_by_seller_with_stock(user_id)
    
    # Convert to dict for JSON serialization
    variants_dict = [v.model_dump() for v in variants]
    
    print(f'Found {len(variants_dict)} variants with stock for user {user_id}')
    
    return json.dumps(variants_dict, default=str)


# Variant Extractor Agent
variant_extractor_agent = LlmAgent(
    name="variant_extractor_agent",
    model="gemini-2.0-flash-001",
    instruction="""You are a variant extraction agent. Your ONLY task is to call the extract_variants function.
            CRITICAL INSTRUCTIONS:
            1. You MUST call the extract_variants function exactly once
            2. The function requires ONE parameter: user_id (integer)
            3. Get the user_id from the session state variable named {u_output}
            4. DO NOT modify, interpret, or change the user_id value
            5. DO NOT generate any additional content or explanations
            6. DO NOT create fake or placeholder data
            
            REQUIRED ACTION:
            Call extract_variants(user_id={u_output})
        
            Return the result as JSON.
            
        """,
    description=VARIANT_EXTRACTOR_PROMPT,
    output_key="extracted_variants",
    tools=[extract_variants]
)
