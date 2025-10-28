# Prompt Saver API Documentation

## Overview
The Prompt Saver API allows sellers to manage AI prompts for different social media platforms. Each seller can have one prompt per platform that can be used for AI-powered content generation.

## Base URL
```
/api/prompts
```

## Authentication
All endpoints require Bearer token authentication. Include the authorization token in the request header:
```
Authorization: Bearer <your_token>
```

## Data Models

### PromptSaverRequest
```json
{
  "prompt": "string (required) - The AI prompt text",
  "platformId": "number (required) - ID of the platform (1=facebook, 2=instagram, 3=x, 4=thread)"
}
```

### PromptSaverResponse
```json
{
  "id": "number - Unique identifier of the prompt",
  "prompt": "string - The prompt text",
  "sellerId": "number - ID of the seller who owns this prompt",
  "platformId": "number - ID of the platform",
  "platformLabel": "string - Human-readable platform name (facebook/instagram/x/thread)",
  "createdAt": "string (ISO datetime) - When the prompt was created"
}
```

### PlatformInfo
```json
{
  "id": "number - Platform ID",
  "label": "string - Platform name (facebook/instagram/x/thread)"
}
```

### ErrorResponse
```json
{
  "message": "string - Error description",
  "error": "string - Error type",
  "status": "number - HTTP status code",
  "timestamp": "string (ISO datetime) - When the error occurred",
  "path": "string - API endpoint that caused the error"
}
```

## Endpoints

### 1. Get All Prompts for Current User
**GET** `/api/prompts`

Returns all prompts for the authenticated seller.

**Headers:**
- `Authorization: Bearer <token>` (required)

**Response:**
- **200 OK**: Array of PromptSaverResponse objects
- **401 Unauthorized**: Invalid or missing token
- **500 Internal Server Error**: Server error

**Example Response:**
```json
[
  {
    "id": 1,
    "prompt": "Create engaging content for my fashion store targeting young adults",
    "sellerId": 123,
    "platformId": 1,
    "platformLabel": "facebook",
    "createdAt": "2024-12-01T10:30:00"
  },
  {
    "id": 2,
    "prompt": "Generate trendy posts for my accessories collection",
    "sellerId": 123,
    "platformId": 2,
    "platformLabel": "instagram",
    "createdAt": "2024-12-01T11:45:00"
  }
]
```

### 2. Get Prompt by Platform
**GET** `/api/prompts/platform/{platformId}`

Returns the prompt for a specific platform for the authenticated seller.

**Headers:**
- `Authorization: Bearer <token>` (required)

**Path Parameters:**
- `platformId` (number, required): ID of the platform

**Response:**
- **200 OK**: PromptSaverResponse object
- **404 Not Found**: No prompt found for this platform
- **401 Unauthorized**: Invalid or missing token
- **500 Internal Server Error**: Server error

**Example Response:**
```json
{
  "id": 1,
  "prompt": "Create engaging content for my fashion store targeting young adults",
  "sellerId": 123,
  "platformId": 1,
  "platformLabel": "facebook",
  "createdAt": "2024-12-01T10:30:00"
}
```

### 3. Create or Update Prompt
**POST** `/api/prompts`

Creates a new prompt or updates an existing one for the specified platform. Each seller can only have one prompt per platform.

**Headers:**
- `Authorization: Bearer <token>` (required)
- `Content-Type: application/json`

**Request Body:**
```json
{
  "prompt": "Create engaging content for my fashion store targeting young adults",
  "platformId": 1
}
```

**Response:**
- **200 OK**: PromptSaverResponse object (created or updated)
- **400 Bad Request**: Invalid request data or platform doesn't exist
- **401 Unauthorized**: Invalid or missing token
- **500 Internal Server Error**: Server error

**Example Response:**
```json
{
  "id": 1,
  "prompt": "Create engaging content for my fashion store targeting young adults",
  "sellerId": 123,
  "platformId": 1,
  "platformLabel": "facebook",
  "createdAt": "2024-12-01T10:30:00"
}
```

### 4. Delete Prompt
**DELETE** `/api/prompts/platform/{platformId}`

