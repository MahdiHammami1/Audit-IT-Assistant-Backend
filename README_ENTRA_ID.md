# Microsoft Entra ID Integration - Documentation

## Overview

This document describes the Microsoft Entra ID (Azure AD) OAuth2 authentication integration for the AuditIT backend.

The implementation provides a complete OAuth2 flow for user authentication via Microsoft Entra ID while maintaining a stateless JWT-based session management on the backend.

## Architecture

### Flow Diagram

```
┌─────────────┐         ┌──────────────────┐         ┌──────────────┐
│   Client    │         │  AuditIT Backend │         │  Entra ID    │
│  (Frontend) │         │   Spring Boot    │         │   (Azure AD) │
└─────────────┘         └──────────────────┘         └──────────────┘
      │                         │                           │
      │                         │                           │
      │----GET /entra/login────→│                           │
      │                         │                           │
      │←──Authorization URL─────│                           │
      │                         │                           │
      │─────(redirect)─────────────────────────────────→│
      │                         │                           │
      │                         │     (user logs in)        │
      │                         │←──Auth Code + State──────│
      │←──(with code)──────────────────────────────────────│
      │                         │                           │
      │─GET /entra/callback?code=xxx─→│                    │
      │                         │                           │
      │                    /backend| exchanges code│
      │                    internally│                       │
      │                         │──Token Exchange Request──→│
      │                         │                           │
      │                         │←──Access + ID Tokens─────│
      │                         │                           │
      │                    /backend|
      │                    /creates JWT│
      │                         │                           │
      │←──JWT Token + User Info──│                           │
      │                         │                           │
      └─(store JWT)─────────────────────────────────────────┘
```

### Key Components

1. **EntraIdProperties** - Configuration bean for Entra ID credentials and endpoints
2. **EntraIdAuthService** - Main service handling OAuth2 flow
3. **AuthEntraController** - REST endpoints for login/callback/logout
4. **SecurityConfig** - Spring Security configuration allowing public Entra ID endpoints
5. **DTOs** - Data transfer objects for token and user info

## Setup Guide

### Prerequisites

1. Azure AD tenant with admin access
2. Application registration in Azure AD
3. Client ID and Client Secret from app registration
4. Configured Redirect URI (callback endpoint)

### Step 1: Register Application in Azure AD

1. Sign in to Azure Portal (https://portal.azure.com)
2. Navigate to **Azure Active Directory** → **App registrations**
3. Click **New registration**
4. Configure:
   - **Name**: AuditIT Backend (or your preferred name)
   - **Supported account types**: Choose based on your tenant setup
   - **Redirect URI**: 
     - Platform: Web
     - URI: `http://localhost:8080/api/entra/callback` (for local development)
     - For production: `https://yourdomain.com/api/entra/callback`

5. Click **Register**

### Step 2: Configure Client Credentials

1. In your app registration, go to **Certificates & secrets**
2. Click **New client secret**
3. Set expiration (recommended: 24 months for production)
4. Copy the **Value** (Client Secret) - you'll need this immediately
5. Note the **Client ID** from the Overview tab

### Step 3: Configure API Permissions

1. In your app registration, go to **API permissions**
2. Click **Add a permission**
3. Select **Microsoft Graph**
4. Select **Delegated permissions**
5. Search and add:
   - `openid`
   - `profile`
   - `email`
6. Click **Grant admin consent for [Tenant]**

### Step 4: Get Your Metadata URL

1. In your app registration Overview, find the **Endpoints** button
2. Click it to view available endpoints
3. Copy the **OpenID Connect metadata document** URL
   - Format: `https://login.microsoftonline.com/{tenant}/v2.0/.well-known/openid-configuration`
   - Or for multi-tenant: `https://login.microsoftonline.com/common/v2.0/.well-known/openid-configuration`

### Step 5: Set Environment Variables

Create a `.env` file in your project root or set these in your deployment environment:

```bash
# Microsoft Entra ID Configuration
AUTH_ENTRA_CLIENT_ID=<your-client-id>
AUTH_ENTRA_CLIENT_SECRET=<your-client-secret>
AUTH_ENTRA_REDIRECT_URI=http://localhost:8080/api/entra/callback
AUTH_ENTRA_POST_LOGOUT_REDIRECT_URI=http://localhost:5173
AUTH_ENTRA_METADATA_URL=https://login.microsoftonline.com/{tenant-id}/v2.0/.well-known/openid-configuration
AUTH_ENTRA_TENANT_ID=<your-tenant-id>  # Optional

# Other existing configs
JWT_SECRET=your-jwt-secret-key
MONGODB_URI=your-mongodb-uri
# ... other configs
```

**Important Security Notes:**
- Never commit `.env` file to version control
- Use strong JWT secret (minimum 32 characters)
- Client Secret should only be on backend, never exposed to frontend
- Use HTTPS in production

### Step 6: Enable Security in application.properties

By default, security is disabled to avoid build issues. Enable it when ready:

```properties
security.enabled=true
```

Or set via environment variable:
```bash
export SECURITY_ENABLED=true
```

## API Endpoints

### 1. Initiate Login

**GET** `/api/entra/login`

Returns the Microsoft Entra ID authorization URL.

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Entra ID login URL generated",
  "data": {
    "loginUrl": "https://login.microsoftonline.com/...?client_id=...&redirect_uri=...&response_type=code&scope=openid%20profile%20email&state=..."
  }
}
```

**Frontend Usage:**
```javascript
// 1. Get login URL
const response = await fetch('/api/entra/login');
const { data } = await response.json();

