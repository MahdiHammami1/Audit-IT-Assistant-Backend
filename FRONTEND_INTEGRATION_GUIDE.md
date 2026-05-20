# Frontend Integration Guide - Microsoft Entra ID

## Overview

This guide shows how to integrate Microsoft Entra ID authentication in your React frontend.

## Installation

### 1. Copy the TypeScript Client

Copy `FRONTEND_ENTRA_CLIENT.ts` to your React project:

```bash
cp FRONTEND_ENTRA_CLIENT.ts src/services/EntraIdAuthService.ts
```

### 2. Install Dependencies (if needed)

The service only uses native Fetch API, no additional dependencies required.

## Usage Examples

### Example 1: Login Component

```tsx
// src/components/LoginPage.tsx
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { EntraIdAuthService } from '../services/EntraIdAuthService';

const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const authService = new EntraIdAuthService('/api');

  const handleEntraLogin = async () => {
    try {
      setLoading(true);
      setError(null);
      const { loginUrl } = await authService.getLoginUrl();
      // Redirect to Microsoft Entra ID
      window.location.href = loginUrl;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed');
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <h1>AuditIT - Login</h1>
      
      {error && <div className="error-message">{error}</div>}
      
      <button 
        onClick={handleEntraLogin} 
        disabled={loading}
        className="btn btn-primary btn-lg"
      >
        {loading ? 'Redirecting...' : 'Sign in with Microsoft'}
      </button>
    </div>
  );
};

export default LoginPage;
```

### Example 2: Callback Component

```tsx
// src/components/AuthCallback.tsx
import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { EntraIdAuthService } from '../services/EntraIdAuthService';

const AuthCallback: React.FC = () => {
  const navigate = useNavigate();
  const [error, setError] = React.useState<string | null>(null);
  const authService = new EntraIdAuthService('/api');

  useEffect(() => {
    const handleCallback = async () => {
      try {
        // Extract authorization code from URL
        const params = new URLSearchParams(window.location.search);
        const code = params.get('code');
        
        if (!code) {
          setError('Authorization code not found in URL');
          setTimeout(() => navigate('/login'), 3000);
          return;
        }

        // Exchange code for JWT token
        const authResponse = await authService.handleCallback(code);
        
        // Store authentication data
        authService.storeAuthData(authResponse);
        
        // Log successful login
        console.log('Login successful:', authResponse.user);
        
        // Redirect to dashboard
        navigate('/dashboard', { replace: true });
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Authentication failed');
        setTimeout(() => navigate('/login'), 3000);
      }
    };

    handleCallback();
  }, [navigate]);

  if (error) {
    return (
      <div className="callback-error">
        <h2>Authentication Error</h2>
        <p>{error}</p>
        <p>Redirecting to login...</p>
      </div>
    );
  }

  return (
    <div className="callback-loading">
      <h2>Processing authentication...</h2>
      <p>Please wait while we complete your login.</p>
    </div>
  );
};

export default AuthCallback;
```

### Example 3: Protected Route Component

```tsx
// src/components/ProtectedRoute.tsx
import React from 'react';
import { Navigate } from 'react-router-dom';
import { EntraIdAuthService } from '../services/EntraIdAuthService';

interface ProtectedRouteProps {
  component: React.ComponentType<any>;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ component: Component }) => {
  const authService = new EntraIdAuthService('/api');
  
  if (!authService.isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }

  return <Component />;
};

export default ProtectedRoute;
```

### Example 4: User Profile Component

```tsx
// src/components/UserProfile.tsx
import React from 'react';
import { EntraIdAuthService } from '../services/EntraIdAuthService';

const UserProfile: React.FC = () => {
  const authService = new EntraIdAuthService('/api');
  const user = authService.getStoredUser();

  if (!user) {
    return <div>No user information available</div>;
  }

  return (
    <div className="user-profile">
      <h2>User Profile</h2>
      <div className="profile-info">
        <p><strong>Name:</strong> {user.fullName}</p>
        <p><strong>Email:</strong> {user.email}</p>
        <p><strong>ID:</strong> {user.id}</p>
      </div>
    </div>
  );
};

export default UserProfile;
```

### Example 5: Logout Component

