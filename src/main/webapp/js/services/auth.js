/**
 * Authentication Service
 * Handles user authentication, token management, and session handling
 */

import { ApiService } from './api.js';
import { APP_CONFIG } from '../main.js';
import { logger } from '../utils/dev.js';
import { stateManager } from '../utils/state.js';

export class AuthService {
    constructor() {
        this.apiService = new ApiService();
        this.tokenRefreshTimer = null;
        this.refreshPromise = null;
        this.loginAttempts = 0;
        this.maxLoginAttempts = 5;
        this.lockoutDuration = 15 * 60 * 1000; // 15 minutes
        
        this.initializeState();
    }

    /**
     * Initialize authentication state
     */
    initializeState() {
        // Set up state management
        stateManager.set('auth.isAuthenticated', false);
        stateManager.set('auth.user', null);
        stateManager.set('auth.token', null);
        stateManager.set('auth.refreshToken', null);
        stateManager.set('auth.tokenExpiry', null);
        stateManager.set('auth.loginAttempts', 0);
        stateManager.set('auth.lockoutUntil', null);
        
        // Restore login attempts from localStorage
        const storedAttempts = localStorage.getItem('sim_login_attempts');
        const storedLockout = localStorage.getItem('sim_lockout_until');
        
        if (storedAttempts) {
            this.loginAttempts = parseInt(storedAttempts, 10);
            stateManager.set('auth.loginAttempts', this.loginAttempts);
        }
        
        if (storedLockout) {
            const lockoutTime = parseInt(storedLockout, 10);
            if (Date.now() < lockoutTime) {
                stateManager.set('auth.lockoutUntil', lockoutTime);
            } else {
                // Lockout expired, clear it
                localStorage.removeItem('sim_lockout_until');
                localStorage.removeItem('sim_login_attempts');
                this.loginAttempts = 0;
                stateManager.set('auth.loginAttempts', 0);
            }
        }
    }

    /**
     * Check if account is locked out
     */
    isLockedOut() {
        const lockoutUntil = stateManager.get('auth.lockoutUntil');
        return lockoutUntil && Date.now() < lockoutUntil;
    }

    /**
     * Get remaining lockout time in minutes
     */
    getRemainingLockoutTime() {
        const lockoutUntil = stateManager.get('auth.lockoutUntil');
        if (!lockoutUntil || Date.now() >= lockoutUntil) return 0;
        
        return Math.ceil((lockoutUntil - Date.now()) / (60 * 1000));
    }

    /**
     * Handle failed login attempt
     */
    handleFailedLogin() {
        this.loginAttempts++;
        stateManager.set('auth.loginAttempts', this.loginAttempts);
        localStorage.setItem('sim_login_attempts', this.loginAttempts.toString());
        
        if (this.loginAttempts >= this.maxLoginAttempts) {
            const lockoutUntil = Date.now() + this.lockoutDuration;
            stateManager.set('auth.lockoutUntil', lockoutUntil);
            localStorage.setItem('sim_lockout_until', lockoutUntil.toString());
            
            logger.warn(`Account locked out until ${new Date(lockoutUntil).toLocaleString()}`);
        }
    }

    /**
     * Clear login attempts on successful login
     */
    clearLoginAttempts() {
        this.loginAttempts = 0;
        stateManager.set('auth.loginAttempts', 0);
        stateManager.set('auth.lockoutUntil', null);
        localStorage.removeItem('sim_login_attempts');
        localStorage.removeItem('sim_lockout_until');
    }

