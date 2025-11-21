from asyncio import Event
from typing import override, AsyncGenerator

from google.adk.agents import BaseAgent, LlmAgent, InvocationContext
import logging

from cpl_agent.agent_core.sub_agents.variant_extractor.agent import variant_extractor_agent
from cpl_agent.agent_core.sub_agents.variant_formatter.agent import variant_formatter_agent

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class CplAgent(BaseAgent):
    """
    CPL (Customer Product List) Agent
    
    This agent processes user prompts and generates responses based on 
    product variant data with stock information. Unlike the post_generator 
    which focuses on categories, this agent works directly with product 
    variants that have available stock.
    """
    
    variant_extractor: LlmAgent
    variant_formatter: LlmAgent
    
    def __init__(self, name: str, variant_extractor: LlmAgent, variant_formatter: LlmAgent):
        super().__init__(
            name=name,
            variant_extractor=variant_extractor,
            variant_formatter=variant_formatter
        )

    @override
    async def _run_async_impl(self, ctx: InvocationContext) -> AsyncGenerator[Event, None]:

        if "u_output" not in ctx.session.state:
            logger.error("User id not found in session state!")
            return

        u_output = ctx.session.state.get('u_output')
        logger.info(f"[{self.name}] - Processing request for User ID: {u_output}")

        # Step 1: Extract variants with stock
        logger.info(f"[{self.name}] - Extracting variants with stock...")
        async for event in self.variant_extractor.run_async(ctx):
            logger.info(f"[{self.name}] - {event.model_dump_json(indent=2, exclude_none=True)}")
            logger.info(f"[{self.name}] Demi tour e vita")
            yield event

        # Check if variants were extracted
        extracted_variants = ctx.session.state.get('extracted_variants')
        if not extracted_variants:
            logger.warning(f"[{self.name}] - No variants found with stock for user {u_output}")
            return

        logger.info(f"[{self.name}] - Variants extracted successfully")

        # Step 2: Format the output based on user's prompt
        logger.info(f"[{self.name}] - Formatting output based on user prompt...")
        async for event in self.variant_formatter.run_async(ctx):
            logger.info(f"[{self.name}] - {event.model_dump_json(indent=2, exclude_none=True)}")
            yield event

        formatted_output = ctx.session.state.get('formatted_output')
        if not formatted_output:
            logger.warning(f"[{self.name}] - Failed to format output")
            return

        logger.info(f"[{self.name}] - Processing complete")


# Initialize the root CPL agent
cpl_root_agent = CplAgent(
    name="cpl_root_agent",
    variant_extractor=variant_extractor_agent,
    variant_formatter=variant_formatter_agent
)
