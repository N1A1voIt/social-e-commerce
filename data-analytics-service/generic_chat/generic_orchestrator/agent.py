import logging
from asyncio import Event
from typing import AsyncGenerator, override

from google.adk.agents import BaseAgent, LlmAgent, InvocationContext

from generic_chat.agent import generic_agent
from generic_chat.formatter_agent.agent import formatter_agent

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class GenericOrchestratorAgent(BaseAgent):
    generic_chat: LlmAgent
    formatter_agent: LlmAgent

    def __init__(self, name, generic_chat: LlmAgent, formatter_agent: LlmAgent):
        super().__init__(
            name=name,
            generic_chat=generic_chat,
            formatter_agent=formatter_agent,
        )

    @override
    async def _run_async_impl(self, ctx: InvocationContext) -> AsyncGenerator[Event, None]:
        u_output = ctx.session.state.get('u_output')
        if "u_output" not in ctx.session.state:
            logger.error("User id not found in session state!")
            # Handle the error appropriately
            return

        # First call to generic_chat
        async for event in self.generic_chat.run_async(ctx):
            logger.info(f"[{self.name}] - {event.model_dump_json(indent=2, exclude_none=True)}")
            yield event

        generic_output = []
        if "extracted_categories" in ctx.session.state:
            generic_output = ctx.session.state['generic_output']
            logger.info(f"[{self.name}] - {generic_output}")

        # if generic_output is None or "```json" not in generic_output:
        #     return

        print(f"Generic Output: {generic_output}")

        # query generation agent call
        async for event in self.formatter_agent.run_async(ctx):
            logger.info(f"[{self.name}] - {event.model_dump_json(indent=2, exclude_none=True)}")
            yield event

        logger.info(f"[{self.name}] - User id: {ctx.session.state['u_output']}")

        formatter_output = ctx.session.state['formatted_output']
        logger.info(f"[{self.name}] - {formatter_output}")

        if formatter_output is None:
            return

agent = GenericOrchestratorAgent(name="AgentGeneric",generic_chat = generic_agent, formatter_agent = formatter_agent)
generic_orchestrator_agent = agent