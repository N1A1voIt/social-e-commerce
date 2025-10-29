import json
import uuid
from typing import Optional

from dotenv import load_dotenv
from fastapi import FastAPI, Header, HTTPException
from google.adk import Runner
from google.adk.sessions import InMemorySessionService
from google.genai import types
from starlette.middleware.cors import CORSMiddleware

from nlp_analyzer.agent.nlp_analyzer import nlp_messaging_agent
from post_generator.agent_core.agent import agent, root_agent
from prompt_parameter.PromptSaverViewRepository import PromptSaverViewRepository
from tokens.TokenV2Repository import TokenV2Repository
from utils.query_modifier import QueryPayload
from generic_chat.agent import generic_chat_agent
from generic_chat.api_function_caller import APIFunctionCaller
from nlp_analyzer.db.order_repository import OrderRepository
from datetime import datetime
import re
from pydantic import BaseModel

app = FastAPI()
load_dotenv()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*",],  # or ["*"] for dev
    allow_credentials=True,
    allow_methods=["*"],  # Or ["GET", "POST", "OPTIONS"]
    allow_headers=["*"],  # You can restrict this if needed
)
@app.post("/generate-post")
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
    repo = PromptSaverViewRepository()
    prompt_views = repo.find_by_id_seller(user_id)

    prompt_texts = "\n".join(f"[{p.platform}] {p.prompt}" for p in prompt_views)

    query_payload.query += prompt_texts

    SESSION_ID = str(uuid.uuid4())
    session_service = InMemorySessionService()
    session = await session_service.create_session(app_name="SOCIALPOST", user_id=str(user_id), session_id=SESSION_ID,state={"u_output": user_id})
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

@app.post("/extract-skus-qty")
async def extract_skus_qty(query_payload:QueryPayload,authorization: Optional[str] = Header(None)):
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
    session = await session_service.create_session(app_name="MESSAGINGNLP", user_id=str(user_id), session_id=SESSION_ID,
                                                   state={"u_output": user_id})

    runner = Runner(agent=nlp_messaging_agent, app_name="MESSAGINGNLP", session_service=session_service)
    def call_agent(query: str):
        content = types.Content(role="user", parts=[types.Part(text=query)])
        events = runner.run(user_id=str(user_id), session_id=SESSION_ID, new_message=content)
        returned = ""
        for event in events:
            if event.is_final_response():
                returned = event.content.parts[0].text
        return json.loads(returned)
    return call_agent(query=query)


class SalesEvolutionPayload(BaseModel):
    start_date: str
    end_date: str


@app.post("/sales-evolution")
async def get_sales_evolution(
        payload: SalesEvolutionPayload,
        authorization: Optional[str] = Header(None)
):
    """
    Get sales evolution between two dates for orders with status > 25.
    Returns daily aggregated sales data.
    """
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

    try:
        # Parse dates
        start_date = datetime.strptime(payload.start_date, "%Y-%m-%d")
        end_date = datetime.strptime(payload.end_date, "%Y-%m-%d")

        # Get sales evolution
        order_repo = OrderRepository()
        sales_data = order_repo.get_sales_evolution(
            start_date=start_date,
            end_date=end_date,
            user_id=user_id,
            status_threshold=25
        )

        # Also get summary
        summary = order_repo.get_sales_summary(
            start_date=start_date,
            end_date=end_date,
            user_id=user_id,
            status_threshold=25
        )

        return {
            "success": True,
            "data": {
                "evolution": [item.dict() for item in sales_data],
                "summary": summary
            }
        }

    except ValueError as e:
        raise HTTPException(
            status_code=400,
            detail=f"Invalid date format. Use YYYY-MM-DD. Error: {str(e)}"
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Error retrieving sales data: {str(e)}"
        )


@app.post("/generic-chat")
async def generic_chat(
        query_payload: QueryPayload,
        authorization: Optional[str] = Header(None)
):
    """
    Generic NLP-powered chat endpoint that understands user queries and routes them
    to appropriate API functions.
    """
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

    # Create API caller with authorization
    api_caller = APIFunctionCaller(authorization=authorization)

    # Create session for the agent
    SESSION_ID = str(uuid.uuid4())
    session_service = InMemorySessionService()
    session = await session_service.create_session(
        app_name="GENERICCHAT",
        user_id=str(user_id),
        session_id=SESSION_ID,
        state={"u_output": user_id, "authorization": authorization}
    )

    runner = Runner(agent=generic_chat_agent, app_name="GENERICCHAT", session_service=session_service)

    def call_agent(query: str):
        content = types.Content(role="user", parts=[types.Part(text=query)])
        events = runner.run(user_id=str(user_id), session_id=SESSION_ID, new_message=content)

        returned = ""
        for event in events:
            if event.is_final_response():
                returned = event.content.parts[0].text
        return returned

    # Get the agent's response
    agent_response = call_agent(query=query)

    # Parse the response to see if it contains API call markers
    if "CALL_API:generate_post:" in agent_response:
        # Extract the query for post generation
        match = re.search(r"CALL_API:generate_post:(.*?)(?:\n|$)", agent_response, re.DOTALL)
        if match:
            post_query = match.group(1).strip()
            result = await api_caller.call_generate_post(post_query)
            if result["success"]:
                return {
                    "intent": "generate_post",
                    "data": result["data"],
                    "agent_response": agent_response.replace(match.group(0), "")
                }
            else:
                return {
                    "intent": "generate_post",
                    "error": result["error"],
                    "status_code": result["status_code"]
                }

    elif "CALL_API:extract_skus:" in agent_response:
        # Extract the message for SKU extraction
        match = re.search(r"CALL_API:extract_skus:(.*?)(?:\n|$)", agent_response, re.DOTALL)
        if match:
            extract_query = match.group(1).strip()
            result = await api_caller.call_extract_skus(extract_query)
            if result["success"]:
                return {
                    "intent": "extract_skus",
                    "data": result["data"],
                    "agent_response": agent_response.replace(match.group(0), "")
                }
            else:
                return {
                    "intent": "extract_skus",
                    "error": result["error"],
                    "status_code": result["status_code"]
                }

    elif "CALL_API:sales_evolution:" in agent_response:
        # Extract the dates for sales evolution
        match = re.search(r"CALL_API:sales_evolution:(.*?)(?:\n|$)", agent_response, re.DOTALL)
        if match:
            dates_str = match.group(1).strip()
            # Parse the dates (format: start_date|end_date)
            dates = dates_str.split("|")
            if len(dates) == 2:
                start_date = dates[0].strip()
                end_date = dates[1].strip()
                result = await api_caller.call_sales_evolution(start_date, end_date)
                if result["success"]:
                    return {
                        "intent": "sales_evolution",
                        "data": result["data"],
                        "agent_response": agent_response.replace(match.group(0), "")
                    }
                else:
                    return {
                        "intent": "sales_evolution",
                        "error": result["error"],
                        "status_code": result["status_code"]
                    }

    # If no API call was needed, return the agent's response directly
    return {
        "intent": "general",
        "response": agent_response
    }


@app.get("/hello/{name}")
async def say_hello(name: str):
    return {"message": f"Hello {name}"}
