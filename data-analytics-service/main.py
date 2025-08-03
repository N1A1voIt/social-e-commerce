import uuid
from typing import Optional

from dotenv import load_dotenv
from fastapi import FastAPI, Header, HTTPException
from google.adk import Runner
from google.adk.sessions import InMemorySessionService
from google.genai import types
from sympy import content

from post_generator.agent_core.agent import agent, root_agent
from tokens.TokenV2Repository import TokenV2Repository
from utils.query_modifier import QueryPayload

app = FastAPI()
load_dotenv()


@app.post("/")
async def create_post(
        query_payload: QueryPayload,
        authorization: Optional[str] = Header(None)  # Accepts the Authorization header
):
    query = query_payload.query
    if not authorization:
        raise HTTPException(
            status_code=401,
            detail="Authorization header is missing"
        )

    tokenRepository = TokenV2Repository()
    user_id = tokenRepository.find_user_id_by_token(authorization)

    if not user_id:
        raise HTTPException(
            status_code=401,
            detail="Invalid authentication token"
        )

    SESSION_ID = str(uuid.uuid4())
    session_service = InMemorySessionService()
    session = await session_service.create_session(app_name="SOCIALPOST", user_id=str(user_id), session_id=SESSION_ID,state={"authorization_token": authorization})
    # session.state['authorization_token'] = authorization
    # await session_service.c(session)
    runner = Runner(agent=root_agent, app_name="SOCIALPOST", session_service=session_service)

    def call_agent(query: str):
        content = types.Content(role="user", parts=[types.Part(text=query)])
        events = runner.run(user_id=str(user_id), session_id=SESSION_ID, new_message=content)

        returned = ""
        for event in events:
            if event.is_final_response():
                returned = event.content.parts[0].text
        return returned
#    await session_service.delete_session(session_id=SESSION_ID)
    return call_agent(query=query)

@app.get("/hello/{name}")
async def say_hello(name: str):
    return {"message": f"Hello {name}"}
