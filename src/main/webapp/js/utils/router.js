/**
 * Router Utility
 * Handles client-side routing for Single Page Application
 */

import { rbacManager } from './rbac.js';

export class Router {
    constructor() {
        this.routes = new Map();
        this.currentRoute = null;
        this.defaultRoute = 'dashboard';
        this.beforeRouteChange = null;
        this.afterRouteChange = null;
    }

    /**
     * Initialize router
     */
    init() {
        // Register default routes
        this.registerDefaultRoutes();

        // Handle initial route
        const initialRoute = this.getRouteFromURL() || this.defaultRoute;
        this.navigate(initialRoute, false);

        console.log('Router initialized');
    }

    /**
     * Register default application routes
     */
    registerDefaultRoutes() {
        this.register('dashboard', {
            title: 'Dashboard',
            component: () => import('../components/dashboard.js'),
            requiresAuth: true,
            breadcrumb: [{ text: 'Dashboard', href: '#dashboard' }]
        });

        this.register('students', {
            title: 'Students',
            component: () => import('../components/students.js'),
            requiresAuth: true,
            permissions: ['VIEW_STUDENTS', 'MANAGE_STUDENTS'],
            breadcrumb: [
                { text: 'Dashboard', href: '#dashboard' },
                { text: 'Students', href: '#students' }
            ]
        });

        this.register('users', {
            title: 'Users',
            component: () => import('../components/users.js'),
            requiresAuth: true,
            permissions: ['VIEW_USERS', 'MANAGE_USERS'],
            breadcrumb: [
                { text: 'Dashboard', href: '#dashboard' },
                { text: 'Users', href: '#users' }
            ]
        });

        this.register('grades', {
            title: 'Grades',
            component: () => import('../components/grades.js'),
            requiresAuth: true,
            permissions: ['VIEW_GRADES', 'MANAGE_GRADES'],
            breadcrumb: [
                { text: 'Dashboard', href: '#dashboard' },
                { text: 'Grades', href: '#grades' }
            ]
        });

        this.register('reports', {
            title: 'Reports',
            component: () => import('../components/reports.js'),
            requiresAuth: true,
            permissions: ['VIEW_REPORTS', 'VIEW_BASIC_REPORTS'],
            breadcrumb: [
                { text: 'Dashboard', href: '#dashboard' },
                { text: 'Reports', href: '#reports' }
            ]
        });

        this.register('profile', {
            title: 'Profile',
            component: () => import('../components/profile.js'),
            requiresAuth: true,
            breadcrumb: [
                { text: 'Dashboard', href: '#dashboard' },
                { text: 'Profile', href: '#profile' }
            ]
        });

        this.register('settings', {
            title: 'Settings',
            component: () => import('../components/settings.js'),
            requiresAuth: true,
            breadcrumb: [
                { text: 'Dashboard', href: '#dashboard' },
                { text: 'Settings', href: '#settings' }
            ]
        });
    }

    /**
     * Register a route
     */
    register(path, config) {
        this.routes.set(path, {
            path,
            title: config.title || path,
            component: config.component,
            requiresAuth: config.requiresAuth !== false,
            permissions: config.permissions || [],
            roles: config.roles || [],
            breadcrumb: config.breadcrumb || [],
            beforeEnter: config.beforeEnter,
            afterEnter: config.afterEnter,
            ...config
        });
    }

