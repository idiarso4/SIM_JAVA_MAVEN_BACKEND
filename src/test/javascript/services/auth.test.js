/**
 * Authentication Service Tests
 */

import { AuthService } from '../../../main/webapp/js/services/auth.js';

// Mock localStorage
const localStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn()
};
global.localStorage = localStorageMock;

// Mock fetch
global.fetch = jest.fn();

describe('AuthService', () => {
  let authService;

  beforeEach(() => {
    authService = new AuthService();
    jest.clearAllMocks();
  });

  describe('login', () => {
    it('should login successfully with valid credentials', async () => {
      const mockResponse = {
        data: {
          token: 'mock-token',
          refreshToken: 'mock-refresh-token',
          user: { id: 1, username: 'testuser' }
        }
      };

      // Mock successful API response
      authService.apiService.post = jest.fn().mockResolvedValue(mockResponse);

      const credentials = {
        identifier: 'testuser',
        password: 'password123'
      };

      const result = await authService.login(credentials);

      expect(result.success).toBe(true);
      expect(result.user).toEqual(mockResponse.data.user);
      expect(localStorage.setItem).toHaveBeenCalledWith('sim_auth_token', 'mock-token');
    });

    it('should throw error on invalid credentials', async () => {
      const mockError = new Error('Invalid credentials');
      authService.apiService.post = jest.fn().mockRejectedValue(mockError);

      const credentials = {
        identifier: 'testuser',
        password: 'wrongpassword'
      };

      await expect(authService.login(credentials)).rejects.toThrow('Invalid credentials');
    });
  });

  describe('logout', () => {
    it('should logout successfully', async () => {
      authService.apiService.post = jest.fn().mockResolvedValue({});
      localStorage.getItem.mockReturnValue('mock-token');

      await authService.logout();

      expect(authService.apiService.post).toHaveBeenCalledWith('/auth/logout');
      expect(localStorage.removeItem).toHaveBeenCalledWith('sim_auth_token');
    });
  });

  describe('isAuthenticated', () => {
    it('should return true when valid token exists', () => {
      localStorage.getItem.mockReturnValue('valid-token');
      authService.isTokenExpired = jest.fn().mockReturnValue(false);

      const result = authService.isAuthenticated();

      expect(result).toBe(true);
    });

    it('should return false when no token exists', () => {
      localStorage.getItem.mockReturnValue(null);

      const result = authService.isAuthenticated();

      expect(result).toBe(false);
    });
  });
});