Deletes the prompt for a specific platform for the authenticated seller.

**Headers:**
- `Authorization: Bearer <token>` (required)

**Path Parameters:**
- `platformId` (number, required): ID of the platform

**Response:**
- **204 No Content**: Prompt successfully deleted
- **404 Not Found**: No prompt found for this platform
- **401 Unauthorized**: Invalid or missing token
- **500 Internal Server Error**: Server error

### 5. Get Available Platforms
**GET** `/api/prompts/platforms`

Returns all available social media platforms.

**Headers:**
- `Authorization: Bearer <token>` (required)

**Response:**
- **200 OK**: Array of PlatformInfo objects
- **401 Unauthorized**: Invalid or missing token
- **500 Internal Server Error**: Server error

**Example Response:**
```json
[
  {
    "id": 1,
    "label": "facebook"
  },
  {
    "id": 2,
    "label": "instagram"
  },
  {
    "id": 3,
    "label": "x"
  },
  {
    "id": 4,
    "label": "thread"
  }
]
```

## Platform IDs Reference
- **1**: Facebook
- **2**: Instagram  
- **3**: X (formerly Twitter)
- **4**: Threads

## Error Handling

All endpoints return consistent error responses with the following structure:

```json
{
  "message": "Detailed error description",
  "error": "Error type",
  "status": 400,
  "timestamp": "2024-12-01T10:30:00",
  "path": "/api/prompts"
}
```

### Common Error Codes:
- **400 Bad Request**: Invalid request data, validation errors
- **401 Unauthorized**: Missing or invalid authentication token
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Unexpected server error

## Usage Examples

### JavaScript/Fetch API Examples

#### Get all prompts for current user:
```javascript
const response = await fetch('/api/prompts', {
  headers: {
    'Authorization': `Bearer ${userToken}`,
    'Content-Type': 'application/json'
  }
});

if (response.ok) {
  const prompts = await response.json();
  console.log('User prompts:', prompts);
} else {
  console.error('Error:', response.status);
}
```

#### Create or update a prompt:
```javascript
const promptData = {
  prompt: "Create engaging content for my fashion store targeting young adults",
  platformId: 1
};

const response = await fetch('/api/prompts', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${userToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(promptData)
});

if (response.ok) {
  const savedPrompt = await response.json();
  console.log('Prompt saved:', savedPrompt);
} else {
  console.error('Error saving prompt:', response.status);
}
```

#### Get prompt for specific platform:
```javascript
const platformId = 1; // Facebook
const response = await fetch(`/api/prompts/platform/${platformId}`, {
  headers: {
    'Authorization': `Bearer ${userToken}`,
    'Content-Type': 'application/json'
  }
});

if (response.ok) {
  const prompt = await response.json();
  console.log('Facebook prompt:', prompt);
} else if (response.status === 404) {
  console.log('No prompt found for this platform');
} else {
  console.error('Error:', response.status);
}
```

#### Delete a prompt:
```javascript
const platformId = 1; // Facebook
const response = await fetch(`/api/prompts/platform/${platformId}`, {
  method: 'DELETE',
  headers: {
    'Authorization': `Bearer ${userToken}`
  }
});

if (response.ok) {
  console.log('Prompt deleted successfully');
} else if (response.status === 404) {
  console.log('No prompt found to delete');
} else {
  console.error('Error deleting prompt:', response.status);
}
```

#### Get available platforms:
```javascript
const response = await fetch('/api/prompts/platforms', {
  headers: {
    'Authorization': `Bearer ${userToken}`,
    'Content-Type': 'application/json'
  }
});

if (response.ok) {
  const platforms = await response.json();
  console.log('Available platforms:', platforms);
} else {
  console.error('Error:', response.status);
}
```

### React Component Example

