import uuid

from google.adk import Runner
from google.adk.sessions import InMemorySessionService
from google.genai import types

from post_generator.agent_core.agent import agent
from post_generator.agent_core.sub_agents.category_extractor.agent import category_extractor_agent

SESSION_ID = str(uuid.uuid4())
USER_ID = str(uuid.uuid4())
session_service = InMemorySessionService()
session = session_service.create_session(app_name="SOCIALPOST", user_id=USER_ID,session_id=SESSION_ID)
runner = Runner(agent=category_extractor_agent, app_name="SOCIALPOST", session_service=session_service)
def call_agent(query: str):
    content = types.Content(role="user",parts=[types.Part(text=query)])
    events = runner.run(user_id=USER_ID,session_id=SESSION_ID,new_message=content)
    for event in events:
        print(event.content.parts[0].text)
        if event.is_final_response():
            return event.content.parts[0].text

print(call_agent(query="Create a post that aims to leverage Electronics and Technology based products"))