from flask import Flask, request, jsonify
from flask_cors import CORS
import random
import logging
import requests
import time

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@app.route('/api/ai-delivery-selection', methods=['POST'])
def ai_delivery_selection():
    """
    Simulated AI endpoint for delivery driver selection.
    Receives request, processes it, and POSTs result back to callback URL.
    
    Expected request format:
    {
        "deliveryId": 1,
        "firebaseUIDs": [
            {"uid": "firebase_uid_1"},
            {"uid": "firebase_uid_2"},
            {"uid": "firebase_uid_3"}
        ],
        "callbackUrl": "http://localhost:8080/api/deliveries/ai-callback"
    }
    
    Response (sent to callback URL):
    {
        "deliveryId": 1,
        "uid": "firebase_uid_2"
    }
    """
    try:
        data = request.get_json()
        
        logger.info(f"Received AI selection request: {data}")
        
        delivery_id = data.get('deliveryId')
        firebase_uids = data.get('firebaseUIDs', [])
        callback_url = data.get('callbackUrl')
        
        if not delivery_id:
            return jsonify({"error": "deliveryId is required"}), 400
        
        if not firebase_uids or len(firebase_uids) == 0:
            return jsonify({"error": "firebaseUIDs array is required and must not be empty"}), 400
        
        if not callback_url:
            return jsonify({"error": "callbackUrl is required"}), 400
        
        # Simulate AI processing time
        logger.info("AI is analyzing applicants...")
        time.sleep(2)  # Simulate 2 seconds of AI processing
        
        # Simulate AI decision making (random selection for now)
        # In a real scenario, this would use ML models, historical data, etc.
        selected_applicant = random.choice(firebase_uids)
        selected_uid = selected_applicant.get('uid')
        
        logger.info(f"AI selected UID: {selected_uid} for delivery ID: {delivery_id}")
        
        # Prepare response
        ai_response = {
            "deliveryId": delivery_id,
            "uid": selected_uid
        }
        
        # POST the result back to the callback URL
        logger.info(f"Posting AI selection result to callback URL: {callback_url}")
        try:
            callback_response = requests.post(
                callback_url,
                json=ai_response,
                headers={'Content-Type': 'application/json'},
                timeout=10
            )
            logger.info(f"Callback response status: {callback_response.status_code}")
            logger.info(f"Callback response body: {callback_response.text}")
            
            if callback_response.status_code == 200:
                return jsonify({
                    "status": "success",
                    "message": "AI selection completed and sent to callback URL",
                    "deliveryId": delivery_id
                }), 200
            else:
                logger.error(f"Callback failed with status {callback_response.status_code}")
                return jsonify({
                    "status": "error",
                    "message": "AI selection completed but callback failed",
                    "callbackStatus": callback_response.status_code
                }), 500
                
        except requests.exceptions.RequestException as e:
            logger.error(f"Error calling callback URL: {str(e)}")
            return jsonify({
                "status": "error",
                "message": "AI selection completed but callback failed",
                "error": str(e)
            }), 500
        
    except Exception as e:
        logger.error(f"Error in AI selection: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({"status": "healthy", "service": "AI Delivery Selection"}), 200

if __name__ == '__main__':
    print("=" * 60)
    print("AI Delivery Selection Mock Server (Webhook Pattern)")
    print("=" * 60)
    print("Server running on: http://localhost:5000")
    print("Endpoint: POST /api/ai-delivery-selection")
    print("Health check: GET /health")
    print("")
    print("This server receives delivery selection requests and")
    print("POSTs the AI decision back to the provided callback URL")
    print("=" * 60)
    app.run(host='0.0.0.0', port=5000, debug=True)