```jsx
import React, { useState, useEffect } from 'react';

const PromptManager = ({ authToken }) => {
  const [prompts, setPrompts] = useState([]);
  const [platforms, setPlatforms] = useState([]);
  const [selectedPlatform, setSelectedPlatform] = useState('');
  const [promptText, setPromptText] = useState('');
  const [loading, setLoading] = useState(false);

  // Load prompts and platforms on component mount
  useEffect(() => {
    loadPrompts();
    loadPlatforms();
  }, []);

  const loadPrompts = async () => {
    try {
      const response = await fetch('/api/prompts', {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        setPrompts(data);
      }
    } catch (error) {
      console.error('Error loading prompts:', error);
    }
  };

  const loadPlatforms = async () => {
    try {
      const response = await fetch('/api/prompts/platforms', {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        setPlatforms(data);
      }
    } catch (error) {
      console.error('Error loading platforms:', error);
    }
  };

  const savePrompt = async () => {
    if (!selectedPlatform || !promptText.trim()) return;
    
    setLoading(true);
    try {
      const response = await fetch('/api/prompts', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          prompt: promptText,
          platformId: parseInt(selectedPlatform)
        })
      });
      
      if (response.ok) {
        await loadPrompts(); // Reload prompts
        setPromptText('');
        setSelectedPlatform('');
      }
    } catch (error) {
      console.error('Error saving prompt:', error);
    } finally {
      setLoading(false);
    }
  };

  const deletePrompt = async (platformId) => {
    try {
      const response = await fetch(`/api/prompts/platform/${platformId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });
      
      if (response.ok) {
        await loadPrompts(); // Reload prompts
      }
    } catch (error) {
      console.error('Error deleting prompt:', error);
    }
  };

  return (
    <div className="prompt-manager">
      <h2>AI Prompt Manager</h2>
      
      {/* Create/Update Form */}
      <div className="prompt-form">
        <select 
          value={selectedPlatform} 
          onChange={(e) => setSelectedPlatform(e.target.value)}
        >
          <option value="">Select Platform</option>
          {platforms.map(platform => (
            <option key={platform.id} value={platform.id}>
              {platform.label}
            </option>
          ))}
        </select>
        
        <textarea
          value={promptText}
          onChange={(e) => setPromptText(e.target.value)}
          placeholder="Enter your AI prompt..."
          rows={4}
        />
        
        <button 
          onClick={savePrompt} 
          disabled={loading || !selectedPlatform || !promptText.trim()}
        >
          {loading ? 'Saving...' : 'Save Prompt'}
        </button>
      </div>

      {/* Existing Prompts List */}
      <div className="prompts-list">
        <h3>Your Prompts</h3>
        {prompts.map(prompt => (
          <div key={prompt.id} className="prompt-item">
            <div className="prompt-header">
              <strong>{prompt.platformLabel}</strong>
              <button onClick={() => deletePrompt(prompt.platformId)}>
                Delete
              </button>
            </div>
            <p>{prompt.prompt}</p>
            <small>Created: {new Date(prompt.createdAt).toLocaleDateString()}</small>
          </div>
        ))}
      </div>
    </div>
  );
};

export default PromptManager;
```

## Security Notes

1. **Authentication Required**: All endpoints require valid Bearer token authentication
2. **User Isolation**: Users can only access their own prompts - no cross-user data access
3. **Input Validation**: All inputs are validated server-side
4. **Rate Limiting**: Consider implementing rate limiting on your client side for better UX

## Best Practices

1. **Error Handling**: Always check response status and handle errors appropriately
2. **Loading States**: Show loading indicators during API calls
3. **Validation**: Validate data on client side before sending to API
4. **Caching**: Consider caching platform information since it rarely changes
5. **Token Management**: Ensure proper token storage and refresh mechanisms

## Testing

You can test the API endpoints using tools like:
- **Postman**: Create a collection with all endpoints
- **curl**: Command line testing
- **Browser DevTools**: For frontend integration testing

### Example curl commands:

```bash
# Get all prompts
curl -X GET "http://localhost:8080/api/prompts" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Create prompt
curl -X POST "http://localhost:8080/api/prompts" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Your AI prompt text", "platformId": 1}'

# Get platforms
curl -X GET "http://localhost:8080/api/prompts/platforms" \
  -H "Authorization: Bearer YOUR_TOKEN"
```