// 2. Redirect user to Entra ID
window.location.href = data.loginUrl;
```

### 2. OAuth2 Callback

**GET** `/api/entra/callback?code=<authorization_code>&state=<state>`

Processes the authorization code from Entra ID. This endpoint is called by Entra ID after user approves permissions.

**Query Parameters:**
- `code` (required): Authorization code from Entra ID
- `state` (optional): State parameter for CSRF protection

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Authentication successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3OC1hYmNkLWVmZ2gtaWprbCIsImVtYWlsIjoidXNlckBlbWFpbC5jb20iLCJpYXQiOjE2NzY2MzA4MzMsImV4cCI6MTY3NjcxNzIzM30.abcdef123456",
    "user": {
      "id": "12345678-abcd-efgh-ijkl",
      "email": "user@email.com",
      "firstName": "John",
      "lastName": "Doe",
      "fullName": "John Doe"
    },
    "message": "Authentication successful via Microsoft Entra ID"
  }
}
```

**Frontend Usage:**
```javascript
// After redirect back to your frontend with code
const code = new URLSearchParams(window.location.search).get('code');

// Fetch callback to backend
const response = await fetch(`/api/entra/callback?code=${code}`);
const { data, success } = await response.json();

if (success) {
  // Store JWT token
  localStorage.setItem('authToken', data.token);
  // Store user info
  localStorage.setItem('user', JSON.stringify(data.user));
  // Redirect to dashboard
  window.location.href = '/dashboard';
}
```

**Error Responses:**
- `400 Bad Request`: Code parameter missing
- `401 Unauthorized`: Code exchange failed
- `500 Internal Server Error`: Server error during authentication

### 3. Logout

**POST** `/api/entra/logout`

Logs out the authenticated user.

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Logout successful",
  "data": null
}
```

**Frontend Usage:**
```javascript
// Clear stored tokens
localStorage.removeItem('authToken');
localStorage.removeItem('user');

// Call backend logout endpoint (optional)
await fetch('/api/entra/logout', { method: 'POST' });

// Redirect to login
window.location.href = '/login';
```

## Usage with Protected Endpoints

### Setting Authorization Header

After successful authentication, include the JWT token in all API requests:

```javascript
// Example: GET /api/profiles
const response = await fetch('/api/profiles', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
  }
});
```

### Token Validation

The backend automatically validates JWT tokens on all protected endpoints using `JwtAuthenticationFilter`.

## Security Considerations

### 1. Token Storage on Frontend
- **Recommended**: Store tokens in httpOnly cookies (more secure)
- **Current**: localStorage (convenient but XSS-vulnerable)
- For production, consider:
  ```javascript
  // Set token as httpOnly cookie
  // Backend should set via: 
  // response.addCookie(new HttpCookie("authToken", jwt) { HttpOnly = true });
  ```

### 2. Token Expiration
- JWT tokens expire after 24 hours (configurable via `jwt.expiration`)
- Implement refresh token flow for extended sessions
- Automatic logout after token expiration

### 3. HTTPS in Production
- Always use HTTPS for all Entra ID communication
- Set secure flag on cookies
- Use strong TLS 1.2+

### 4. Email Verification
- Users authenticated via Entra ID are automatically marked as verified
- Email uniqueness is enforced at database level

### 5. Cross-Tenant Setup
- For multi-tenant deployments, the common endpoint is used by default
- For single-tenant, specify `AUTH_ENTRA_TENANT_ID` for better performance

## Troubleshooting

### Issue: "Entra ID is not properly configured"

**Solution**: Ensure all required environment variables are set:
```bash
echo $AUTH_ENTRA_CLIENT_ID
echo $AUTH_ENTRA_CLIENT_SECRET
echo $AUTH_ENTRA_REDIRECT_URI
echo $AUTH_ENTRA_METADATA_URL
```

### Issue: "Failed to fetch Entra ID metadata"

**Possible causes:**
- Incorrect metadata URL
- Network connectivity issue
- Entra ID service down

**Solution**: 
- Verify metadata URL is accessible: `curl https://login.microsoftonline.com/{tenant}/v2.0/.well-known/openid-configuration`
- Check network proxies
- Verify firewall rules

