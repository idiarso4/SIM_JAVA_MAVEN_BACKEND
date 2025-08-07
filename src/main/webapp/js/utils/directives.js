/**
 * UI Directives for Role-Based Access Control
 * Provides declarative way to show/hide elements based on permissions and roles
 */

import { rbacManager } from './rbac.js';
import { stateManager } from './state.js';
import { logger } from './dev.js';

export class DirectiveManager {
  constructor() {
    this.directives = new Map();
    this.observer = null;
    
    this.registerDefaultDirectives();
    this.setupMutationObserver();
  }

  /**
   * Register default directives
   */
  registerDefaultDirectives() {
    // Permission-based directives
    this.register('v-if-permission', (element, value) => {
      const permissions = value.split(',').map(p => p.trim());
      const hasPermission = rbacManager.hasAnyPermission(permissions);
      this.toggleElement(element, hasPermission);
    });

    this.register('v-if-all-permissions', (element, value) => {
      const permissions = value.split(',').map(p => p.trim());
      const hasAllPermissions = rbacManager.hasAllPermissions(permissions);
      this.toggleElement(element, hasAllPermissions);
    });

    this.register('v-if-no-permission', (element, value) => {
      const permissions = value.split(',').map(p => p.trim());
      const hasPermission = rbacManager.hasAnyPermission(permissions);
      this.toggleElement(element, !hasPermission);
    });

    // Role-based directives
    this.register('v-if-role', (element, value) => {
      const roles = value.split(',').map(r => r.trim());
      const hasRole = rbacManager.hasAnyRole(roles);
      this.toggleElement(element, hasRole);
    });

    this.register('v-if-no-role', (element, value) => {
      const roles = value.split(',').map(r => r.trim());
      const hasRole = rbacManager.hasAnyRole(roles);
      this.toggleElement(element, !hasRole);
    });

    // Authentication-based directives
    this.register('v-if-authenticated', (element, value) => {
      const isAuthenticated = stateManager.get('auth.isAuthenticated');
      this.toggleElement(element, isAuthenticated);
    });

    this.register('v-if-not-authenticated', (element, value) => {
      const isAuthenticated = stateManager.get('auth.isAuthenticated');
      this.toggleElement(element, !isAuthenticated);
    });

    // Action-based directives
    this.register('v-if-can', (element, value) => {
      const [action, resource, ownerId] = value.split(',').map(v => v.trim());
      const canPerform = rbacManager.canPerformAction(action, resource, ownerId);
      this.toggleElement(element, canPerform);
    });

    // Route-based directives
    this.register('v-if-route-accessible', (element, value) => {
      const canAccess = rbacManager.canAccessRoute(value.trim());
      this.toggleElement(element, canAccess);
    });

    // Conditional class directives
    this.register('v-class-if-permission', (element, value) => {
      const [className, permissionList] = value.split(':').map(v => v.trim());
      const permissions = permissionList.split(',').map(p => p.trim());
      const hasPermission = rbacManager.hasAnyPermission(permissions);
      
      if (hasPermission) {
        element.classList.add(className);
      } else {
        element.classList.remove(className);
      }
    });

    this.register('v-class-if-role', (element, value) => {
      const [className, roleList] = value.split(':').map(v => v.trim());
      const roles = roleList.split(',').map(r => r.trim());
      const hasRole = rbacManager.hasAnyRole(roles);
      
      if (hasRole) {
        element.classList.add(className);
      } else {
        element.classList.remove(className);
      }
    });

    // Attribute directives
    this.register('v-disabled-unless-permission', (element, value) => {
      const permissions = value.split(',').map(p => p.trim());
      const hasPermission = rbacManager.hasAnyPermission(permissions);
      element.disabled = !hasPermission;
    });

    this.register('v-disabled-unless-role', (element, value) => {
      const roles = value.split(',').map(r => r.trim());
      const hasRole = rbacManager.hasAnyRole(roles);
      element.disabled = !hasRole;
    });

    // Content directives
    this.register('v-text-role', (element, value) => {
      const roleDisplayName = rbacManager.getUserRoleDisplayName();
      element.textContent = roleDisplayName;
    });

    this.register('v-text-user-name', (element, value) => {
      const user = rbacManager.getCurrentUser();
      if (user) {
        const displayName = user.firstName || user.username || 'User';
        element.textContent = displayName;
      }
    });

    this.register('v-text-user-email', (element, value) => {
      const user = rbacManager.getCurrentUser();
      if (user && user.email) {
        element.textContent = user.email;
      }
    });
  }