    /**
     * Navigate to a route
     */
    async navigate(path, pushState = true) {
        try {
            console.log(`Navigating to: ${path}`);

            const route = this.routes.get(path);
            if (!route) {
                console.error(`Route not found: ${path}`);
                return this.navigate(this.defaultRoute, pushState);
            }

            // Run before route change hook
            if (this.beforeRouteChange) {
                const canProceed = await this.beforeRouteChange(route, this.currentRoute);
                if (!canProceed) {
                    console.log('Navigation cancelled by beforeRouteChange hook');
                    return;
                }
            }

            // Check authentication
            if (route.requiresAuth && !this.isAuthenticated()) {
                console.log('Authentication required, showing login modal');
                this.showLoginModal();
                return;
            }

            // Check permissions using RBAC manager
            if (route.permissions && route.permissions.length > 0 && !rbacManager.hasAnyPermission(route.permissions)) {
                console.log('Insufficient permissions for route:', path);
                this.showAccessDenied(route.permissions);
                return;
            }

            // Check roles using RBAC manager
            if (route.roles && route.roles.length > 0 && !rbacManager.hasAnyRole(route.roles)) {
                console.log('Insufficient roles for route:', path);
                this.showAccessDenied(route.roles, 'roles');
                return;
            }

            // Run route's beforeEnter hook
            if (route.beforeEnter) {
                const canEnter = await route.beforeEnter(route);
                if (!canEnter) {
                    console.log('Navigation cancelled by route beforeEnter hook');
                    return;
                }
            }

            // Update browser history
            if (pushState) {
                history.pushState({ route: path }, route.title, `#${path}`);
            }

            // Update page title
            document.title = `${route.title} - SIM`;

            // Update navigation
            this.updateNavigation(path);

            // Update breadcrumb
            this.updateBreadcrumb(route.breadcrumb);

            // Load and render component
            await this.loadComponent(route);

            // Update current route
            this.currentRoute = route;

            // Run route's afterEnter hook
            if (route.afterEnter) {
                await route.afterEnter(route);
            }

            // Run after route change hook
            if (this.afterRouteChange) {
                await this.afterRouteChange(route, this.currentRoute);
            }

            console.log(`Successfully navigated to: ${path}`);

        } catch (error) {
            console.error('Navigation error:', error);
            this.showError('Failed to load page. Please try again.');
        }
    }

    /**
     * Load and render route component
     */
    async loadComponent(route) {
        const mainContent = document.getElementById('main-content');
        if (!mainContent) {
            throw new Error('Main content container not found');
        }

        try {
            // Show loading state
            mainContent.innerHTML = `
                <div class="d-flex justify-content-center align-items-center" style="min-height: 200px;">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                </div>
            `;

            // Load component module
            const componentModule = await route.component();
            const ComponentClass = componentModule.default || componentModule[Object.keys(componentModule)[0]];

            if (!ComponentClass) {
                throw new Error(`Component not found for route: ${route.path}`);
            }

            // Create and render component
            const component = new ComponentClass();
            const content = await component.render();

            // Update main content
            mainContent.innerHTML = content;

            // Initialize component if it has an init method
            if (component.init && typeof component.init === 'function') {
                await component.init();
            }

            // Add fade-in animation
            mainContent.classList.add('fade-in');

        } catch (error) {
            console.error('Error loading component:', error);
            mainContent.innerHTML = `
                <div class="error-container">
                    <div class="error-icon">
                        <i class="fas fa-exclamation-triangle"></i>
                    </div>
                    <div class="error-message">
                        Failed to load page content
                    </div>
                    <button class="btn btn-primary" onclick="location.reload()">
                        <i class="fas fa-refresh me-2"></i>Reload Page
                    </button>
                </div>
            `;
        }
    }

    /**
     * Update navigation active state
     */
    updateNavigation(currentPath) {
        const navLinks = document.querySelectorAll('.navbar-nav .nav-link[data-route]');
        navLinks.forEach(link => {
            const route = link.getAttribute('data-route');
            if (route === currentPath) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });
    }

    /**
     * Update breadcrumb navigation
     */
    updateBreadcrumb(breadcrumbItems) {
        const breadcrumb = document.getElementById('breadcrumb');
        if (!breadcrumb || !breadcrumbItems) return;

        breadcrumb.innerHTML = '';

        breadcrumbItems.forEach((item, index) => {
            const li = document.createElement('li');
            li.className = 'breadcrumb-item';

            if (index === breadcrumbItems.length - 1) {
                // Last item is active
                li.classList.add('active');
                li.setAttribute('aria-current', 'page');
                li.textContent = item.text;
            } else {
                // Other items are links
                const a = document.createElement('a');
                a.href = item.href;
                a.textContent = item.text;
                a.addEventListener('click', (e) => {
                    e.preventDefault();
                    const route = item.href.replace('#', '');
                    this.navigate(route);
                });
                li.appendChild(a);
            }

            breadcrumb.appendChild(li);
        });
    }

