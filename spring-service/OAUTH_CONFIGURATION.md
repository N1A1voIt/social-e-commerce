# OAuth Configuration Guide

This document explains how to configure OAuth settings for the Social Media Page Management system.

## Overview

The system supports OAuth 2.0 authentication for multiple social media platforms:
- Facebook
- Instagram  
- X (Twitter)

## Environment Setup

### 1. Copy Environment Template

```bash
cp .env.example .env
```

### 2. Configure Required Environment Variables

#### Database Configuration
```bash
DATABASE_URL=jdbc:postgresql://localhost:5436/postgres
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
```

#### Base URL
Set the base URL for OAuth callbacks:
```bash
BASE_URL=http://localhost:8080  # For development
BASE_URL=https://yourdomain.com  # For production
```

#### Token Encryption Key
**CRITICAL**: Generate a secure encryption key (32+ characters):
```bash
TOKEN_ENCRYPTION_KEY=YourSecureRandomEncryptionKey123456789012
```

⚠️ **Security Warning**: Never use default keys in production!

### 3. Platform-Specific Configuration

#### Facebook OAuth
1. Create a Facebook App at [Facebook Developers](https://developers.facebook.com/)
2. Configure OAuth redirect URI: `{BASE_URL}/api/oauth/facebook/callback`
3. Set environment variables:
```bash
FACEBOOK_CLIENT_ID=your-facebook-app-id
FACEBOOK_CLIENT_SECRET=your-facebook-app-secret
```

#### Instagram OAuth
1. Create an Instagram Basic Display App
2. Configure OAuth redirect URI: `{BASE_URL}/api/oauth/instagram/callback`
3. Set environment variables:
```bash
INSTAGRAM_CLIENT_ID=your-instagram-app-id
INSTAGRAM_CLIENT_SECRET=your-instagram-app-secret
```

#### X (Twitter) OAuth
1. Create a Twitter App at [Twitter Developer Portal](https://developer.twitter.com/)
2. Enable OAuth 2.0 with PKCE
3. Configure OAuth redirect URI: `{BASE_URL}/api/oauth/x/callback`
4. Set environment variables:
```bash
X_CLIENT_ID=your-x-app-client-id
X_CLIENT_SECRET=your-x-app-client-secret
```

## Configuration Profiles

### Development Profile (`application-dev.properties`)
- Enables SQL logging
- Uses relaxed security settings
- Shorter token rotation intervals
- Debug logging enabled

### Production Profile (`application-prod.properties`)
- Disables SQL logging
- Stricter security settings
- Longer token rotation intervals
- Minimal logging

### Activate Profile
```bash
# Development
SPRING_PROFILES_ACTIVE=dev

# Production
SPRING_PROFILES_ACTIVE=prod
```

## Advanced Configuration

### Token Rotation Settings
```bash
# Enable/disable automatic token rotation
OAUTH_TOKEN_ROTATION_ENABLED=true

# Rotation frequency (milliseconds)
OAUTH_TOKEN_ROTATION_RATE=3600000  # 1 hour

# Initial delay before first rotation
OAUTH_TOKEN_ROTATION_DELAY=60000   # 1 minute

# Hours before expiration to trigger rotation
OAUTH_TOKEN_EXPIRATION_THRESHOLD=24
```

### Rate Limiting
```bash
# Enable/disable rate limiting
OAUTH_RATE_LIMIT_ENABLED=true

# Requests per minute per IP
OAUTH_RATE_LIMIT_RPM=60

# Burst capacity
OAUTH_RATE_LIMIT_BURST=100
```

### Retry Configuration
```bash
# Maximum retry attempts for failed requests
OAUTH_RETRY_MAX_ATTEMPTS=3

# Initial delay between retries (milliseconds)
OAUTH_RETRY_INITIAL_DELAY=1000

# Backoff multiplier
OAUTH_RETRY_MULTIPLIER=2.0

# Maximum delay between retries
OAUTH_RETRY_MAX_DELAY=10000
```

### Security Settings
```bash
# OAuth state parameter expiration (minutes)
OAUTH_STATE_EXPIRATION=10

# Enable CSRF protection
OAUTH_CSRF_ENABLED=true
```

## Configuration Validation

The application validates all OAuth configurations on startup:

- ✅ Token encryption key security
- ✅ Platform client credentials
- ✅ URL format validation
- ✅ Scheduled task settings
- ✅ Rate limiting parameters

If validation fails, the application will not start and will log specific error messages.

## Troubleshooting

### Common Issues

1. **"Token encryption key must be provided"**
   - Set `TOKEN_ENCRYPTION_KEY` environment variable
   - Ensure key is at least 32 characters long

2. **"Client ID is missing or using default value"**
   - Replace placeholder values in environment variables
   - Ensure platform apps are properly configured

3. **"Redirect URI is not a valid URL"**
   - Check `BASE_URL` environment variable
   - Ensure URLs match platform app configurations

4. **OAuth callback errors**
   - Verify redirect URIs match exactly in platform apps
   - Check that `BASE_URL` is accessible from the internet (for production)

### Debug Mode

Enable debug logging for OAuth operations:
```bash
logging.level.com.itu.socialcom.demo.socialmedia=DEBUG
```

## Security Best Practices

1. **Never commit secrets to version control**
2. **Use strong, unique encryption keys**
3. **Regularly rotate OAuth app secrets**
4. **Monitor OAuth usage and errors**
5. **Use HTTPS in production**
6. **Implement proper CORS policies**
7. **Regular security audits**

## Platform-Specific Notes

### Facebook
- Requires app review for certain permissions
- Page access tokens have different expiration rules
- Business verification may be required

### Instagram
- Limited to Instagram Basic Display API
- Requires Facebook app configuration
- User must approve each connection

### X (Twitter)
- Uses OAuth 2.0 with PKCE
- Rate limits are strictly enforced
- API access levels affect available endpoints

## Support

For configuration issues:
1. Check application logs for specific error messages
2. Verify platform app configurations
3. Test OAuth flows in development environment first
4. Consult platform-specific documentation