### Issue: "Email not found in user information"

**Possible causes:**
- User doesn't have email configured in Azure AD
- Scope 'email' not granted

**Solution:**
- Add email to user profile in Azure AD
- Verify API permissions in app registration
- Grant admin consent again

### Issue: User Created Multiple Times

**Possible causes:**
- Network retry on code exchange
- Multiple authentication attempts with same code

**Solution:**
- Implement idempotency key in code exchange
- This is handled automatically in current implementation

## Configuration Examples

### Development Environment (.env)

```bash
# Server
PORT=8080

# Database
MONGODB_URI=mongodb://localhost:27017/auditit-dev

# JWT
JWT_SECRET=your-development-secret-min-32-characters-long-1234567890

# Microsoft Entra ID (Development Tenant)
AUTH_ENTRA_CLIENT_ID=12345678-1234-1234-1234-123456789012
AUTH_ENTRA_CLIENT_SECRET=your-client-secret-here
AUTH_ENTRA_REDIRECT_URI=http://localhost:8080/api/entra/callback
AUTH_ENTRA_POST_LOGOUT_REDIRECT_URI=http://localhost:5173
AUTH_ENTRA_METADATA_URL=https://login.microsoftonline.com/common/v2.0/.well-known/openid-configuration
AUTH_ENTRA_TENANT_ID=12345678-1234-1234-1234-123456789012

# Security
SECURITY_ENABLED=true

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

### Production Environment

```bash
# Server  
PORT=8080

# Database
MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/auditit

# JWT (Use strong random value)
JWT_SECRET=generate-32-character-random-string-here-7d9f2k8m

# Microsoft Entra ID (Production Tenant)
AUTH_ENTRA_CLIENT_ID=abcdefgh-abcd-efgh-ijkl-abcdefghijkl
AUTH_ENTRA_CLIENT_SECRET=your-production-client-secret
AUTH_ENTRA_REDIRECT_URI=https://api.yourdomain.com/api/entra/callback
AUTH_ENTRA_POST_LOGOUT_REDIRECT_URI=https://yourdomain.com
AUTH_ENTRA_METADATA_URL=https://login.microsoftonline.com/{tenant-id}/v2.0/.well-known/openid-configuration
AUTH_ENTRA_TENANT_ID=your-tenant-id

# Security
SECURITY_ENABLED=true

# CORS (restrict to your domain)
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

## Advanced Configuration

### Using Metadata URL with Single Tenant

For better performance in single-tenant scenarios:

```bash
# Replace {tenant-id} with your actual tenant ID
AUTH_ENTRA_METADATA_URL=https://login.microsoftonline.com/{tenant-id}/v2.0/.well-known/openid-configuration
```

### Customizing Token Expiration

In `application.properties`:
```properties
# 24 hours in milliseconds
jwt.expiration=86400000

# Refresh token expiration (7 days)
jwt.refresh-expiration=604800000
```

### Implementing Token Refresh (Future Enhancement)

```java
// In AuthEntraService.java - Future implementation
public String refreshToken(String refreshToken) throws AuthenticationException {
    // Exchange refresh token for new access token
    // Update JWT generation to include refresh token claim
}
```

## Next Steps

1. **Configure Frontend**: Create login component that redirects to `/api/entra/login`
2. **Implement Refresh Token**: Add token refresh mechanism for long sessions
3. **Add User Roles**: Map Azure AD groups to local application roles
4. **Audit Logging**: Log all authentication events
5. **MFA Configuration**: Enable multi-factor authentication in Azure AD
6. **Conditional Access**: Set up Azure AD Conditional Access policies

## Support

For issues or questions:
1. Check Azure AD application logs
2. Review Spring Security logs: `logging.level.org.springframework.security=DEBUG`
3. Verify all environment variables are set correctly
4. Ensure redirect URIs match exactly in Azure AD and application config

