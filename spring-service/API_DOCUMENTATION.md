# Social Media Page Management API Documentation

## Overview

The Social Media Page Management API provides endpoints for connecting and managing social media pages through OAuth 2.0 authentication. The API supports Facebook, Instagram, and X (Twitter) platforms with secure token management and automatic token rotation.

## Base URL

```
Development: http://localhost:8080/api
Production: https://yourdomain.com/api
```

## Authentication

All endpoints require proper authentication. The seller ID is passed as a request parameter to identify the user context.

## Common Response Format

All API responses follow a consistent format:

```json
{
  "status": 200,
  "data": {
    // Response data varies by endpoint
  }
}
```

### Error Response Format

```json
{
  "status": 400,
  "data": {
    "errorCode": "INVALID_REQUEST",
    "message": "Error description",
    "platform": "facebook" // (if applicable)
  }
}
```

## OAuth Endpoints

### 1. Initiate OAuth Flow

Starts the OAuth authorization process for a specific platform.

**Endpoint:** `GET /oauth/{platform}/authorize`

**Parameters:**
- `platform` (path): Platform identifier (`facebook`, `instagram`, `x`)
- `sellerId` (query): Seller's unique identifier

**Example Request:**
```http
GET /api/oauth/facebook/authorize?sellerId=123
```

**Example Response:**
```json
{
  "status": 200,
  "data": {
    "authorizationUrl": "https://www.facebook.com/v18.0/dialog/oauth?client_id=...&redirect_uri=...&state=...",
    "state": "abc123def456",
    "platform": "facebook"
  }
}
```

**Usage Flow:**
1. Call this endpoint to get the authorization URL
2. Redirect user to the authorization URL
3. User authorizes the application on the platform
4. Platform redirects back to the callback URL

### 2. Handle OAuth Callback

Processes the OAuth callback with authorization code.

**Endpoint:** `GET /oauth/{platform}/callback`

**Parameters:**
- `platform` (path): Platform identifier
- `code` (query): OAuth authorization code from platform
- `state` (query): State parameter for validation
- `error` (query, optional): Error code if authorization failed
- `error_description` (query, optional): Error description

**Example Request:**
```http
GET /api/oauth/facebook/callback?code=abc123&state=def456
```

**Example Response:**
```json
{
  "status": 200,
  "data": {
    "platform": "facebook",
    "tokenReceived": true,
    "availablePages": [
      {
        "id": "page123",
        "name": "My Business Page",
        "category": "Business",
        "profilePictureUrl": "https://...",
        "accessToken": "page_access_token",
        "permissions": ["pages_manage_posts", "pages_read_engagement"]
      }
    ],
    "sellerId": 123
  }
}
```

### 3. Connect Specific Page

Connects a specific page after OAuth authorization.

**Endpoint:** `POST /oauth/{platform}/connect`

**Request Body:**
```json
{
  "sellerId": 123,
  "pageId": "page123",
  "accessToken": "page_access_token",
  "pageName": "My Business Page"
}
```

**Example Response:**
```json
{
  "status": 201,
  "data": {
    "connectedPage": {
      "id": 1,
      "status": "active",
      "platformIdentifier": "page123",
      "pageTitle": "My Business Page",
      "associatedMedia": null,
      "linkToPlatform": "https://facebook.com/page123",
      "platformId": 1,
      "sellerId": 123
    },
    "platform": "facebook",
    "status": "connected"
  }
}
```

### 4. Get Supported Platforms

Returns list of supported social media platforms.

**Endpoint:** `GET /oauth/platforms`

**Example Response:**
```json
{
  "status": 200,
  "data": ["facebook", "instagram", "x"]
}
```

## Page Management Endpoints

### 1. Get Connected Pages

Retrieves all connected pages for a seller.

**Endpoint:** `GET /pages`

**Parameters:**
- `sellerId` (query): Seller's unique identifier

**Example Request:**
```http
GET /api/pages?sellerId=123
```

**Example Response:**
```json
{
  "status": 200,
  "data": {
    "pages": [
      {
        "page": {
          "id": 1,
          "status": "active",
          "platformIdentifier": "page123",
          "pageTitle": "My Business Page",
          "associatedMedia": null,
          "linkToPlatform": "https://facebook.com/page123",
          "platformId": 1,
          "sellerId": 123
        },
        "platform": {
          "id": 1,
          "label": "facebook",
          "name": "Facebook"
        },
        "hasValidAccessToken": true,
        "hasValidRefreshToken": true
      }
    ],
    "totalCount": 1,
    "sellerId": 123
  }
}
```

