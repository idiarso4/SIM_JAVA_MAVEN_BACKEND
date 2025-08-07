/**
 * Authentication Service
 * Handles user authentication, token management, and session handling
 */

import { ApiService } from './api.js';
import { APP_CONFIG } from '../main.js';

export class AuthService {
    constructor() {
        this.apiService = new ApiService();
        this.tokenRefreshTimer = null;
    }

    /**
     * User login
     */
    async login(credentials) {
        try {
            const response = await this.apiService.post('/auth/login', credentials);
            
            if (response.data) {
                // Store tokens
                this.storeTokens(response.data);
                
                // Set up token refresh
                this.setupTokenRefresh();
                
                return {
                    success: true,
                    user: response.data.user,
                    token: response.data.token
                };
            }
            
            throw new Error('Invalid response from server');
            
        } catch (error) {
            console.error('Login error:', error);
            throw new Error(error.response?.data?.message || 'Login failed');
        }
    }

    /**
     * User logout
     */
    async logout() {
        try {
            const token = this.getToken();
            if (token) {
                await this.apiService.post('/auth/logout');
            }
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            this.clearTokens();
            this.clearTokenRefresh();
        }
    }

    /**
     * Refresh authentication token
     */
    async refreshToken() {
        try {
            const refreshToken = this.getRefreshToken();
            if (!refreshToken) {
                throw new Error('No refresh token available');
            }

            const response = await this.apiService.post('/auth/refresh', {
                refreshToken: refreshToken
            });

            if (response.data) {
                this.storeTokens(response.data);
                this.setupTokenRefresh();
                return response.data;
            }

            throw new Error('Token refresh failed');

        } catch (error) {
            console.error('Token refresh error:', error);
            this.clearTokens();
            throw error;
        }
    }

    /**
     * Validate token with backend
     */
    async validateToken(token) {
        try {
            const response = await this.apiService.get('/auth/validate', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            return response.data?.valid === true;
        } catch (error) {
            console.error('Token validation error:', error);
            return false;
        }
    }

    /**
     * Get current user information
     */
    async getCurrentUser() {
        try {
            const response = await this.apiService.get('/auth/me');
            return response.data?.user;
        } catch (error) {
            console.error('Get current user error:', error);
            throw error;
        }
    }

    /**
     * Request password reset
     */
    async requestPasswordReset(email) {
        try {
            await this.apiService.post('/auth/password-reset', { email });
            return true;
        } catch (error) {
            console.error('Password reset request error:', error);
            throw new Error(error.response?.data?.message || 'Failed to send reset email');
        }
    }

    /**
     * Confirm password reset
     */
    async confirmPasswordReset(token, newPassword) {
        try {
            await this.apiService.post('/auth/password-reset/confirm', {
                token,
                newPassword
            });
            return true;
        } catch (error) {
            console.error('Password reset confirmation error:', error);
            throw new Error(error.response?.data?.message || 'Failed to reset password');
        }
    }

    /**
     * Change password
     */
    async changePassword(currentPassword, newPassword) {
        try {
            await this.apiService.post('/auth/change-password', {
                currentPassword,
                newPassword
            });
            return true;
        } catch (error) {
            console.error('Change password error:', error);
            throw new Error(error.response?.data?.message || 'Failed to change password');
        }
    }

    /**
     * Store authentication tokens
     */
    storeTokens(authData) {
        if (authData.token) {
            localStorage.setItem(APP_CONFIG.TOKEN_KEY, authData.token);
        }
        if (authData.refreshToken) {
            localStorage.setItem(APP_CONFIG.REFRESH_TOKEN_KEY, authData.refreshToken);
        }
        if (authData.user) {
            localStorage.setItem(APP_CONFIG.USER_KEY, JSON.stringify(authData.user));
        }
    }

    /**
     * Get stored authentication token
     */
    getToken() {
        return localStorage.getItem(APP_CONFIG.TOKEN_KEY);
    }

    /**
     * Get stored refresh token
     */
    getRefreshToken() {
        return localStorage.getItem(APP_CONFIG.REFRESH_TOKEN_KEY);
    }

    /**
     * Get stored user data
     */
    getStoredUser() {
        const userData = localStorage.getItem(APP_CONFIG.USER_KEY);
        return userData ? JSON.parse(userData) : null;
    }

    /**
     * Clear stored tokens
     */
    clearTokens() {
        localStorage.removeItem(APP_CONFIG.TOKEN_KEY);
        localStorage.removeItem(APP_CONFIG.REFRESH_TOKEN_KEY);
        localStorage.removeItem(APP_CONFIG.USER_KEY);
    }

    /**
     * Check if user is authenticated
     */
    isAuthenticated() {
        const token = this.getToken();
        return !!token && !this.isTokenExpired(token);
    }

    /**
     * Check if token is expired
     */
    isTokenExpired(token) {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const currentTime = Date.now() / 1000;
            return payload.exp < currentTime;
        } catch (error) {
            console.error('Error checking token expiration:', error);
            return true;
        }
    }

    /**
     * Set up automatic token refresh
     */
    setupTokenRefresh() {
        this.clearTokenRefresh();
        
        const token = this.getToken();
        if (!token) return;

        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const expirationTime = payload.exp * 1000;
            const currentTime = Date.now();
            const refreshTime = expirationTime - currentTime - (5 * 60 * 1000); // Refresh 5 minutes before expiry

            if (refreshTime > 0) {
                this.tokenRefreshTimer = setTimeout(() => {
                    this.refreshToken().catch(error => {
                        console.error('Automatic token refresh failed:', error);
                        // Redirect to login if refresh fails
                        window.location.reload();
                    });
                }, refreshTime);
            }
        } catch (error) {
            console.error('Error setting up token refresh:', error);
        }
    }

    /**
     * Clear token refresh timer
     */
    clearTokenRefresh() {
        if (this.tokenRefreshTimer) {
            clearTimeout(this.tokenRefreshTimer);
            this.tokenRefreshTimer = null;
        }
    }

    /**
     * Get authorization header
     */
    getAuthHeader() {
        const token = this.getToken();
        return token ? { 'Authorization': `Bearer ${token}` } : {};
    }
}