  /**
   * Register a new directive
   */
  register(name, handler) {
    this.directives.set(name, handler);
  }

  /**
   * Toggle element visibility
   */
  toggleElement(element, show) {
    if (show) {
      element.style.display = element.dataset.originalDisplay || '';
      element.removeAttribute('aria-hidden');
    } else {
      if (!element.dataset.originalDisplay) {
        element.dataset.originalDisplay = element.style.display || 'block';
      }
      element.style.display = 'none';
      element.setAttribute('aria-hidden', 'true');
    }
  }

  /**
   * Process all directives in the document
   */
  processDirectives(container = document) {
    this.directives.forEach((handler, directiveName) => {
      const elements = container.querySelectorAll(`[${directiveName}]`);
      elements.forEach(element => {
        const value = element.getAttribute(directiveName);
        try {
          handler(element, value);
        } catch (error) {
          logger.error(`Error processing directive ${directiveName}:`, error);
        }
      });
    });
  }

  /**
   * Setup mutation observer to process directives on DOM changes
   */
  setupMutationObserver() {
    this.observer = new MutationObserver((mutations) => {
      let shouldProcess = false;
      
      mutations.forEach((mutation) => {
        if (mutation.type === 'childList') {
          mutation.addedNodes.forEach((node) => {
            if (node.nodeType === Node.ELEMENT_NODE) {
              // Check if the added node or its children have directives
              const hasDirectives = this.hasDirectives(node);
              if (hasDirectives) {
                shouldProcess = true;
              }
            }
          });
        }
      });
      
      if (shouldProcess) {
        // Debounce processing to avoid excessive calls
        clearTimeout(this.processTimeout);
        this.processTimeout = setTimeout(() => {
          this.processDirectives();
        }, 50);
      }
    });

    this.observer.observe(document.body, {
      childList: true,
      subtree: true
    });
  }

  /**
   * Check if element or its children have directives
   */
  hasDirectives(element) {
    // Check the element itself
    for (const directiveName of this.directives.keys()) {
      if (element.hasAttribute && element.hasAttribute(directiveName)) {
        return true;
      }
    }

    // Check children
    if (element.querySelectorAll) {
      for (const directiveName of this.directives.keys()) {
        if (element.querySelectorAll(`[${directiveName}]`).length > 0) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Refresh all directives (useful when auth state changes)
   */
  refresh() {
    logger.debug('Refreshing all directives');
    this.processDirectives();
  }

  /**
   * Destroy the directive manager
   */
  destroy() {
    if (this.observer) {
      this.observer.disconnect();
    }
    clearTimeout(this.processTimeout);
  }
}

// Create singleton instance
export const directiveManager = new DirectiveManager();

// Auto-refresh directives when auth state changes
stateManager.subscribe('auth.isAuthenticated', () => {
  setTimeout(() => directiveManager.refresh(), 100);
});

stateManager.subscribe('auth.user', () => {
  setTimeout(() => directiveManager.refresh(), 100);
});

// Process directives when DOM is ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    directiveManager.processDirectives();
  });
} else {
  directiveManager.processDirectives();
}

// Utility functions for manual directive processing
export const processDirectives = (container) => directiveManager.processDirectives(container);
export const refreshDirectives = () => directiveManager.refresh();
export const registerDirective = (name, handler) => directiveManager.register(name, handler);