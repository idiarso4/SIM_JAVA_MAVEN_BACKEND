/**
 * AuthService Integration Test Suite
 * Tests for JWT authentication service integration with API service
 */

import { AuthService } from '../js/services/auth.js';
import { ApiService } from '../js/services/api.js';

// Mock the dependencies
jest.mock('../js/main.js', () => ({
  APP_CONFIG: {
    API_BASE_URL: 'http://localhost:8080/api/v1',
    TOKEN_KEY: 'sim_auth_token',
    REFRESH_TOKEN_KEY: 'sim_refresh_token',
    USER_KEY: 'sim_current_user'
  }
}));

jest.mock('../js/utils/dev.js', () => ({
  logger: {
    debug: jest.fn(),
    info: jest.fn(),
    warn: jest.fn(),
    error: jest.fn()
  }
}));

jest.mock('../js/utils/state.js', () => ({
  stateManager: {
    set: jest.fn(),
    get: jest.fn().mockReturnValue(null),
    subscribe: jest.fn()
  }
}));

describe('AuthService Integration Tests', () => {
  let authService;
  let apiService;

  beforeEach(() => {
    // Clear all mocks
    jest.clearAllMocks();
    
    // Reset localStorage
    localStorage.clear();
    
    // Create real API service instance
    apiService = new ApiService();
    
    // Create auth service instance
    authService = new AuthService();
    
    // Mock fetch for API calls
    global.fetch = jest.fn();
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe('Authentication Flow Integration', () => {
    test('should complete full login flow with API integration', async () => {
      // Mock successful login response
      const mockLoginResponse = {
        ok: true,
        status: 200,
        headers: new Map([['content-type', 'application/json']]),
        json: jest.fn().mockResolvedValue({
          token: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImV4cCI6MTY0MDk5NTgwMH0.signature',
          refreshToken: 'refresh-token-123',
          user: {
            id: 1,
            username: 'testuser',
            email: 'test@example.com',
            firstName: 'Test',
            lastName: 'User',
            roles: ['USER'],
            permissions: ['READ_STUDENTS']
          }
        })
      };

      global.fetch.mockResolvedValue(mockLoginResponse);

      const credentials = {
        identifier: 'test@example.com',
        password: 'password123'
      };

      const result = await authService.login(credentials);

      // Verify API call was made correctly
      expect(global.fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/auth/login',
        expect.objectContaining({
          method: 'POST',
          headers: expect.objectContaining({
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          }),
          body: JSON.stringify(credentials)
        })
      );

      // Verify login result
      expect(result.success).toBe(true);
      expect(result.user.username).toBe('testuser');
      expect(result.token).toBeTruthy();

      // Verify tokens are stored
      expect(localStorage.getItem('sim_auth_token')).toBeTruthy();
      expect(localStorage.getItem('sim_refresh_token')).toBe('refresh-token-123');
      expect(localStorage.getItem('sim_current_user')).toBeTruthy();
    });

    test('should handle API authentication errors correctly', async () => {
      // Mock 401 error response
      const mockErrorResponse = {
        ok: false,
        status: 401,
        headers: new Map([['content-type', 'application/json']]),
        json: jest.fn().mockResolvedValue({
          message: 'Invalid credentials'
        })
      };

      global.fetch.mockResolvedValue(mockErrorResponse);

      const credentials = {
        identifier: 'test@example.com',
        password: 'wrongpassword'
      };

      await expect(authService.login(credentials)).rejects.toThrow('Invalid email/username or password');

      // Verify no tokens are stored
      expect(localStorage.getItem('sim_auth_token')).toBeNull();
      expect(localStorage.getItem('sim_refresh_token')).toBeNull();
    });

    test('should integrate token refresh with API service', async () => {
      // Set up existing refresh token
      localStorage.setItem('sim_refresh_token', 'existing-refresh-token');

      // Mock successful refresh response
      const mockRefreshResponse = {
        ok: true,
        status: 200,
        headers: new Map([['content-type', 'application/json']]),
        json: jest.fn().mockResolvedValue({
          token: 'new-jwt-token',
          refreshToken: 'new-refresh-token'
        })
      };

      global.fetch.mockResolvedValue(mockRefreshResponse);

      const result = await authService.refreshToken();

      // Verify API call was made correctly
      expect(global.fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/auth/refresh',
        expect.objectContaining({
          method: 'POST',
          headers: expect.objectContaining({
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          }),
          body: JSON.stringify({
            refreshToken: 'existing-refresh-token'
          })
        })
      );

      // Verify new tokens are stored
      expect(result.token).toBe('new-jwt-token');
      expect(localStorage.getItem('sim_auth_token')).toBe('new-jwt-token');
      expect(localStorage.getItem('sim_refresh_token')).toBe('new-refresh-token');
    });

    test('should handle token refresh failure and clear tokens', async () => {
      localStorage.setItem('sim_refresh_token', 'invalid-refresh-token');
      localStorage.setItem('sim_auth_token', 'old-token');

      // Mock 401 error response for refresh
      const mockErrorResponse = {
        ok: false,
        status: 401,
        headers: new Map([['content-type', 'application/json']]),
        json: jest.fn().mockResolvedValue({
          message: 'Invalid refresh token'
        })
      };

      global.fetch.mockResolvedValue(mockErrorResponse);

      // Mock window.dispatchEvent
      const mockDispatchEvent = jest.fn();
      Object.defineProperty(window, 'dispatchEvent', {
        value: mockDispatchEvent,
        writable: true
      });

      await expect(authService.refreshToken()).rejects.toThrow();

      // Verify tokens are cleared
      expect(localStorage.getItem('sim_auth_token')).toBeNull();
      expect(localStorage.getItem('sim_refresh_token')).toBeNull();

      // Verify event is dispatched
      expect(mockDispatchEvent).toHaveBeenCalledWith(
        expect.objectContaining({
          type: 'authTokenExpired'
        })
      );
    });

    test('should integrate logout with API service', async () => {
      // Set up existing tokens
      localStorage.setItem('sim_auth_token', 'existing-token');
      localStorage.setItem('sim_refresh_token', 'existing-refresh-token');
      localStorage.setItem('sim_current_user', '{"id": 1, "username": "testuser"}');

      // Mock successful logout response
      const mockLogoutResponse = {
        ok: true,
        status: 200,
        headers: new Map([['content-type', 'application/json']]),
        json: jest.fn().mockResolvedValue({})
      };

      global.fetch.mockResolvedValue(mockLogoutResponse);

      await authService.logout();

      // Verify API call was made with authorization header
      expect(global.fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/auth/logout',
        expect.objectContaining({
          method: 'POST',
          headers: expect.objectContaining({
            'Authorization': 'Bearer existing-token'
          })
        })
      );

      // Verify all tokens are cleared
      expect(localStorage.getItem('sim_auth_token')).toBeNull();
      expect(localStorage.getItem('sim_refresh_token')).toBeNull();
      expect(localStorage.getItem('sim_current_user')).toBeNull();
    });
  });

  describe('API Service Integration', () => {
    test('should automatically add authorization header to API requests', async () => {
      localStorage.setItem('sim_auth_token', 'test-token');

      // Mock successful API response
      const mockResponse = {
        ok: true,
        status: 200,
        headers: new Map([['content-type', 'application/json']]),
        json: jest.fn().mockResolvedValue({ data: 'test' })
      };

      global.fetch.mockResolvedValue(mockResponse);

      await apiService.get('/test-endpoint');

      // Verify authorization header was added
      expect(global.fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/test-endpoint',
        expect.objectContaining({
          headers: expect.objectContaining({
            'Authorization': 'Bearer test-token'
          })
        })
      );
    });

    test('should handle API requests without token', async () => {
      // No token in localStorage

      // Mock successful API response
      const mockResponse = {
        ok: true,
        status: 200,
        headers: new Map([['content-type', 'application/json']]),
        json: jest.fn().mockResolvedValue({ data: 'test' })
      };

      global.fetch.mockResolvedValue(mockResponse);

      await apiService.get('/test-endpoint');

      // Verify no authorization header was added
      expect(global.fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/test-endpoint',
        expect.objectContaining({
          headers: expect.not.objectContaining({
            'Authorization': expect.any(String)
          })
        })
      );
    });

    test('should validate token with backend API', async () => {
      const testToken = 'test-token-for-validation';

      // Mock successful validation response
      const mockResponse = {
        ok: true,
        status: 200,
        headers: new Map([['content-type', 'application/json']]),
        json: jest.fn().mockResolvedValue({ valid: true })
      };

      global.fetch.mockResolvedValue(mockResponse);

      const isValid = await authService.validateToken(testToken);

      expect(isValid).toBe(true);
      expect(global.fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/auth/validate',
        expect.objectContaining({
          headers: expect.objectContaining({
            'Authorization': 'Bearer test-token-for-validation'
          })
        })
      );
    });

    test('should get current user from API', async () => {
      localStorage.setItem('sim_auth_token', 'valid-token');

      const mockUserData = {
        id: 1,
        username: 'testuser',
        email: 'test@example.com',
        firstName: 'Test',
        lastName: 'User'
      };

      // Mock successful user response
      const mockResponse = {
        ok: true,
        status: 200,
        headers: new Map([['content-type', 'application/json']]),
        json: jest.fn().mockResolvedValue({ user: mockUserData })
      };

      global.fetch.mockResolvedValue(mockResponse);

      const userData = await authService.getCurrentUser();

      expect(userData).toEqual(mockUserData);
      expect(global.fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/v1/auth/me',
        expect.objectContaining({
          headers: expect.objectContaining({
            'Authorization': 'Bearer valid-token'
          })
        })
      );
    });
  });

  describe('Error Handling Integration', () => {
    test('should handle network errors gracefully', async () => {
      // Mock network error
      global.fetch.mockRejectedValue(new TypeError('Failed to fetch'));

      const credentials = {
        identifier: 'test@example.com',
        password: 'password123'
      };

      await expect(authService.login(credentials)).rejects.toThrow('Network error. Please check your connection.');
    });

    test('should handle server errors appropriately', async () => {
      // Mock 500 server error
      const mockErrorResponse = {
        ok: false,
        status: 500,
        headers: new Map([['content-type', 'application/json']]),
        json: jest.fn().mockResolvedValue({
          message: 'Internal server error'
        })
      };

      global.fetch.mockResolvedValue(mockErrorResponse);

      const credentials = {
        identifier: 'test@example.com',
        password: 'password123'
      };

      await expect(authService.login(credentials)).rejects.toThrow();
    });

    test('should handle malformed JSON responses', async () => {
      // Mock response with invalid JSON
      const mockResponse = {
        ok: true,
        status: 200,
        headers: new Map([['content-type', 'application/json']]),
        json: jest.fn().mockRejectedValue(new SyntaxError('Unexpected token'))
      };

      global.fetch.mockResolvedValue(mockResponse);

      const credentials = {
        identifier: 'test@example.com',
        password: 'password123'
      };

      await expect(authService.login(credentials)).rejects.toThrow();
    });
  });

  describe('Session Management Integration', () => {
    test('should initialize authentication from storage on startup', async () => {
      // Set up valid stored data
      const validToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImV4cCI6MTY0MDk5NTgwMH0.signature';
      const userData = {
        id: 1,
        username: 'testuser',
        email: 'test@example.com'
      };

      localStorage.setItem('sim_auth_token', validToken);
      localStorage.setItem('sim_current_user', JSON.stringify(userData));

      // Mock Date.now to make token valid
      jest.spyOn(Date, 'now').mockReturnValue(1640995200000); // Before token expiry

      const result = await authService.initializeFromStorage();

      expect(result).toBe(true);
    });

    test('should clear invalid stored data on startup', async () => {
      // Set up expired token
      const expiredToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImV4cCI6MTY0MDk5NDYwMH0.signature';
      const userData = {
        id: 1,
        username: 'testuser'
      };

      localStorage.setItem('sim_auth_token', expiredToken);
      localStorage.setItem('sim_current_user', JSON.stringify(userData));

      // Mock Date.now to make token expired
      jest.spyOn(Date, 'now').mockReturnValue(1640995200000); // After token expiry

      const result = await authService.initializeFromStorage();

      expect(result).toBe(false);
      expect(localStorage.getItem('sim_auth_token')).toBeNull();
      expect(localStorage.getItem('sim_current_user')).toBeNull();
    });
  });
});