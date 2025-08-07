/**
 * Main JavaScript entry point for School Information Management System
 * Handles application initialization, routing, and core functionality
 */

// Application configuration
const APP_CONFIG = {
    API_BASE_URL: 'http://localhost:8080/api/v1',
    APP_NAME: 'School Information Management System',
    VERSION: '1.0.0',
    TOKEN_KEY: 'sim_auth_token',
    REFRESH_TOKEN_KEY: 'sim_refresh_token',
    USER_KEY: 'sim_current_user'
};

// Application state
const AppState = {
    currentUser: null,
    isAuthenticated: false,
    currentRoute: 'dashboard',
    loading: false,
    error: null
};

// Import modules (will be implemented in subsequent tasks)
import { AuthService } from './services/auth.js';
import { ApiService } from './services/api.js';
import { Router } from './utils/router.js';
import { NotificationService } from './services/notification.js';
import { LoadingService } from './services/loading.js';

/**
 * Application class - Main application controller
 */
class Application {
    constructor() {
        this.authService = new AuthService();
        this.apiService = new ApiService();
        this.router = new Router();
        this.notificationService = new NotificationService();
        this.loadingService = new LoadingService();
        
        this.init();
    }

    /**
     * Initialize the application
     */
    async init() {
        try {
            console.log('Initializing SIM Application...');
            
            // Show loading spinner
            this.loadingService.show();
            
            // Set up event listeners
            this.setupEventListeners();
            
            // Check authentication status
            await this.checkAuthStatus();
            
            // Initialize router
            this.router.init();
            
            // Hide loading spinner and show app
            this.loadingService.hide();
            this.showApp();
            
            console.log('SIM Application initialized successfully');
            
        } catch (error) {
            console.error('Failed to initialize application:', error);
            this.handleError(error);
        }
    }

