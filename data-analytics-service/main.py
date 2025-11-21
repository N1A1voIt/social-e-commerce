import json
import re
import uuid
import time
import random
from typing import Optional

from dotenv import load_dotenv
from fastapi import FastAPI, Header, HTTPException
from google.adk import Runner
from google.adk.sessions import InMemorySessionService
from google.genai import types
from starlette.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import httpx

from generic_chat.agent import generic_agent
from generic_chat.generic_orchestrator.agent import generic_orchestrator_agent
from nlp_analyzer.agent.nlp_analyzer import nlp_messaging_agent
from post_generator.agent_core.agent import agent, root_agent
from cpl_agent.agent_core.agent import cpl_root_agent
from prompt_parameter.PromptSaverViewRepository import PromptSaverViewRepository
from tokens.TokenV2Repository import TokenV2Repository
from utils.query_modifier import QueryPayload

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
    runner = Runner(agent=cpl_root_agent, app_name="SOCIALPOST", session_service=session_service)

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

@app.post("/cpl-agent")
async def cpl_agent_endpoint(
        query_payload: QueryPayload,
        authorization: Optional[str] = Header(None)
):
    """
    CPL (Customer Product List) Agent endpoint.
    
    This endpoint processes user queries about products with available stock.
    Unlike /generate-post which focuses on categories, this endpoint works directly 
    with product variants that have stock > 0.
    
    The agent can handle various requests like:
    - Generate promotional content for available products
    - List products by category with stock info
    - Create custom formatted outputs based on user prompts
    - Generate social media content for specific products
    
    Args:
        query_payload: Contains the user's query/prompt
        authorization: Bearer token for authentication
    
    Returns:
        Formatted response based on user's prompt and available product variants
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
    
    print(f"[CPL Agent] Processing request for user {user_id}")

    # Create session with user_id in state
    SESSION_ID = str(uuid.uuid4())
    session_service = InMemorySessionService()
    session = await session_service.create_session(
        app_name="CPLAGENT", 
        user_id=str(user_id), 
        session_id=SESSION_ID,
        state={"u_output": user_id}
    )
    
    # Initialize runner with CPL agent
    runner = Runner(
        agent=root_agent, 
        app_name="CPLAGENT", 
        session_service=session_service
    )

    def call_agent(query: str):
        content = types.Content(role="user", parts=[types.Part(text=query)])
        events = runner.run(user_id=str(user_id), session_id=SESSION_ID, new_message=content)

        returned = ""
        for event in events:
            if event.is_final_response():
                returned = event.content.parts[0].text
        return returned
    
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

@app.post("/generic-chat")
async def generic_chat(query_payload: QueryPayload, authorization: Optional[str] = Header(None)):
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
    session = await session_service.create_session(app_name="GENERICNLP", user_id=str(user_id), session_id=SESSION_ID,
                                                   state={"u_output": user_id})

    runner = Runner(agent=generic_orchestrator_agent, app_name="GENERICNLP", session_service=session_service)

    def call_agent(query: str):
        content = types.Content(role="user", parts=[types.Part(text=query)])
        events = runner.run(user_id=str(user_id), session_id=SESSION_ID, new_message=content)
        returned = ""
        for event in events:
            if event.is_final_response():
                returned = event.content.parts[0].text
        print("returned:"+returned)
        returned = re.sub(r"^```json\s*", "", returned.strip())
        returned = re.sub(r"\s*```$", "", returned)

        print("Cleaned returned:", returned)

        return json.loads(returned)
    return call_agent(query=query)

# Models for AI Delivery Selection
class FirebaseUidDTO(BaseModel):
    uid: str
    
class AiDeliveryAssistanceRequest(BaseModel):
    deliveryId: int
    firebaseUIDs: list[FirebaseUidDTO]
    callbackUrl: str

class AiDeliveryResponse(BaseModel):
    deliveryId: int
    uid: str

@app.post("/api/ai-delivery-selection")
async def ai_delivery_selection(request: AiDeliveryAssistanceRequest):
    """
    Mock AI endpoint for delivery driver selection.
    Receives POST with delivery info and callback URL.
    Simulates AI processing and POSTs result back to callback URL.
    """
    try:
        # Validate required fields
        if not request.callbackUrl:
            raise HTTPException(status_code=400, detail="callbackUrl is required")
        
        if not request.firebaseUIDs or len(request.firebaseUIDs) == 0:
            raise HTTPException(status_code=400, detail="firebaseUIDs list cannot be empty")
        
        print(f"[AI Server] Received request for delivery {request.deliveryId}")
        print(f"[AI Server] Callback URL: {request.callbackUrl}")
        print(f"[AI Server] Number of candidates: {len(request.firebaseUIDs)}")
        
        # Simulate AI processing delay
        time.sleep(2)
        
        # Randomly select one UID (mock AI decision)
        selected_uid_dto = random.choice(request.firebaseUIDs)
        selected_uid = selected_uid_dto.uid
        
        print(f"[AI Server] AI selected UID: {selected_uid}")
        
        # Prepare response
        ai_response = {
            "deliveryId": request.deliveryId,
            "uid": selected_uid
        }
        
        # POST result back to callback URL
        async with httpx.AsyncClient() as client:
            callback_response = await client.post(
                request.callbackUrl,
                json=ai_response,
                headers={"Content-Type": "application/json"}
            )
            
            print(f"[AI Server] Callback POST status: {callback_response.status_code}")
            
            if callback_response.status_code != 200:
                print(f"[AI Server] Callback failed: {callback_response.text}")
                return {
                    "status": "error",
                    "message": f"Callback failed with status {callback_response.status_code}"
                }
        
        return {
            "status": "success",
            "message": "AI processing completed and result sent to callback URL"
        }
        
    except HTTPException:
        raise
    except Exception as e:
        print(f"[AI Server] Error: {str(e)}")
        raise HTTPException(status_code=500, detail=f"AI processing failed: {str(e)}")

@app.get("/hello/{name}")
async def say_hello(name: str):
    return {"message": f"Hello {name}"}