```tsx
// src/components/LogoutButton.tsx
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { EntraIdAuthService } from '../services/EntraIdAuthService';

const LogoutButton: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = React.useState(false);
  const authService = new EntraIdAuthService('/api');

  const handleLogout = async () => {
    try {
      setLoading(true);
      await authService.logout();
      navigate('/login', { replace: true });
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <button 
      onClick={handleLogout}
      disabled={loading}
      className="btn btn-secondary"
    >
      {loading ? 'Logging out...' : 'Logout'}
    </button>
  );
};

export default LogoutButton;
```

### Example 6: App Routing Setup

```tsx
// src/App.tsx
import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './components/LoginPage';
import AuthCallback from './components/AuthCallback';
import Dashboard from './components/Dashboard';
import ProtectedRoute from './components/ProtectedRoute';

const App: React.FC = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/auth/callback" element={<AuthCallback />} />
        <Route path="/dashboard" element={<ProtectedRoute component={Dashboard} />} />
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
};

export default App;
```

### Example 7: Making Authenticated API Calls

```tsx
// src/components/ProfilesList.tsx
import React, { useEffect, useState } from 'react';
import { EntraIdAuthService } from '../services/EntraIdAuthService';

interface Profile {
  id: string;
  email: string;
  fullName: string;
}

const ProfilesList: React.FC = () => {
  const [profiles, setProfiles] = useState<Profile[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const authService = new EntraIdAuthService('/api');

  useEffect(() => {
    const fetchProfiles = async () => {
      try {
        setLoading(true);
        // Make authenticated API call
        const data = await authService.authenticatedFetch<Profile[]>('/profiles');
        setProfiles(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load profiles');
      } finally {
        setLoading(false);
      }
    };

    fetchProfiles();
  }, []);

  if (loading) return <div>Loading profiles...</div>;
  if (error) return <div className="error">Error: {error}</div>;

  return (
    <div className="profiles-list">
      <h2>Profiles</h2>
      <table>
        <thead>
          <tr>
            <th>Email</th>
            <th>Full Name</th>
          </tr>
        </thead>
        <tbody>
          {profiles.map((profile) => (
            <tr key={profile.id}>
              <td>{profile.email}</td>
              <td>{profile.fullName}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default ProfilesList;
```

## Context API Integration (Optional)

For better state management, use React Context:

```tsx
// src/context/AuthContext.tsx
import React, { createContext, useContext } from 'react';
import { EntraIdAuthService } from '../services/EntraIdAuthService';

interface AuthUser {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
}

interface AuthContextType {
  user: AuthUser | null;
  isAuthenticated: boolean;
  logout: () => Promise<void>;
  storeAuthData: (data: any) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const authService = new EntraIdAuthService('/api');
  const [user, setUser] = React.useState<AuthUser | null>(
    authService.getStoredUser()
  );

  const logout = async () => {
    await authService.logout();
    setUser(null);
  };

  const storeAuthData = (data: any) => {
    authService.storeAuthData(data);
    setUser(data.user);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: authService.isAuthenticated(),
        logout,
        storeAuthData,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};
```

## Environment Configuration

Create `.env` in your React project:

```
# Local Development
REACT_APP_API_BASE_URL=http://localhost:8080/api
REACT_APP_AUTH_CALLBACK_PATH=/auth/callback

# Production
# REACT_APP_API_BASE_URL=https://api.yourdomain.com
```

## Security Best Practices

1. **Never expose backend secrets** - Client secret stays on backend only
2. **Use httpOnly Cookies** - Consider storing JWT in httpOnly cookies instead of localStorage
3. **HTTPS Only** - Always use HTTPS in production
4. **CORS** - Ensure backend CORS is properly configured
5. **Token Refresh** - Implement refresh token flow for long sessions
6. **Logout** - Clear all sensitive data on logout

## Troubleshooting

### Issue: "CORS error" when calling backend

**Solution:**
- Ensure backend has frontend URL in CORS configuration
- Check that `cors.allowed-origins` includes your frontend domain

### Issue: "Token not sent in requests"

**Solution:**
- Check browser DevTools Network tab for `Authorization` header
- Verify `getAuthHeader()` is being called
- Ensure token is properly stored after login

### Issue: "Infinite redirect loop"

**Solution:**
- Check that redirect URI in Entra ID exactly matches backend configuration
- Verify `AUTH_ENTRA_REDIRECT_URI` environment variable
- Clear browser cache and cookies

## Next Steps

1. Implement token refresh mechanism
2. Add user role management
3. Implement audit logging for authentication events
4. Set up MFA (Multi-Factor Authentication)
5. Add password-less authentication options

## Support

For detailed backend configuration, see `README_ENTRA_ID.md` in the backend project.