### 2. Get Page Details

Retrieves details of a specific managed page.

**Endpoint:** `GET /pages/{pageId}`

**Parameters:**
- `pageId` (path): Managed page ID
- `sellerId` (query): Seller's unique identifier

**Example Request:**
```http
GET /api/pages/1?sellerId=123
```

**Example Response:**
```json
{
  "status": 200,
  "data": {
    "page": {
      "id": 1,
      "status": "active",
      "platformIdentifier": "page123",
      "pageTitle": "My Business Page",
      "associatedMedia": null,
      "linkToPlatform": "https://facebook.com/page123",
      "platformId": 1,
      "sellerId": 123
    },
    "status": {
      "isActive": true,
      "hasValidTokens": true,
      "lastTokenRefresh": "2024-01-15T10:30:00Z",
      "nextTokenExpiration": "2024-03-15T10:30:00Z",
      "requiresReauth": false
    },
    "sellerId": 123
  }
}
```

### 3. Disconnect Page

Disconnects a social media page and revokes tokens.

**Endpoint:** `DELETE /pages/{pageId}`

**Parameters:**
- `pageId` (path): Managed page ID
- `sellerId` (query): Seller's unique identifier

**Example Request:**
```http
DELETE /api/pages/1?sellerId=123
```

**Example Response:**
```json
{
  "status": 200,
  "data": {
    "message": "Page disconnected successfully",
    "pageId": 1,
    "sellerId": 123,
    "status": "disconnected"
  }
}
```

### 4. Refresh Page Tokens

Manually refreshes tokens for a specific page.

**Endpoint:** `POST /pages/{pageId}/refresh`

**Parameters:**
- `pageId` (path): Managed page ID
- `sellerId` (query): Seller's unique identifier

**Example Request:**
```http
POST /api/pages/1/refresh?sellerId=123
```

**Example Response:**
```json
{
  "status": 200,
  "data": {
    "message": "Tokens refreshed successfully",
    "pageId": 1,
    "sellerId": 123,
    "status": {
      "isActive": true,
      "hasValidTokens": true,
      "lastTokenRefresh": "2024-01-15T11:00:00Z",
      "nextTokenExpiration": "2024-03-15T11:00:00Z",
      "requiresReauth": false
    }
  }
}
```

### 5. Get Page Status

Retrieves connection status for a specific page.

**Endpoint:** `GET /pages/{pageId}/status`

**Parameters:**
- `pageId` (path): Managed page ID
- `sellerId` (query): Seller's unique identifier

**Example Request:**
```http
GET /api/pages/1/status?sellerId=123
```

**Example Response:**
```json
{
  "status": 200,
  "data": {
    "pageId": 1,
    "sellerId": 123,
    "status": {
      "isActive": true,
      "hasValidTokens": true,
      "lastTokenRefresh": "2024-01-15T10:30:00Z",
      "nextTokenExpiration": "2024-03-15T10:30:00Z",
      "requiresReauth": false
    }
  }
}
```

### 6. Get Pages by Platform

Retrieves pages filtered by platform.

**Endpoint:** `GET /pages/by-platform`

**Parameters:**
- `sellerId` (query): Seller's unique identifier
- `platform` (query, optional): Platform to filter by

**Example Request:**
```http
GET /api/pages/by-platform?sellerId=123&platform=facebook
```

**Example Response:**
```json
{
  "status": 200,
  "data": {
    "pages": [
      {
        "page": {
          "id": 1,
          "status": "active",
          "platformIdentifier": "page123",
          "pageTitle": "My Business Page",
          "associatedMedia": null,
          "linkToPlatform": "https://facebook.com/page123",
          "platformId": 1,
          "sellerId": 123
        },
        "platform": {
          "id": 1,
          "label": "facebook",
          "name": "Facebook"
        },
        "hasValidAccessToken": true,
        "hasValidRefreshToken": true
      }
    ],
    "totalCount": 1,
    "sellerId": 123,
    "platform": "facebook"
  }
}
```

## Error Codes and Status Codes

### HTTP Status Codes

- `200 OK`: Request successful
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid request parameters
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Access denied
- `404 Not Found`: Resource not found
- `429 Too Many Requests`: Rate limit exceeded
- `500 Internal Server Error`: Server error

