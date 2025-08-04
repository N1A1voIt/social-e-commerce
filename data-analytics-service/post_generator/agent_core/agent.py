from asyncio import Event
from typing import override, AsyncGenerator

from google.adk.agents import BaseAgent, LlmAgent, InvocationContext
import logging

from post_generator.agent_core.sub_agents.category_extractor.agent import category_extractor_agent

from post_generator.agent_core.sub_agents.db_category_extractor.agent import db_extractor_agent
from post_generator.agent_core.sub_agents.product_extractors.agent import product_extractor_agent

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class PostGeneratorAgent(BaseAgent):
    category_extactor: LlmAgent
    db_extractor: LlmAgent
    product_extractor_agent:LlmAgent
    def __init__(self, name,category_extactor: LlmAgent,db_extractor: LlmAgent,product_extractor_agent:LlmAgent):
        super().__init__(
            name=name,
            category_extactor=category_extactor,
            db_extractor=db_extractor,
            product_extractor_agent = product_extractor_agent
        )

    @override
    async def _run_async_impl(self, ctx: InvocationContext) -> AsyncGenerator[Event, None]:
        u_output = ctx.session.state.get('u_output')
        if "u_output" not in ctx.session.state:
            logger.error("User id not found in session state!")
            # Handle the error appropriately
            return

        # First call to category_extactor
        async for event in self.category_extactor.run_async(ctx):
            logger.info(f"[{self.name}] - {event.model_dump_json(indent=2, exclude_none=True)}")
            yield event

        category_extractor_output = []
        if "extracted_categories" in ctx.session.state:
            category_extractor_output = ctx.session.state['extracted_categories']
            logger.info(f"[{self.name}] - {category_extractor_output}")

        if category_extractor_output is None or "```json" not in category_extractor_output:
            return

        # query generation agent call
        async for event in self.db_extractor.run_async(ctx):
            logger.info(f"[{self.name}] - {event.model_dump_json(indent=2, exclude_none=True)}")
            yield event

        logger.info(f"[{self.name}] - User id: {ctx.session.state['u_output']}")


        db_category_output = ctx.session.state['db_category_output']
        logger.info(f"[{self.name}] - {db_category_output}")

        if db_category_output is None:
            return

        async for event in self.product_extractor_agent.run_async(ctx):
            logger.info(f"[{self.name}] - {event.model_dump_json(indent=2, exclude_none=True)} - {ctx.session.state}")
            yield event

        extracted_products_v2 = ctx.session.state['extracted_products_v2']
        logger.info(f"[{self.name}] - {extracted_products_v2}")

        if extracted_products_v2 is None:
            return
        #
        # # query review rewrite agent call
        # async for event in self.query_review_rewrite_agent.run_async(ctx):
        #     logger.info(f"[{self.name}] - {event.model_dump_json(indent=2, exclude_none=True)}")
        #     yield event
        #
        # query_review_rewrite_output = ctx.session.state['query_review_rewrite_output']
        # logger.info(f"[{self.name}] - {query_review_rewrite_output}")
        #
        # if query_review_rewrite_output is None:
        #     return
        #
        # # query execution agent call
        # async for event in self.query_execution_agent.run_async(ctx):
        #     print(f"DEBUG: Event object type: {type(event)}, Event content: {event}")
        #
        #     logger.info(f"[{self.name}] - {event.model_dump_json(indent=2, exclude_none=True)}")
        #     yield event

        # query_execution_output = ctx.session.state['query_execution_output']
        # logger.info(f"[{self.name}] - {query_execution_output}")
        # query_execution_output = ctx.session.state.get('query_execution_output')
        # ctx.session.state['query_execution_output'] = self.serialize_dates(query_execution_output)
        #
        # if query_execution_output is None:
        #     return

agent = PostGeneratorAgent(name="Agent",category_extactor=category_extractor_agent,db_extractor=db_extractor_agent,product_extractor_agent=product_extractor_agent)
root_agent = agent
