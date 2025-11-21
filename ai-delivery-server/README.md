# AI Delivery Selection Mock Server (Webhook Pattern)

This is a mock AI server that simulates intelligent delivery driver selection using a **webhook/callback pattern**.

## How It Works

1. Your main server sends a POST request with delivery info and a callback URL
2. The AI server processes the request (simulates AI analysis)
3. The AI server **POSTs the result back** to your callback URL
4. Your main server receives the callback and assigns the driver

This is a **true webhook pattern** where both parties communicate via POST requests.

## Setup

1. Create a virtual environment (optional but recommended):
```bash
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
```

2. Install dependencies:
```bash
pip install -r requirements.txt
```

3. Run the server:
```bash
python ai_server.py
```

The server will start on `http://localhost:5000`

## API Endpoints

### POST /api/ai-delivery-selection

Receives a delivery selection request and POSTs the result back to the callback URL.

**Request:**
```json
{
  "deliveryId": 1,
  "firebaseUIDs": [
    {"uid": "firebase_uid_1"},
    {"uid": "firebase_uid_2"},
    {"uid": "firebase_uid_3"}
  ],
  "callbackUrl": "http://localhost:8080/api/deliveries/ai-callback"
}
```

**Immediate Response (202 Accepted):**
```json
{
  "status": "success",
  "message": "AI selection completed and sent to callback URL",
  "deliveryId": 1
}
```

**Callback POST (sent to your server):**
```json
{
  "deliveryId": 1,
  "uid": "firebase_uid_2"
}
```

### GET /health

Health check endpoint.

**Response:**
```json
{
  "status": "healthy",
  "service": "AI Delivery Selection"
}
```

## Testing

### Test with curl

```bash
# Send request to AI server
curl -X POST http://localhost:5000/api/ai-delivery-selection \
  -H "Content-Type: application/json" \
  -d '{
    "deliveryId": 1,
    "firebaseUIDs": [
      {"uid": "test_uid_1"},
      {"uid": "test_uid_2"},
      {"uid": "test_uid_3"}
    ],
    "callbackUrl": "http://localhost:8080/api/deliveries/ai-callback"
  }'
```

### Test the complete flow

1. Start your main Spring Boot server (port 8080)
2. Start this AI mock server (port 5000)
3. Use the frontend to request AI assistance
4. Watch the logs to see:
   - Frontend sends request to Spring Boot
   - Spring Boot forwards to AI server
   - AI server processes and POSTs back to Spring Boot
   - Spring Boot assigns the driver

## Configuration

The AI server can be configured by modifying these variables in `ai_server.py`:

- **Port**: Default is 5000
- **Processing delay**: Default is 2 seconds (simulates AI thinking time)
- **Host**: Default is '0.0.0.0' (accepts connections from anywhere)

## Workflow Diagram

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│   Frontend   │       │ Spring Boot  │       │  AI Server   │
└──────┬───────┘       └──────┬───────┘       └──────┬───────┘
       │                      │                       │
       │ 1. Request AI        │                       │
       │    Assistance        │                       │
       ├─────────────────────>│                       │
       │                      │                       │
       │ 2. Accepted (202)    │                       │
       │<─────────────────────┤                       │
       │                      │                       │
       │                      │ 3. POST Request       │
       │                      │    + callback URL     │
       │                      ├──────────────────────>│
       │                      │                       │
       │                      │ 4. Accepted          │
       │                      │<──────────────────────┤
       │                      │                       │
       │                      │                    [AI Processing]
       │                      │                       │
       │                      │ 5. POST Result       │
       │                      │    (Callback)         │
       │                      │<──────────────────────┤
       │                      │                       │
       │                   [Assign Driver]            │
       │                      │                       │
       │ 6. Poll for status   │                       │
       │    (Optional)        │                       │
       ├─────────────────────>│                       │
       │                      │                       │
```

## Note

This is a **simulation server** for development and testing. In production, this would be replaced with:
- Real machine learning models for optimal driver selection
- Scalable message queue system (RabbitMQ, Kafka, etc.)
- Proper authentication and security
- Database for tracking AI decisions
- Retry logic for failed callbacks
- Monitoring and alerting