### OAuth Error Codes

| Error Code | Description | Resolution |
|------------|-------------|------------|
| `INVALID_STATE` | OAuth state parameter invalid or expired | Restart OAuth flow |
| `INVALID_REQUEST` | Missing or invalid request parameters | Check request format |
| `UNSUPPORTED_PLATFORM` | Platform not supported | Use supported platform |
| `TOKEN_EXPIRED` | Access token has expired | Refresh tokens or re-authenticate |
| `RATE_LIMIT_EXCEEDED` | Too many requests | Wait and retry with backoff |
| `PAGE_ALREADY_CONNECTED` | Page is already connected | Use existing connection |
| `CONNECTION_ERROR` | Failed to connect page | Check platform configuration |
| `REFRESH_ERROR` | Token refresh failed | Re-authenticate required |

### Platform-Specific Error Codes

#### Facebook
- `FB_INVALID_CODE`: Invalid authorization code
- `FB_EXPIRED_CODE`: Authorization code expired
- `FB_INSUFFICIENT_PERMISSIONS`: Missing required permissions

#### Instagram
- `IG_INVALID_USER`: Invalid Instagram user
- `IG_BUSINESS_REQUIRED`: Business account required
- `IG_RATE_LIMITED`: Instagram API rate limit

#### X (Twitter)
- `X_INVALID_CREDENTIALS`: Invalid app credentials
- `X_SUSPENDED_USER`: User account suspended
- `X_SCOPE_INSUFFICIENT`: Insufficient OAuth scopes

## Integration Examples

### Frontend Integration (JavaScript)

#### 1. Initiate OAuth Flow

```javascript
async function initiateOAuth(platform, sellerId) {
  try {
    const response = await fetch(`/api/oauth/${platform}/authorize?sellerId=${sellerId}`);
    const data = await response.json();
    
    if (data.status === 200) {
      // Redirect user to authorization URL
      window.location.href = data.data.authorizationUrl;
    } else {
      console.error('OAuth initiation failed:', data.data.message);
    }
  } catch (error) {
    console.error('Error initiating OAuth:', error);
  }
}
```

#### 2. Handle OAuth Callback

```javascript
// This would typically be handled on your callback page
async function handleOAuthCallback() {
  const urlParams = new URLSearchParams(window.location.search);
  const code = urlParams.get('code');
  const state = urlParams.get('state');
  const error = urlParams.get('error');
  
  if (error) {
    console.error('OAuth error:', error);
    return;
  }
  
  // The callback is automatically handled by the backend
  // You can redirect to a success page or show available pages
}
```

#### 3. Connect Specific Page

```javascript
async function connectPage(platform, sellerId, pageId, accessToken, pageName) {
  try {
    const response = await fetch(`/api/oauth/${platform}/connect`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        sellerId: sellerId,
        pageId: pageId,
        accessToken: accessToken,
        pageName: pageName
      })
    });
    
    const data = await response.json();
    
    if (data.status === 201) {
      console.log('Page connected successfully:', data.data.connectedPage);
    } else {
      console.error('Page connection failed:', data.data.message);
    }
  } catch (error) {
    console.error('Error connecting page:', error);
  }
}
```

#### 4. Get Connected Pages

```javascript
async function getConnectedPages(sellerId) {
  try {
    const response = await fetch(`/api/pages?sellerId=${sellerId}`);
    const data = await response.json();
    
    if (data.status === 200) {
      return data.data.pages;
    } else {
      console.error('Failed to get pages:', data.data.message);
      return [];
    }
  } catch (error) {
    console.error('Error getting pages:', error);
    return [];
  }
}
```

#### 5. Disconnect Page

```javascript
async function disconnectPage(sellerId, pageId) {
  try {
    const response = await fetch(`/api/pages/${pageId}?sellerId=${sellerId}`, {
      method: 'DELETE'
    });
    
    const data = await response.json();
    
    if (data.status === 200) {
      console.log('Page disconnected successfully');
    } else {
      console.error('Failed to disconnect page:', data.data.message);
    }
  } catch (error) {
    console.error('Error disconnecting page:', error);
  }
}
```

### React Integration Example

