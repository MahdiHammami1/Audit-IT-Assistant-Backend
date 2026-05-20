/**
 * ============================================================
 * AUDITIT FRONTEND - ENTRA ID AUTHENTICATION SERVICE
 * ============================================================
 *
 * TypeScript/JavaScript service for handling Microsoft Entra ID authentication
 * on the client side (React/Vue/Angular, etc.)
 *
 * Usage in React:
 * ```tsx
 * import { EntraIdAuthService } from './services/EntraIdAuthService';
 *
 * const authService = new EntraIdAuthService('http://localhost:8080/api');
 *
 * // In login component:
 * const handleLogin = async () => {
 *   const { loginUrl } = await authService.getLoginUrl();
 *   window.location.href = loginUrl;
 * };
 *
 * // In callback component (after redirect from Entra ID):
 * useEffect(() => {
 *   const params = new URLSearchParams(window.location.search);
 *   const code = params.get('code');
 *
 *   if (code) {
 *     authService.handleCallback(code).then(auth => {
 *       localStorage.setItem('token', auth.token);
 *       localStorage.setItem('user', JSON.stringify(auth.user));
 *       navigate('/dashboard');
 *     });
 *   }
 * }, []);
 * ```
 */

interface EntraUserInfo {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
}

interface EntraAuthResponse {
  token: string;
  user: EntraUserInfo;
  message: string;
  redirectUrl?: string;
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp?: string;
}

/**
 * Service for handling Microsoft Entra ID authentication
 */
export class EntraIdAuthService {
  private baseUrl: string;

  constructor(baseUrl: string = '/api') {
    this.baseUrl = baseUrl;
  }

  /**
   * Get Microsoft Entra ID login URL
   *
   * @returns {Promise<{loginUrl: string}>} Login URL to redirect user to
   */
  async getLoginUrl(): Promise<{ loginUrl: string }> {
    const response = await fetch(`${this.baseUrl}/entra/login`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`Failed to get login URL: ${response.statusText}`);
    }

    const apiResponse: ApiResponse<{ loginUrl: string }> = await response.json();

    if (!apiResponse.success) {
      throw new Error(apiResponse.message);
    }

    return apiResponse.data;
  }

  /**
   * Handle OAuth2 callback after Entra ID redirect
   *
   * @param {string} code - Authorization code from Entra ID
   * @returns {Promise<EntraAuthResponse>} Authentication response with token and user info
   */
  async handleCallback(code: string): Promise<EntraAuthResponse> {
    if (!code) {
      throw new Error('Authorization code is missing');
    }

    const response = await fetch(`${this.baseUrl}/entra/callback?code=${code}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`Authentication failed: ${response.statusText}`);
    }

    const apiResponse: ApiResponse<EntraAuthResponse> = await response.json();

    if (!apiResponse.success) {
      throw new Error(apiResponse.message);
    }

    return apiResponse.data;
  }

  /**
   * Logout user
   *
   * @returns {Promise<void>}
   */
  async logout(): Promise<void> {
    try {
      await fetch(`${this.baseUrl}/entra/logout`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.getStoredToken()}`,
        },
      });
    } catch (error) {
      console.error('Logout error:', error);
    }

    // Clear stored tokens and user info
    this.clearAuthStorage();
  }

  /**
   * Get stored JWT token
   *
   * @returns {string | null} Stored token or null
   */
  getStoredToken(): string | null {
    return localStorage.getItem('authToken');
  }

  /**
   * Get stored user info
   *
   * @returns {EntraUserInfo | null} Stored user info or null
   */
  getStoredUser(): EntraUserInfo | null {
    const userJson = localStorage.getItem('user');
    return userJson ? JSON.parse(userJson) : null;
  }

  /**
   * Check if user is authenticated
   *
   * @returns {boolean} True if user has stored token
   */
  isAuthenticated(): boolean {
    return !!this.getStoredToken();
  }

  /**
   * Store authentication tokens and user info
   *
   * @param {EntraAuthResponse} authResponse - Authentication response from backend
   */
  storeAuthData(authResponse: EntraAuthResponse): void {
    localStorage.setItem('authToken', authResponse.token);
    localStorage.setItem('user', JSON.stringify(authResponse.user));
    localStorage.setItem('authTime', new Date().toISOString());
  }

  /**
   * Clear stored authentication data
   */
  clearAuthStorage(): void {
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');
    localStorage.removeItem('authTime');
  }

  /**
   * Get authorization header for API calls
   *
   * @returns {{'Authorization': string} | {}} Authorization header or empty object if not authenticated
   */
  getAuthHeader(): { 'Authorization': string } | {} {
    const token = this.getStoredToken();
    return token ? { 'Authorization': `Bearer ${token}` } : {};
  }

  /**
   * Make authenticated API call
   *
   * @param {string} endpoint - API endpoint path (relative to /api)
   * @param {RequestInit} options - Fetch options
   * @returns {Promise<any>} API response data
   */
  async authenticatedFetch<T = any>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const response = await fetch(`${this.baseUrl}${endpoint}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...this.getAuthHeader(),
        ...options.headers,
      },
    });

    if (response.status === 401) {
      // Token expired or invalid
      this.clearAuthStorage();
      throw new Error('Session expired. Please login again.');
    }

    if (!response.ok) {
      throw new Error(`API error: ${response.statusText}`);
    }

    const apiResponse: ApiResponse<T> = await response.json();

    if (!apiResponse.success) {
      throw new Error(apiResponse.message);
    }

    return apiResponse.data;
  }
}

