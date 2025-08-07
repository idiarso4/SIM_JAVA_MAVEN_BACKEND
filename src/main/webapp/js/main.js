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

// Import modules
import { AuthService } from './services/auth.js';
import { ApiService } from './services/api.js';
import { Router } from './utils/router.js';
import { NotificationService } from './services/notification.js';
import { LoadingService } from './services/loading.js';
import { templateManager } from './utils/template.js';
import { layoutManager } from './utils/layout.js';
import { stateManager } from './utils/state.js';
import { validator } from './utils/validation.js';
import { rbacManager } from './utils/rbac.js';
import { directiveManager } from './utils/directives.js';
import { initDevTools, logger, performance as perfMonitor } from './utils/dev.js';

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
        this.templateManager = templateManager;
        this.layoutManager = layoutManager;
        this.stateManager = stateManager;
        this.validator = validator;
        this.rbacManager = rbacManager;
        this.directiveManager = directiveManager;
        
        this.init();
    }

    /**
     * Initialize the application
     */
    async init() {
        try {
            logger.info('Initializing SIM Application...');
            perfMonitor.mark('app-init-start');
            
            // Initialize development tools
            initDevTools();
            
            // Show loading spinner
            this.loadingService.show();
            
            // Initialize core systems
            await this.initializeCoreServices();
            
            // Set up event listeners
            this.setupEventListeners();
            
            // Check authentication status
            await this.checkAuthStatus();
            
            // Initialize router
            this.router.init();
            
            // Setup application state
            this.setupApplicationState();
            
            // Hide loading spinner and show app
            this.loadingService.hide();
            this.showApp();
            
            perfMonitor.mark('app-init-end');
            perfMonitor.measure('app-initialization', 'app-init-start', 'app-init-end');
            
            logger.info('SIM Application initialized successfully');
            this.layoutManager.announce('Application loaded successfully');
            
        } catch (error) {
            logger.error('Failed to initialize application:', error);
            this.handleError(error);
        }
    }

    /**
     * Initialize core services
     */
    async initializeCoreServices() {
        try {
            // Initialize template manager
            await this.templateManager.loadComponentTemplates();
            
            // Initialize layout manager
            this.layoutManager.init();
            
            // Setup API interceptors
            this.setupApiInterceptors();
            
            // Setup error handling
            this.setupGlobalErrorHandling();
            
            logger.debug('Core services initialized');
        } catch (error) {
            logger.error('Failed to initialize core services:', error);
            throw error;
        }
    }

    /**
     * Setup API interceptors
     */
    setupApiInterceptors() {
        // Add request interceptor for authentication
        this.apiService.setDefaultHeader('X-Requested-With', 'XMLHttpRequest');
        
        // Add response interceptor for error handling
        const originalRequest = this.apiService.request.bind(this.apiService);
        this.apiService.request = async (method, endpoint, options = {}) => {
            try {
                const response = await originalRequest(method, endpoint, options);
                return response;
            } catch (error) {
                this.handleApiError(error);
                throw error;
            }
        };
    }

    /**
     * Setup global error handling
     */
    setupGlobalErrorHandling() {
        // Handle unhandled promise rejections
        window.addEventListener('unhandledrejection', (event) => {
            logger.error('Unhandled promise rejection:', event.reason);
            this.notificationService.showError('An unexpected error occurred');
            event.preventDefault();
        });

        // Handle global errors
        window.addEventListener('error', (event) => {
            logger.error('Global error:', event.error);
            this.notificationService.showError('An unexpected error occurred');
        });
    }

    /**
     * Handle API errors
     */
    handleApiError(error) {
        if (error.status === 401) {
            // Unauthorized - redirect to login
            this.clearAuthData();
            this.showLoginModal();
        } else if (error.status === 403) {
            // Forbidden
            this.notificationService.showError('Access denied');
        } else if (error.status === 429) {
            // Too many requests
            this.notificationService.showWarning('Too many requests. Please try again later.');
        } else if (error.status >= 500) {
            // Server error
            this.notificationService.showError('Server error. Please try again later.');
        }
    }

    /**
     * Setup application state
     */
    setupApplicationState() {
        // Listen for theme changes
        window.addEventListener('themeChanged', (event) => {
            logger.debug('Theme changed to:', event.detail.theme);
        });

        // Listen for layout changes
        window.addEventListener('layoutResize', (event) => {
            logger.debug('Layout resized:', event.detail);
        });

        // Setup periodic token refresh
        if (AppState.isAuthenticated) {
            this.setupTokenRefresh();
        }
    }

    /**
     * Setup token refresh
     */
    setupTokenRefresh() {
        // Refresh token every 30 minutes
        setInterval(async () => {
            try {
                if (this.isAuthenticated()) {
                    await this.authService.refreshToken();
                    logger.debug('Token refreshed automatically');
                }
            } catch (error) {
                logger.warn('Automatic token refresh failed:', error);
            }
        }, 30 * 60 * 1000);
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
        const userNameElement = document.getElementById('current-user-name');
        if (userNameElement) {
            userNameElement.textContent = userData.firstName || userData.username;
        }
        
        // Update user info in dropdown
        const userInfoElement = document.getElementById('user-info');
        if (userInfoElement) {
            const roleDisplay = this.rbacManager.getUserRoleDisplayName();
            userInfoElement.textContent = `${userData.firstName || userData.username} (${roleDisplay})`;
        }
        
        // Store in localStorage
        localStorage.setItem(APP_CONFIG.USER_KEY, JSON.stringify(userData));
        
        // Apply RBAC permissions to UI
        setTimeout(() => {
            this.rbacManager.applyUIPermissions();
            this.updateNavigationBasedOnPermissions();
        }, 100);
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
     * Show login modal or redirect to login page
     */
    showLoginModal() {
        // Check if we're already on the login page
        if (window.location.pathname.includes('login.html')) {
            return;
        }
        
        // Redirect to dedicated login page for better UX
        window.location.href = 'login.html';
    }

    /**
     * Initialize login form component
     */
    async initializeLoginForm() {
        try {
            const { default: LoginForm } = await import('./components/login-form.js');
            const container = document.getElementById('login-form-container');
            
            if (container) {
                this.loginForm = new LoginForm(container, {
                    showRememberMe: true,
                    showForgotPassword: true,
                    autoFocus: false // We'll handle focus manually
                });
                
                // Setup login form event listeners
                this.setupLoginFormListeners();
                
                logger.debug('Login form initialized');
            }
        } catch (error) {
            logger.error('Failed to initialize login form:', error);
            this.notificationService.showError('Failed to load login form');
        }
    }

    /**
     * Setup login form event listeners
     */
    setupLoginFormListeners() {
        const container = document.getElementById('login-form-container');
        if (!container) return;

        // Handle login attempt
        container.addEventListener('loginAttempt', async (e) => {
            const { loginData } = e.detail;
            
            try {
                logger.debug('Processing login attempt');
                const response = await this.authService.login(loginData);
                
                if (response.success) {
                    this.loginForm.handleLoginSuccess(response.user);
                    
                    // Store auth data
                    this.setAuthenticatedUser(response.user);
                    
                    // Hide login modal
                    const loginModal = bootstrap.Modal.getInstance(document.getElementById('loginModal'));
                    if (loginModal) {
                        loginModal.hide();
                    }
                    
                    // Show success notification
                    this.notificationService.showSuccess('Welcome back!');
                    
                    // Navigate to dashboard
                    this.router.navigate('dashboard');
                    
                } else {
                    throw new Error(response.message || 'Login failed');
                }
                
            } catch (error) {
                logger.error('Login attempt failed:', error);
                this.loginForm.handleLoginError(error);
            }
        });

        // Handle login success
        container.addEventListener('loginSuccess', (e) => {
            const { userData } = e.detail;
            logger.info('Login successful for user:', userData.username);
            
            // Update UI state
            this.layoutManager.announce(`Welcome back, ${userData.firstName || userData.username}`);
        });

        // Handle login error
        container.addEventListener('loginError', (e) => {
            const { error } = e.detail;
            logger.warn('Login error handled by form:', error.message);
        });
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
        return this.rbacManager.hasPermission(permission);
    }

    /**
     * Check if user has specific role
     */
    hasRole(role) {
        return this.rbacManager.hasRole(role);
    }

    /**
     * Update navigation based on user permissions
     */
    updateNavigationBasedOnPermissions() {
        const navItems = [
            { element: 'nav-students', route: 'students' },
            { element: 'nav-users', route: 'users' },
            { element: 'nav-grades', route: 'grades' },
            { element: 'nav-reports', route: 'reports' }
        ];

        navItems.forEach(({ element, route }) => {
            const navElement = document.querySelector(`[data-route="${route}"]`);
            if (navElement) {
                if (this.rbacManager.canAccessRoute(route)) {
                    navElement.style.display = '';
                    navElement.parentElement.style.display = '';
                } else {
                    navElement.style.display = 'none';
                    navElement.parentElement.style.display = 'none';
                }
            }
        });

        // Update page actions based on permissions
        this.updatePageActions();
    }

    /**
     * Update page actions based on current route and permissions
     */
    updatePageActions() {
        const currentRoute = this.router.getCurrentRoute();
        if (!currentRoute) return;

        const pageToolbar = document.getElementById('page-toolbar');
        if (!pageToolbar) return;

        // Clear existing actions
        pageToolbar.innerHTML = '';

        // Add route-specific actions based on permissions
        const routeActions = this.getRouteActions(currentRoute.path);
        routeActions.forEach(action => {
            if (!action.permissions || this.rbacManager.hasAnyPermission(action.permissions)) {
                const button = document.createElement('button');
                button.className = `btn ${action.class || 'btn-primary'} me-2`;
                button.innerHTML = `${action.icon ? `<i class="${action.icon} me-1"></i>` : ''}${action.text}`;
                
                if (action.onClick) {
                    button.addEventListener('click', action.onClick);
                }
                
                pageToolbar.appendChild(button);
            }
        });
    }

    /**
     * Get actions for specific route
     */
    getRouteActions(route) {
        const actions = {
            'students': [
                {
                    text: 'Add Student',
                    icon: 'fas fa-plus',
                    class: 'btn-primary',
                    permissions: ['CREATE_STUDENT', 'MANAGE_STUDENTS'],
                    onClick: () => this.showCreateStudentModal()
                },
                {
                    text: 'Import Students',
                    icon: 'fas fa-upload',
                    class: 'btn-outline-primary',
                    permissions: ['MANAGE_STUDENTS'],
                    onClick: () => this.showImportStudentsModal()
                },
                {
                    text: 'Export Students',
                    icon: 'fas fa-download',
                    class: 'btn-outline-secondary',
                    permissions: ['EXPORT_STUDENT_DATA', 'EXPORT_DATA'],
                    onClick: () => this.exportStudents()
                }
            ],
            'users': [
                {
                    text: 'Add User',
                    icon: 'fas fa-user-plus',
                    class: 'btn-primary',
                    permissions: ['CREATE_USER', 'MANAGE_USERS'],
                    onClick: () => this.showCreateUserModal()
                }
            ],
            'grades': [
                {
                    text: 'Add Grade',
                    icon: 'fas fa-plus',
                    class: 'btn-primary',
                    permissions: ['EDIT_GRADES', 'MANAGE_GRADES'],
                    onClick: () => this.showAddGradeModal()
                }
            ],
            'reports': [
                {
                    text: 'Generate Report',
                    icon: 'fas fa-chart-bar',
                    class: 'btn-primary',
                    permissions: ['VIEW_REPORTS'],
                    onClick: () => this.showGenerateReportModal()
                }
            ]
        };

        return actions[route] || [];
    }

    // Placeholder methods for actions (to be implemented in future tasks)
    showCreateStudentModal() {
        this.notificationService.showInfo('Create Student feature will be implemented in upcoming tasks');
    }

    showImportStudentsModal() {
        this.notificationService.showInfo('Import Students feature will be implemented in upcoming tasks');
    }

    exportStudents() {
        this.notificationService.showInfo('Export Students feature will be implemented in upcoming tasks');
    }

    showCreateUserModal() {
        this.notificationService.showInfo('Create User feature will be implemented in upcoming tasks');
    }

    showAddGradeModal() {
        this.notificationService.showInfo('Add Grade feature will be implemented in upcoming tasks');
    }

    showGenerateReportModal() {
        this.notificationService.showInfo('Generate Report feature will be implemented in upcoming tasks');
    }
}

// Initialize application when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.SIMApp = new Application();
});

// Export for module usage
export { Application, APP_CONFIG, AppState };