```jsx
import React, { useState, useEffect } from 'react';

const SocialMediaManager = ({ sellerId }) => {
  const [pages, setPages] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadConnectedPages();
  }, [sellerId]);

  const loadConnectedPages = async () => {
    try {
      const response = await fetch(`/api/pages?sellerId=${sellerId}`);
      const data = await response.json();
      
      if (data.status === 200) {
        setPages(data.data.pages);
      }
    } catch (error) {
      console.error('Error loading pages:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleConnectPlatform = (platform) => {
    window.location.href = `/api/oauth/${platform}/authorize?sellerId=${sellerId}`;
  };

  const handleDisconnectPage = async (pageId) => {
    try {
      const response = await fetch(`/api/pages/${pageId}?sellerId=${sellerId}`, {
        method: 'DELETE'
      });
      
      if (response.ok) {
        await loadConnectedPages(); // Reload pages
      }
    } catch (error) {
      console.error('Error disconnecting page:', error);
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      <h2>Connected Social Media Pages</h2>
      
      <div>
        <button onClick={() => handleConnectPlatform('facebook')}>
          Connect Facebook
        </button>
        <button onClick={() => handleConnectPlatform('instagram')}>
          Connect Instagram
        </button>
        <button onClick={() => handleConnectPlatform('x')}>
          Connect X (Twitter)
        </button>
      </div>

      <div>
        {pages.map(pageDetail => (
          <div key={pageDetail.page.id} className="page-card">
            <h3>{pageDetail.page.pageTitle}</h3>
            <p>Platform: {pageDetail.platform.name}</p>
            <p>Status: {pageDetail.page.status}</p>
            <p>Valid Tokens: {pageDetail.hasValidAccessToken ? 'Yes' : 'No'}</p>
            
            <button onClick={() => handleDisconnectPage(pageDetail.page.id)}>
              Disconnect
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};

export default SocialMediaManager;
```

## Rate Limiting

The API implements rate limiting to prevent abuse:

- **OAuth endpoints**: 30 requests per minute per IP
- **Page management endpoints**: 60 requests per minute per IP
- **Burst capacity**: 100 requests

When rate limit is exceeded, the API returns:

```json
{
  "status": 429,
  "data": {
    "errorCode": "RATE_LIMIT_EXCEEDED",
    "message": "Too many requests. Please try again later.",
    "retryAfter": 60
  }
}
```

## Security Considerations

1. **HTTPS Required**: All production endpoints must use HTTPS
2. **State Parameter**: OAuth flows use cryptographically secure state parameters
3. **Token Encryption**: All tokens are encrypted before database storage
4. **Session Management**: OAuth state is tied to user sessions
5. **CORS Policy**: Configure appropriate CORS policies for your frontend
6. **Input Validation**: All inputs are validated and sanitized

## Testing

### Using cURL

#### Initiate OAuth Flow
```bash
curl -X GET "http://localhost:8080/api/oauth/facebook/authorize?sellerId=123"
```

#### Get Connected Pages
```bash
curl -X GET "http://localhost:8080/api/pages?sellerId=123"
```

#### Disconnect Page
```bash
curl -X DELETE "http://localhost:8080/api/pages/1?sellerId=123"
```

### Using Postman

Import the following collection to test all endpoints:

```json
{
  "info": {
    "name": "Social Media Page Management API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "OAuth",
      "item": [
        {
          "name": "Initiate OAuth",
          "request": {
            "method": "GET",
            "url": {
              "raw": "{{baseUrl}}/oauth/{{platform}}/authorize?sellerId={{sellerId}}",
              "host": ["{{baseUrl}}"],
              "path": ["oauth", "{{platform}}", "authorize"],
              "query": [{"key": "sellerId", "value": "{{sellerId}}"}]
            }
          }
        }
      ]
    }
  ],
  "variable": [
    {"key": "baseUrl", "value": "http://localhost:8080/api"},
    {"key": "sellerId", "value": "123"},
    {"key": "platform", "value": "facebook"}
  ]
}
```

## Support and Troubleshooting

For API support:

1. **Check logs**: Enable debug logging for detailed error information
2. **Validate configuration**: Ensure all OAuth settings are correct
3. **Test in development**: Always test OAuth flows in development first
4. **Monitor rate limits**: Implement proper retry logic with exponential backoff
5. **Handle errors gracefully**: Implement proper error handling in your frontend

## Changelog

### Version 1.0.0
- Initial API release
- Support for Facebook, Instagram, and X platforms
- OAuth 2.0 authentication flows
- Automatic token rotation
- Comprehensive error handling