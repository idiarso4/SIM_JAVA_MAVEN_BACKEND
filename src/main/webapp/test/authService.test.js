/**
 * AuthService Test Suite
 * Tests for JWT authentication service functionality
 */

import { AuthService } from '../js/services/auth.js';
import { ApiService } from '../js/services/api.js';

// Mock the dependencies
jest.mock('../js/services/api.js');
jest.mock('../js/main.js', () => ({
  APP_CONFIG: {
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
    get: jest.fn(),
    subscribe: jest.fn()
  }
}));

describe('AuthService', () => {
  let authService;
  let mockApiService;

  beforeEach(() => {
    // Clear all mocks
    jest.clearAllMocks();
    
    // Reset localStorage
    localStorage.clear();
    
    // Create mock API service
    mockApiService = {
      post: jest.fn(),
      get: jest.fn()
    };
    ApiService.mockImplementation(() => mockApiService);
    
    // Create auth service instance
    authService = new AuthService();
    
    // Mock Date.now for consistent testing
    jest.spyOn(Date, 'now').mockReturnValue(1640995200000); // 2022-01-01 00:00:00
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe('Initialization', () => {
    test('should initialize with default state', () => {
      expect(authService.loginAttempts).toBe(0);
      expect(authService.maxLoginAttempts).toBe(5);
      expect(authService.lockoutDuration).toBe(15 * 60 * 1000);
    });

    test('should restore login attempts from localStorage', () => {
      localStorage.setItem('sim_login_attempts', '3');
      const newAuthService = new AuthService();
      expect(newAuthService.loginAttempts).toBe(3);
    });

    test('should clear expired lockout data', () => {
      const pastTime = Date.now() - 1000;
      localStorage.setItem('sim_lockout_until', pastTime.toString());
      localStorage.setItem('sim_login_attempts', '5');
      
      const newAuthService = new AuthService();
      expect(newAuthService.loginAttempts).toBe(0);
      expect(localStorage.getItem('sim_lockout_until')).toBeNull();
      expect(localStorage.getItem('sim_login_attempts')).toBeNull();
    });
  });

  describe('Login', () => {
    test('should login successfully with valid credentials', async () => {
      const mockResponse = {
        data: {
          token: 'mock-jwt-token',
          refreshToken: 'mock-refresh-token',
          user: {
            id: 1,
            username: 'testuser',
            email: 'test@example.com',
            roles: ['USER'],
            permissions: ['READ_STUDENTS']
          }
        }
      };

      mockApiService.post.mockResolvedValue(mockResponse);

      const credentials = {
        identifier: 'test@example.com',
        password: 'password123'
      };

      const result = await authService.login(credentials);

      expect(result.success).toBe(true);
      expect(result.user).toEqual(mockResponse.data.user);
      expect(result.token).toBe(mockResponse.data.token);
      expect(mockApiService.post).toHaveBeenCalledWith('/auth/login', credentials);
    });

    test('should handle login failure with invalid credentials', async () => {
      const mockError = {
        status: 401,
        response: {
          data: {
            message: 'Invalid credentials'
          }
        }
      };

      mockApiService.post.mockRejectedValue(mockError);

      const credentials = {
        identifier: 'test@example.com',
        password: 'wrongpassword'
      };

      await expect(authService.login(credentials)).rejects.toThrow('Invalid email/username or password');
      expect(authService.loginAttempts).toBe(1);
    });

    test('should handle account lockout after max attempts', async () => {
      const mockError = {
        status: 401,
        response: {
          data: {
            message: 'Invalid credentials'
          }
        }
      };

      mockApiService.post.mockRejectedValue(mockError);

      const credentials = {
        identifier: 'test@example.com',
        password: 'wrongpassword'
      };

      // Simulate 5 failed attempts
      for (let i = 0; i < 5; i++) {
        try {
          await authService.login(credentials);
        } catch (error) {
          // Expected to fail
        }
      }

      expect(authService.loginAttempts).toBe(5);
      expect(localStorage.getItem('sim_lockout_until')).toBeTruthy();

      // Next attempt should be blocked
      await expect(authService.login(credentials)).rejects.toThrow(/Account temporarily locked/);
    });

    test('should prevent login when account is locked', async () => {
      const futureTime = Date.now() + 10 * 60 * 1000; // 10 minutes from now
      localStorage.setItem('sim_lockout_until', futureTime.toString());
      
      const newAuthService = new AuthService();
      
      const credentials = {
        identifier: 'test@example.com',
        password: 'password123'
      };

      await expect(newAuthService.login(credentials)).rejects.toThrow(/Account temporarily locked/);
    });
  });

  describe('Token Management', () => {
    test('should store tokens correctly', () => {
      const authData = {
        token: 'mock-jwt-token',
        refreshToken: 'mock-refresh-token',
        user: {
          id: 1,
          username: 'testuser'
        }
      };

      authService.storeTokens(authData);

      expect(localStorage.getItem('sim_auth_token')).toBe('mock-jwt-token');
      expect(localStorage.getItem('sim_refresh_token')).toBe('mock-refresh-token');
      expect(localStorage.getItem('sim_current_user')).toBe(JSON.stringify(authData.user));
    });

    test('should retrieve tokens correctly', () => {
      localStorage.setItem('sim_auth_token', 'stored-token');
      localStorage.setItem('sim_refresh_token', 'stored-refresh-token');

      expect(authService.getToken()).toBe('stored-token');
      expect(authService.getRefreshToken()).toBe('stored-refresh-token');
    });

    test('should clear tokens correctly', () => {
      localStorage.setItem('sim_auth_token', 'token');
      localStorage.setItem('sim_refresh_token', 'refresh-token');
      localStorage.setItem('sim_current_user', '{"id": 1}');

      authService.clearTokens();

      expect(localStorage.getItem('sim_auth_token')).toBeNull();
      expect(localStorage.getItem('sim_refresh_token')).toBeNull();
      expect(localStorage.getItem('sim_current_user')).toBeNull();
    });
  });

  describe('JWT Token Parsing', () => {
    test('should parse JWT payload correctly', () => {
      // Mock JWT token with payload: {"sub": "1", "username": "testuser", "exp": 1640995800}
      const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImV4cCI6MTY0MDk5NTgwMH0.signature';
      
      const payload = authService.parseJwtPayload(mockToken);
      
      expect(payload).toEqual({
        sub: "1",
        username: "testuser",
        exp: 1640995800
      });
    });

    test('should handle invalid JWT token', () => {
      const invalidToken = 'invalid.token.format';
      const payload = authService.parseJwtPayload(invalidToken);
      expect(payload).toBeNull();
    });

    test('should check token expiry correctly', () => {
      // Token expires at 1640995800 (600 seconds from now)
      const validToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImV4cCI6MTY0MDk5NTgwMH0.signature';
      
      expect(authService.isTokenExpired(validToken)).toBe(false);
      
      // Token expired 600 seconds ago
      const expiredToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImV4cCI6MTY0MDk5NDYwMH0.signature';
      
      expect(authService.isTokenExpired(expiredToken)).toBe(true);
    });

    test('should check if token is expiring soon', () => {
      // Token expires in 3 minutes (180 seconds)
      const soonToExpireToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImV4cCI6MTY0MDk5NTM4MH0.signature';
      
      expect(authService.isTokenExpiringSoon(soonToExpireToken, 5)).toBe(true);
      expect(authService.isTokenExpiringSoon(soonToExpireToken, 2)).toBe(false);
    });
  });

  describe('Token Refresh', () => {
    test('should refresh token successfully', async () => {
      const mockResponse = {
        data: {
          token: 'new-jwt-token',
          refreshToken: 'new-refresh-token'
        }
      };

      mockApiService.post.mockResolvedValue(mockResponse);
      localStorage.setItem('sim_refresh_token', 'old-refresh-token');

      const result = await authService.refreshToken();

      expect(result).toEqual(mockResponse.data);
      expect(mockApiService.post).toHaveBeenCalledWith('/auth/refresh', {
        refreshToken: 'old-refresh-token'
      });
    });

    test('should handle refresh token failure', async () => {
      const mockError = {
        status: 401,
        response: {
          data: {
            message: 'Invalid refresh token'
          }
        }
      };

      mockApiService.post.mockRejectedValue(mockError);
      localStorage.setItem('sim_refresh_token', 'invalid-refresh-token');

      // Mock window.dispatchEvent
      const mockDispatchEvent = jest.fn();
      Object.defineProperty(window, 'dispatchEvent', {
        value: mockDispatchEvent,
        writable: true
      });

      await expect(authService.refreshToken()).rejects.toThrow();
      
      // Should clear tokens and dispatch event
      expect(localStorage.getItem('sim_auth_token')).toBeNull();
      expect(localStorage.getItem('sim_refresh_token')).toBeNull();
      expect(mockDispatchEvent).toHaveBeenCalledWith(
        expect.objectContaining({
          type: 'authTokenExpired'
        })
      );
    });

    test('should prevent multiple simultaneous refresh requests', async () => {
      const mockResponse = {
        data: {
          token: 'new-jwt-token',
          refreshToken: 'new-refresh-token'
        }
      };

      // Delay the API response
      mockApiService.post.mockImplementation(() => 
        new Promise(resolve => setTimeout(() => resolve(mockResponse), 100))
      );
      
      localStorage.setItem('sim_refresh_token', 'refresh-token');

      // Start two refresh requests simultaneously
      const promise1 = authService.refreshToken();
      const promise2 = authService.refreshToken();

      const [result1, result2] = await Promise.all([promise1, promise2]);

      // Both should return the same result
      expect(result1).toEqual(mockResponse.data);
      expect(result2).toEqual(mockResponse.data);
      
      // API should only be called once
      expect(mockApiService.post).toHaveBeenCalledTimes(1);
    });
  });

  describe('Logout', () => {
    test('should logout successfully', async () => {
      localStorage.setItem('sim_auth_token', 'token');
      localStorage.setItem('sim_refresh_token', 'refresh-token');
      localStorage.setItem('sim_current_user', '{"id": 1}');

      mockApiService.post.mockResolvedValue({});

      await authService.logout();

      expect(mockApiService.post).toHaveBeenCalledWith('/auth/logout');
      expect(localStorage.getItem('sim_auth_token')).toBeNull();
      expect(localStorage.getItem('sim_refresh_token')).toBeNull();
      expect(localStorage.getItem('sim_current_user')).toBeNull();
    });

    test('should clear tokens even if API call fails', async () => {
      localStorage.setItem('sim_auth_token', 'token');
      localStorage.setItem('sim_refresh_token', 'refresh-token');

      mockApiService.post.mockRejectedValue(new Error('Network error'));

      await authService.logout();

      // Tokens should still be cleared
      expect(localStorage.getItem('sim_auth_token')).toBeNull();
      expect(localStorage.getItem('sim_refresh_token')).toBeNull();
    });
  });

  describe('Authentication Status', () => {
    test('should return true for authenticated user with valid token', () => {
      // Token expires in 10 minutes
      const validToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImV4cCI6MTY0MDk5NTgwMH0.signature';
      localStorage.setItem('sim_auth_token', validToken);

      expect(authService.isAuthenticated()).toBe(true);
    });

    test('should return false for expired token', () => {
      // Token expired 10 minutes ago
      const expiredToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImV4cCI6MTY0MDk5NDYwMH0.signature';
      localStorage.setItem('sim_auth_token', expiredToken);

      expect(authService.isAuthenticated()).toBe(false);
    });

    test('should return false when no token exists', () => {
      expect(authService.isAuthenticated()).toBe(false);
    });
  });

  describe('Password Management', () => {
    test('should request password reset successfully', async () => {
      mockApiService.post.mockResolvedValue({});

      const result = await authService.requestPasswordReset('test@example.com');

      expect(result).toBe(true);
      expect(mockApiService.post).toHaveBeenCalledWith('/auth/password-reset', {
        email: 'test@example.com'
      });
    });

    test('should handle password reset request failure', async () => {
      const mockError = {
        response: {
          data: {
            message: 'Email not found'
          }
        }
      };

      mockApiService.post.mockRejectedValue(mockError);

      await expect(authService.requestPasswordReset('invalid@example.com'))
        .rejects.toThrow('Email not found');
    });

    test('should confirm password reset successfully', async () => {
      mockApiService.post.mockResolvedValue({});

      const result = await authService.confirmPasswordReset('reset-token', 'newpassword123');

      expect(result).toBe(true);
      expect(mockApiService.post).toHaveBeenCalledWith('/auth/password-reset/confirm', {
        token: 'reset-token',
        newPassword: 'newpassword123'
      });
    });

    test('should change password successfully', async () => {
      mockApiService.post.mockResolvedValue({});

      const result = await authService.changePassword('oldpassword', 'newpassword123');

      expect(result).toBe(true);
      expect(mockApiService.post).toHaveBeenCalledWith('/auth/change-password', {
        currentPassword: 'oldpassword',
        newPassword: 'newpassword123'
      });
    });
  });

  describe('Session Management', () => {
    test('should get session info correctly', () => {
      // Token with known payload
      const token = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImlhdCI6MTY0MDk5NTIwMCwiZXhwIjoxNjQwOTk1ODAwfQ.signature';
      localStorage.setItem('sim_auth_token', token);

      const sessionInfo = authService.getSessionInfo();

      expect(sessionInfo).toEqual({
        issuedAt: new Date(1640995200 * 1000),
        expiresAt: new Date(1640995800 * 1000),
        timeUntilExpiry: expect.any(Number),
        isExpiringSoon: expect.any(Boolean),
        user: null // No user in state for this test
      });
    });

    test('should validate session correctly', () => {
      // Valid token and user
      const validToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImV4cCI6MTY0MDk5NTgwMH0.signature';
      localStorage.setItem('sim_auth_token', validToken);
      
      // Mock getCurrentUser to return a user
      const mockUser = { id: 1, username: 'testuser' };
      jest.spyOn(authService, 'getCurrentUser').mockReturnValue(mockUser);

      expect(authService.isSessionValid()).toBe(true);
    });

    test('should return false for invalid session', () => {
      // No token
      expect(authService.isSessionValid()).toBe(false);

      // Expired token
      const expiredToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImV4cCI6MTY0MDk5NDYwMH0.signature';
      localStorage.setItem('sim_auth_token', expiredToken);
      
      expect(authService.isSessionValid()).toBe(false);
    });
  });

  describe('Authorization Header', () => {
    test('should return authorization header with token', () => {
      localStorage.setItem('sim_auth_token', 'test-token');

      const header = authService.getAuthHeader();

      expect(header).toEqual({
        'Authorization': 'Bearer test-token'
      });
    });

    test('should return empty object when no token', () => {
      const header = authService.getAuthHeader();

      expect(header).toEqual({});
    });
  });

  describe('Initialization from Storage', () => {
    test('should restore authentication from valid stored data', async () => {
      const validToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImV4cCI6MTY0MDk5NTgwMH0.signature';
      const userData = { id: 1, username: 'testuser' };
      
      localStorage.setItem('sim_auth_token', validToken);
      localStorage.setItem('sim_current_user', JSON.stringify(userData));

      const result = await authService.initializeFromStorage();

      expect(result).toBe(true);
    });

    test('should clear invalid stored data', async () => {
      const expiredToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImV4cCI6MTY0MDk5NDYwMH0.signature';
      const userData = { id: 1, username: 'testuser' };
      
      localStorage.setItem('sim_auth_token', expiredToken);
      localStorage.setItem('sim_current_user', JSON.stringify(userData));

      const result = await authService.initializeFromStorage();

      expect(result).toBe(false);
      expect(localStorage.getItem('sim_auth_token')).toBeNull();
      expect(localStorage.getItem('sim_current_user')).toBeNull();
    });
  });
});