    /**
     * Set up global event listeners
     */
    setupEventListeners() {
        // Navigation event listeners
        document.addEventListener('click', (e) => {
            if (e.target.matches('[data-route]')) {
                e.preventDefault();
                const route = e.target.getAttribute('data-route');
                this.router.navigate(route);
            }
        });

        // Login form submission
        const loginForm = document.getElementById('login-form');
        if (loginForm) {
            loginForm.addEventListener('submit', (e) => this.handleLogin(e));
        }

        // Logout button
        const logoutBtn = document.getElementById('logout-btn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.handleLogout();
            });
        }

        // Forgot password form
        const forgotPasswordForm = document.getElementById('forgot-password-form');
        if (forgotPasswordForm) {
            forgotPasswordForm.addEventListener('submit', (e) => this.handleForgotPassword(e));
        }

        // Handle browser back/forward buttons
        window.addEventListener('popstate', (e) => {
            if (e.state && e.state.route) {
                this.router.navigate(e.state.route, false);
            }
        });

        // Handle window resize for responsive design
        window.addEventListener('resize', () => {
            this.handleResize();
        });

        // Handle online/offline status
        window.addEventListener('online', () => {
            this.notificationService.showSuccess('Connection restored');
        });

        window.addEventListener('offline', () => {
            this.notificationService.showWarning('Connection lost. Some features may not work.');
        });
    }

    /**
     * Check authentication status on app startup
     */
    async checkAuthStatus() {
        try {
            const token = localStorage.getItem(APP_CONFIG.TOKEN_KEY);
            
            if (token) {
                // Validate token with backend
                const isValid = await this.authService.validateToken(token);
                
                if (isValid) {
                    const userData = await this.authService.getCurrentUser();
                    this.setAuthenticatedUser(userData);
                } else {
                    // Token is invalid, clear storage
                    this.clearAuthData();
                }
            }
        } catch (error) {
            console.error('Error checking auth status:', error);
            this.clearAuthData();
        }
    }

    /**
     * Handle user login
     */
    async handleLogin(event) {
        event.preventDefault();
        
        const form = event.target;
        const formData = new FormData(form);
        const loginData = {
            identifier: formData.get('identifier'),
            password: formData.get('password'),
            rememberMe: formData.get('rememberMe') === 'on'
        };

        try {
            // Show loading state
            const submitBtn = document.getElementById('login-submit-btn');
            const spinner = document.getElementById('login-spinner');
            
            submitBtn.disabled = true;
            spinner.classList.remove('d-none');

            // Attempt login
            const response = await this.authService.login(loginData);
            
            if (response.success) {
                // Store auth data
                this.setAuthenticatedUser(response.user);
                
                // Hide login modal
                const loginModal = bootstrap.Modal.getInstance(document.getElementById('loginModal'));
                loginModal.hide();
                
                // Show success message
                this.notificationService.showSuccess('Login successful!');
                
                // Navigate to dashboard
                this.router.navigate('dashboard');
                
            } else {
                throw new Error(response.message || 'Login failed');
            }

        } catch (error) {
            console.error('Login error:', error);
            this.notificationService.showError(error.message || 'Login failed. Please try again.');
            
            // Show field-specific errors if available
            if (error.fieldErrors) {
                this.showFieldErrors(error.fieldErrors);
            }

        } finally {
            // Reset loading state
            const submitBtn = document.getElementById('login-submit-btn');
            const spinner = document.getElementById('login-spinner');
            
            submitBtn.disabled = false;
            spinner.classList.add('d-none');
        }
    }

    /**
     * Handle user logout
     */
    async handleLogout() {
        try {
            await this.authService.logout();
            this.clearAuthData();
            this.notificationService.showInfo('You have been logged out');
            this.showLoginModal();
        } catch (error) {
            console.error('Logout error:', error);
            // Force logout even if API call fails
            this.clearAuthData();
            this.showLoginModal();
        }
    }

    /**
     * Handle forgot password
     */
    async handleForgotPassword(event) {
        event.preventDefault();
        
        const form = event.target;
        const formData = new FormData(form);
        const email = formData.get('email');

        try {
            await this.authService.requestPasswordReset(email);
            this.notificationService.showSuccess('Password reset email sent!');
            
            // Hide forgot password modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('forgotPasswordModal'));
            modal.hide();
            
        } catch (error) {
            console.error('Password reset error:', error);
            this.notificationService.showError(error.message || 'Failed to send reset email');
        }
    }

    /**
     * Set authenticated user data
     */
    setAuthenticatedUser(userData) {
        AppState.currentUser = userData;
        AppState.isAuthenticated = true;
        
        // Update UI
        document.getElementById('current-user-name').textContent = userData.firstName || userData.username;
        
        // Store in localStorage
        localStorage.setItem(APP_CONFIG.USER_KEY, JSON.stringify(userData));
    }

    /**
     * Clear authentication data
     */
    clearAuthData() {
        AppState.currentUser = null;
        AppState.isAuthenticated = false;
        
        // Clear localStorage
        localStorage.removeItem(APP_CONFIG.TOKEN_KEY);
        localStorage.removeItem(APP_CONFIG.REFRESH_TOKEN_KEY);
        localStorage.removeItem(APP_CONFIG.USER_KEY);
    }

    /**
     * Show login modal
     */
    showLoginModal() {
        const loginModal = new bootstrap.Modal(document.getElementById('loginModal'));
        loginModal.show();
    }

    /**
     * Show the main application
     */
    showApp() {
        document.getElementById('loading-spinner').classList.add('d-none');
        document.getElementById('app').classList.remove('d-none');
        
        // Check if user is authenticated
        if (!AppState.isAuthenticated) {
            this.showLoginModal();
        }
    }

    /**
     * Handle application errors
     */
    handleError(error) {
        console.error('Application error:', error);
        
        // Hide loading spinner
        this.loadingService.hide();
        
        // Show error notification
        this.notificationService.showError(
            error.message || 'An unexpected error occurred'
        );
    }

    /**
     * Show field validation errors
     */
    showFieldErrors(fieldErrors) {
        Object.keys(fieldErrors).forEach(fieldName => {
            const field = document.querySelector(`[name="${fieldName}"]`);
            if (field) {
                field.classList.add('is-invalid');
                const feedback = field.nextElementSibling;
                if (feedback && feedback.classList.contains('invalid-feedback')) {
                    feedback.textContent = fieldErrors[fieldName];
                }
            }
        });
    }

    /**
     * Handle window resize
     */
    handleResize() {
        // Update any responsive components
        // This will be expanded in later tasks
    }

    /**
     * Get current user
     */
    getCurrentUser() {
        return AppState.currentUser;
    }

    /**
     * Check if user is authenticated
     */
    isAuthenticated() {
        return AppState.isAuthenticated;
    }

    /**
     * Check if user has specific permission
     */
    hasPermission(permission) {
        if (!AppState.currentUser || !AppState.currentUser.permissions) {
            return false;
        }
        return AppState.currentUser.permissions.includes(permission);
    }

    /**
     * Check if user has specific role
     */
    hasRole(role) {
        if (!AppState.currentUser || !AppState.currentUser.role) {
            return false;
        }
        return AppState.currentUser.role === role;
    }
}

// Initialize application when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.SIMApp = new Application();
});

// Export for module usage
export { Application, APP_CONFIG, AppState };