    /**
     * Get route from current URL
     */
    getRouteFromURL() {
        const hash = window.location.hash.substring(1);
        return hash || null;
    }

    /**
     * Check if user is authenticated
     */
    isAuthenticated() {
        return window.SIMApp && window.SIMApp.isAuthenticated();
    }

    /**
     * Check if user has required permissions (deprecated - use rbacManager directly)
     */
    hasPermissions(permissions) {
        return rbacManager.hasAnyPermission(permissions);
    }

    /**
     * Check if user has required roles (deprecated - use rbacManager directly)
     */
    hasRoles(roles) {
        return rbacManager.hasAnyRole(roles);
    }

    /**
     * Show login modal
     */
    showLoginModal() {
        const loginModal = new bootstrap.Modal(document.getElementById('loginModal'));
        loginModal.show();
    }

    /**
     * Show access denied message
     */
    showAccessDenied(requiredAccess = [], accessType = 'permissions') {
        const mainContent = document.getElementById('main-content');
        if (mainContent) {
            const user = rbacManager.getCurrentUser();
            const userRole = rbacManager.getUserRoleDisplayName();
            
            let accessDetails = '';
            if (requiredAccess.length > 0) {
                const accessList = requiredAccess.join(', ');
                accessDetails = `<p class="text-muted mb-3">Required ${accessType}: <code>${accessList}</code></p>`;
            }
            
            mainContent.innerHTML = `
                <div class="error-container text-center py-5">
                    <div class="error-icon mb-4">
                        <i class="fas fa-shield-alt text-warning" style="font-size: 4rem;"></i>
                    </div>
                    <h2 class="error-message text-warning mb-3">Access Restricted</h2>
                    <p class="text-muted mb-3">You don't have sufficient permissions to access this page.</p>
                    ${accessDetails}
                    <div class="alert alert-info d-inline-block mb-4">
                        <i class="fas fa-info-circle me-2"></i>
                        <strong>Your Role:</strong> ${userRole}
                    </div>
                    <div class="d-flex justify-content-center gap-3">
                        <button class="btn btn-primary" onclick="window.SIMApp.router.navigate('dashboard')">
                            <i class="fas fa-home me-2"></i>Go to Dashboard
                        </button>
                        <button class="btn btn-outline-secondary" onclick="history.back()">
                            <i class="fas fa-arrow-left me-2"></i>Go Back
                        </button>
                    </div>
                    <div class="mt-4">
                        <small class="text-muted">
                            Need access? Contact your administrator at 
                            <a href="mailto:admin@school.edu">admin@school.edu</a>
                        </small>
                    </div>
                </div>
            `;
        }
    }

    /**
     * Show error message
     */
    showError(message) {
        if (window.SIMApp && window.SIMApp.notificationService) {
            window.SIMApp.notificationService.showError(message);
        }
    }

    /**
     * Set before route change hook
     */
    setBeforeRouteChange(callback) {
        this.beforeRouteChange = callback;
    }

    /**
     * Set after route change hook
     */
    setAfterRouteChange(callback) {
        this.afterRouteChange = callback;
    }

    /**
     * Get current route
     */
    getCurrentRoute() {
        return this.currentRoute;
    }

    /**
     * Go back in history
     */
    goBack() {
        history.back();
    }

    /**
     * Go forward in history
     */
    goForward() {
        history.forward();
    }

    /**
     * Replace current route
     */
    replace(path) {
        this.navigate(path, false);
        history.replaceState({ route: path }, '', `#${path}`);
    }
}