    /**
     * User login
     */
    async login(credentials) {
        try {
            // Check if account is locked out
            if (this.isLockedOut()) {
                const remainingTime = this.getRemainingLockoutTime();
                throw new Error(`Account temporarily locked. Try again in ${remainingTime} minutes.`);
            }

            logger.debug('Attempting login for user:', credentials.identifier);
            
            const response = await this.apiService.post('/auth/login', credentials);
            
            // Handle both token and accessToken field names for compatibility
            const token = response.data.token || response.data.accessToken;
            const userRefreshToken = response.data.refreshToken;
            const user = response.data.user;
            
            if (token && user) {
                // Clear any previous login attempts
                this.clearLoginAttempts();
                
                // Normalize response data
                const authData = {
                    token: token,
                    refreshToken: userRefreshToken,
                    user: user
                };
                
                // Store tokens and user data
                this.storeTokens(authData);
                
                // Update state
                stateManager.set('auth.isAuthenticated', true);
                stateManager.set('auth.user', user);
                stateManager.set('auth.token', token);
                stateManager.set('auth.refreshToken', userRefreshToken);
                
                // Parse token expiry
                const tokenExpiry = this.getTokenExpiry(token);
                stateManager.set('auth.tokenExpiry', tokenExpiry);
                
                // Set up token refresh
                this.setupTokenRefresh();
                
                logger.info('Login successful for user:', user.name || user.username || user.email);
                
                return {
                    success: true,
                    user: user,
                    token: token
                };
            }
            
            throw new Error('Invalid response from server');
            
        } catch (error) {
            logger.error('Login error:', error);
            
            // Handle failed login attempt
            if (error.status === 401) {
                this.handleFailedLogin();
            }
            
            // Throw appropriate error
            if (error.status === 401) {
                throw new Error('Invalid email/username or password');
            } else if (error.status === 423) {
                throw new Error('Account is temporarily locked. Please contact support.');
            } else if (error.status === 429) {
                throw new Error('Too many login attempts. Please try again later.');
            } else {
                throw new Error(error.response?.data?.message || error.message || 'Login failed');
            }
        }
    }

    /**
     * User logout
     */
    async logout() {
        try {
            const token = this.getToken();
            if (token) {
                logger.debug('Logging out user');
                await this.apiService.post('/auth/logout');
            }
        } catch (error) {
            logger.error('Logout error:', error);
        } finally {
            this.clearTokens();
            this.clearTokenRefresh();
            this.clearAuthState();
            logger.info('User logged out successfully');
        }
    }

    /**
     * Clear authentication state
     */
    clearAuthState() {
        stateManager.set('auth.isAuthenticated', false);
        stateManager.set('auth.user', null);
        stateManager.set('auth.token', null);
        stateManager.set('auth.refreshToken', null);
        stateManager.set('auth.tokenExpiry', null);
    }

    /**
     * Refresh authentication token
     */
    async refreshToken() {
        // Prevent multiple simultaneous refresh requests
        if (this.refreshPromise) {
            return this.refreshPromise;
        }

        this.refreshPromise = this.performTokenRefresh();
        
        try {
            const result = await this.refreshPromise;
            return result;
        } finally {
            this.refreshPromise = null;
        }
    }

    /**
     * Perform actual token refresh
     */
    async performTokenRefresh() {
        try {
            const refreshToken = this.getRefreshToken();
            if (!refreshToken) {
                throw new Error('No refresh token available');
            }

            logger.debug('Refreshing authentication token');

            const response = await this.apiService.post('/auth/refresh', {
                refreshToken: refreshToken
            });

            // Handle both token and accessToken field names for compatibility
            const token = response.data.token || response.data.accessToken;
            const newRefreshToken = response.data.refreshToken;
            
            if (token) {
                // Normalize response data
                const authData = {
                    token: token,
                    refreshToken: newRefreshToken
                };
                
                // Store new tokens
                this.storeTokens(authData);
                
                // Update state
                stateManager.set('auth.token', token);
                if (newRefreshToken) {
                    stateManager.set('auth.refreshToken', newRefreshToken);
                }
                
                // Update token expiry
                const tokenExpiry = this.getTokenExpiry(token);
                stateManager.set('auth.tokenExpiry', tokenExpiry);
                
                // Set up next refresh
                this.setupTokenRefresh();
                
                logger.debug('Token refreshed successfully');
                return authData;
            }

            throw new Error('Invalid refresh response');

        } catch (error) {
            logger.error('Token refresh error:', error);
            
            // Clear tokens and redirect to login
            this.clearTokens();
            this.clearAuthState();
            
            // Dispatch event for app to handle
            window.dispatchEvent(new CustomEvent('authTokenExpired', {
                detail: { error }
            }));
            
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
     * Parse JWT token payload
     */
    parseJwtPayload(token) {
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));
            
            return JSON.parse(jsonPayload);
        } catch (error) {
            logger.error('Error parsing JWT payload:', error);
            return null;
        }
    }

    /**
     * Get token expiry time
     */
    getTokenExpiry(token) {
        const payload = this.parseJwtPayload(token);
        return payload ? payload.exp * 1000 : null;
    }

    /**
     * Get token issued time
     */
    getTokenIssuedAt(token) {
        const payload = this.parseJwtPayload(token);
        return payload ? payload.iat * 1000 : null;
    }

    /**
     * Get user info from token
     */
    getUserFromToken(token) {
        const payload = this.parseJwtPayload(token);
        if (!payload) return null;
        
        return {
            id: payload.sub,
            username: payload.username,
            email: payload.email,
            roles: payload.roles || [],
            permissions: payload.permissions || []
        };
    }

    /**
     * Check if token is expired
     */
    isTokenExpired(token) {
        if (!token) return true;
        
        const payload = this.parseJwtPayload(token);
        if (!payload || !payload.exp) return true;
        
        const currentTime = Date.now() / 1000;
        const bufferTime = 30; // 30 seconds buffer
        
        return payload.exp < (currentTime + bufferTime);
    }

    /**
     * Check if token will expire soon
     */
    isTokenExpiringSoon(token, thresholdMinutes = 5) {
        if (!token) return true;
        
        const payload = this.parseJwtPayload(token);
        if (!payload || !payload.exp) return true;
        
        const currentTime = Date.now() / 1000;
        const thresholdTime = thresholdMinutes * 60;
        
        return payload.exp < (currentTime + thresholdTime);
    }

    /**
     * Get time until token expires (in milliseconds)
     */
    getTimeUntilExpiry(token) {
        if (!token) return 0;
        
        const payload = this.parseJwtPayload(token);
        if (!payload || !payload.exp) return 0;
        
        const currentTime = Date.now() / 1000;
        const timeUntilExpiry = (payload.exp - currentTime) * 1000;
        
        return Math.max(0, timeUntilExpiry);
    }

    /**
     * Set up automatic token refresh
     */
    setupTokenRefresh() {
        this.clearTokenRefresh();
        
        const token = this.getToken();
        if (!token || this.isTokenExpired(token)) {
            logger.debug('No valid token for refresh setup');
            return;
        }

        const timeUntilExpiry = this.getTimeUntilExpiry(token);
        const refreshTime = Math.max(0, timeUntilExpiry - (5 * 60 * 1000)); // Refresh 5 minutes before expiry

        if (refreshTime > 0) {
            logger.debug(`Setting up token refresh in ${Math.round(refreshTime / 1000)} seconds`);
            
            this.tokenRefreshTimer = setTimeout(async () => {
                try {
                    await this.refreshToken();
                    logger.debug('Automatic token refresh successful');
                } catch (error) {
                    logger.error('Automatic token refresh failed:', error);
                    
                    // Dispatch event for app to handle
                    window.dispatchEvent(new CustomEvent('authTokenRefreshFailed', {
                        detail: { error }
                    }));
                }
            }, refreshTime);
        } else {
            logger.debug('Token expires too soon, immediate refresh needed');
            // Token expires very soon, try to refresh immediately
            this.refreshToken().catch(error => {
                logger.error('Immediate token refresh failed:', error);
            });
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

    /**
     * Check if user has specific permission
     */
    hasPermission(permission) {
        const user = stateManager.get('auth.user');
        if (!user || !user.permissions) return false;
        
        return user.permissions.includes(permission);
    }

    /**
     * Check if user has any of the specified permissions
     */
    hasAnyPermission(permissions) {
        return permissions.some(permission => this.hasPermission(permission));
    }

    /**
     * Check if user has all specified permissions
     */
    hasAllPermissions(permissions) {
        return permissions.every(permission => this.hasPermission(permission));
    }

    /**
     * Check if user has specific role
     */
    hasRole(role) {
        const user = stateManager.get('auth.user');
        if (!user || !user.roles) return false;
        
        return user.roles.includes(role);
    }

    /**
     * Check if user has any of the specified roles
     */
    hasAnyRole(roles) {
        return roles.some(role => this.hasRole(role));
    }

    /**
     * Get current user
     */
    getCurrentUser() {
        return stateManager.get('auth.user');
    }

    /**
     * Update current user data
     */
    updateCurrentUser(userData) {
        const currentUser = this.getCurrentUser();
        if (currentUser) {
            const updatedUser = { ...currentUser, ...userData };
            stateManager.set('auth.user', updatedUser);
            localStorage.setItem(APP_CONFIG.USER_KEY, JSON.stringify(updatedUser));
            
            logger.debug('User data updated');
        }
    }

    /**
     * Get session info
     */
    getSessionInfo() {
        const token = this.getToken();
        if (!token) return null;
        
        const payload = this.parseJwtPayload(token);
        if (!payload) return null;
        
        return {
            issuedAt: new Date(payload.iat * 1000),
            expiresAt: new Date(payload.exp * 1000),
            timeUntilExpiry: this.getTimeUntilExpiry(token),
            isExpiringSoon: this.isTokenExpiringSoon(token),
            user: this.getCurrentUser()
        };
    }

    /**
     * Extend session (refresh token)
     */
    async extendSession() {
        try {
            await this.refreshToken();
            logger.info('Session extended successfully');
            return true;
        } catch (error) {
            logger.error('Failed to extend session:', error);
            return false;
        }
    }

    /**
     * Check session validity
     */
    isSessionValid() {
        const token = this.getToken();
        const user = this.getCurrentUser();
        
        return !!(token && user && !this.isTokenExpired(token));
    }

    /**
     * Subscribe to authentication state changes
     */
    onAuthStateChange(callback) {
        return stateManager.subscribe('auth.isAuthenticated', callback);
    }

    /**
     * Subscribe to user data changes
     */
    onUserChange(callback) {
        return stateManager.subscribe('auth.user', callback);
    }

    /**
     * Subscribe to token changes
     */
    onTokenChange(callback) {
        return stateManager.subscribe('auth.token', callback);
    }

    /**
     * Get authentication state
     */
    getAuthState() {
        return {
            isAuthenticated: stateManager.get('auth.isAuthenticated'),
            user: stateManager.get('auth.user'),
            token: stateManager.get('auth.token'),
            tokenExpiry: stateManager.get('auth.tokenExpiry'),
            loginAttempts: stateManager.get('auth.loginAttempts'),
            lockoutUntil: stateManager.get('auth.lockoutUntil')
        };
    }

    /**
     * Initialize from stored data
     */
    async initializeFromStorage() {
        try {
            const token = this.getToken();
            const user = this.getStoredUser();
            
            if (token && user && !this.isTokenExpired(token)) {
                // Restore authentication state
                stateManager.set('auth.isAuthenticated', true);
                stateManager.set('auth.user', user);
                stateManager.set('auth.token', token);
                
                const tokenExpiry = this.getTokenExpiry(token);
                stateManager.set('auth.tokenExpiry', tokenExpiry);
                
                // Set up token refresh
                this.setupTokenRefresh();
                
                logger.info('Authentication restored from storage');
                return true;
            } else {
                // Clear invalid data
                this.clearTokens();
                this.clearAuthState();
                logger.debug('No valid authentication data in storage');
                return false;
            }
        } catch (error) {
            logger.error('Error initializing from storage:', error);
            this.clearTokens();
            this.clearAuthState();
            return false;
        }
    }
}