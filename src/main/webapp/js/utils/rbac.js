/**
 * Role-Based Access Control (RBAC) Utility
 * Handles permissions, roles, and access control throughout the application
 */

import { stateManager } from './state.js';
import { logger } from './dev.js';

export class RBACManager {
  constructor() {
    this.permissions = new Map();
    this.roles = new Map();
    this.roleHierarchy = new Map();
    
    this.initializeDefaultRoles();
    this.initializeDefaultPermissions();
  }

  /**
   * Initialize default roles
   */
  initializeDefaultRoles() {
    // Define role hierarchy (child roles inherit parent permissions)
    this.roleHierarchy.set('SUPER_ADMIN', []);
    this.roleHierarchy.set('ADMIN', ['SUPER_ADMIN']);
    this.roleHierarchy.set('TEACHER', ['ADMIN']);
    this.roleHierarchy.set('STAFF', ['TEACHER']);
    this.roleHierarchy.set('STUDENT', ['STAFF']);

    // Define roles with their base permissions
    this.roles.set('SUPER_ADMIN', {
      name: 'Super Administrator',
      description: 'Full system access',
      permissions: ['*'] // Wildcard for all permissions
    });

    this.roles.set('ADMIN', {
      name: 'Administrator',
      description: 'Administrative access',
      permissions: [
        'VIEW_DASHBOARD',
        'MANAGE_USERS',
        'MANAGE_STUDENTS',
        'MANAGE_GRADES',
        'VIEW_REPORTS',
        'EXPORT_DATA',
        'MANAGE_SYSTEM_SETTINGS'
      ]
    });

    this.roles.set('TEACHER', {
      name: 'Teacher',
      description: 'Teaching staff access',
      permissions: [
        'VIEW_DASHBOARD',
        'VIEW_STUDENTS',
        'MANAGE_GRADES',
        'VIEW_REPORTS',
        'EXPORT_STUDENT_DATA'
      ]
    });

    this.roles.set('STAFF', {
      name: 'Staff',
      description: 'General staff access',
      permissions: [
        'VIEW_DASHBOARD',
        'VIEW_STUDENTS',
        'VIEW_BASIC_REPORTS'
      ]
    });

    this.roles.set('STUDENT', {
      name: 'Student',
      description: 'Student access',
      permissions: [
        'VIEW_OWN_PROFILE',
        'VIEW_OWN_GRADES',
        'VIEW_OWN_SCHEDULE'
      ]
    });
  }

  /**
   * Initialize default permissions
   */
  initializeDefaultPermissions() {
    const permissionGroups = {
      'Dashboard': [
        { key: 'VIEW_DASHBOARD', name: 'View Dashboard', description: 'Access to main dashboard' }
      ],
      'User Management': [
        { key: 'VIEW_USERS', name: 'View Users', description: 'View user list' },
        { key: 'CREATE_USER', name: 'Create User', description: 'Create new users' },
        { key: 'EDIT_USER', name: 'Edit User', description: 'Edit user information' },
        { key: 'DELETE_USER', name: 'Delete User', description: 'Delete users' },
        { key: 'MANAGE_USERS', name: 'Manage Users', description: 'Full user management' }
      ],
      'Student Management': [
        { key: 'VIEW_STUDENTS', name: 'View Students', description: 'View student list' },
        { key: 'CREATE_STUDENT', name: 'Create Student', description: 'Create new students' },
        { key: 'EDIT_STUDENT', name: 'Edit Student', description: 'Edit student information' },
        { key: 'DELETE_STUDENT', name: 'Delete Student', description: 'Delete students' },
        { key: 'MANAGE_STUDENTS', name: 'Manage Students', description: 'Full student management' }
      ],
      'Grade Management': [
        { key: 'VIEW_GRADES', name: 'View Grades', description: 'View grade information' },
        { key: 'EDIT_GRADES', name: 'Edit Grades', description: 'Edit grade information' },
        { key: 'MANAGE_GRADES', name: 'Manage Grades', description: 'Full grade management' }
      ],
      'Reports': [
        { key: 'VIEW_REPORTS', name: 'View Reports', description: 'Access to reports' },
        { key: 'VIEW_BASIC_REPORTS', name: 'View Basic Reports', description: 'Access to basic reports' },
        { key: 'EXPORT_DATA', name: 'Export Data', description: 'Export system data' },
        { key: 'EXPORT_STUDENT_DATA', name: 'Export Student Data', description: 'Export student data' }
      ],
      'System': [
        { key: 'MANAGE_SYSTEM_SETTINGS', name: 'Manage System Settings', description: 'Access to system settings' }
      ],
      'Personal': [
        { key: 'VIEW_OWN_PROFILE', name: 'View Own Profile', description: 'View own profile' },
        { key: 'VIEW_OWN_GRADES', name: 'View Own Grades', description: 'View own grades' },
        { key: 'VIEW_OWN_SCHEDULE', name: 'View Own Schedule', description: 'View own schedule' }
      ]
    };

    // Store permissions by group
    Object.entries(permissionGroups).forEach(([group, permissions]) => {
      permissions.forEach(permission => {
        this.permissions.set(permission.key, {
          ...permission,
          group
        });
      });
    });
  }

  /**
   * Get current user from state
   */
  getCurrentUser() {
    return stateManager.get('auth.user');
  }

  /**
   * Check if user has specific permission
   */
  hasPermission(permission) {
    const user = this.getCurrentUser();
    if (!user) return false;

    // Super admin has all permissions
    if (this.hasRole('SUPER_ADMIN')) return true;

    // Check direct permissions
    if (user.permissions && user.permissions.includes(permission)) return true;

    // Check role-based permissions
    const userRoles = user.roles || [user.role].filter(Boolean);
    return userRoles.some(role => this.roleHasPermission(role, permission));
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
    const user = this.getCurrentUser();
    if (!user) return false;

    const userRoles = user.roles || [user.role].filter(Boolean);
    return userRoles.includes(role);
  }

  /**
   * Check if user has any of the specified roles
   */
  hasAnyRole(roles) {
    return roles.some(role => this.hasRole(role));
  }

  /**
   * Check if role has specific permission
   */
  roleHasPermission(role, permission) {
    const roleData = this.roles.get(role);
    if (!roleData) return false;

    // Check for wildcard permission
    if (roleData.permissions.includes('*')) return true;

    // Check direct permission
    if (roleData.permissions.includes(permission)) return true;

    // Check inherited permissions from parent roles
    const parentRoles = this.roleHierarchy.get(role) || [];
    return parentRoles.some(parentRole => this.roleHasPermission(parentRole, permission));
  }

  /**
   * Get all permissions for a role
   */
  getRolePermissions(role) {
    const roleData = this.roles.get(role);
    if (!roleData) return [];

    if (roleData.permissions.includes('*')) {
      // Return all available permissions
      return Array.from(this.permissions.keys());
    }

    const permissions = new Set(roleData.permissions);

    // Add inherited permissions
    const parentRoles = this.roleHierarchy.get(role) || [];
    parentRoles.forEach(parentRole => {
      const parentPermissions = this.getRolePermissions(parentRole);
      parentPermissions.forEach(permission => permissions.add(permission));
    });

    return Array.from(permissions);
  }

  /**
   * Get user's effective permissions
   */
  getUserPermissions() {
    const user = this.getCurrentUser();
    if (!user) return [];

    const permissions = new Set();

    // Add direct permissions
    if (user.permissions) {
      user.permissions.forEach(permission => permissions.add(permission));
    }

    // Add role-based permissions
    const userRoles = user.roles || [user.role].filter(Boolean);
    userRoles.forEach(role => {
      const rolePermissions = this.getRolePermissions(role);
      rolePermissions.forEach(permission => permissions.add(permission));
    });

    return Array.from(permissions);
  }

  /**
   * Check if user can access route
   */
  canAccessRoute(route) {
    const routeConfig = this.getRouteConfig(route);
    if (!routeConfig) return true; // No restrictions

    // Check authentication requirement
    if (routeConfig.requiresAuth && !stateManager.get('auth.isAuthenticated')) {
      return false;
    }

    // Check role requirements
    if (routeConfig.roles && routeConfig.roles.length > 0) {
      if (!this.hasAnyRole(routeConfig.roles)) return false;
    }

    // Check permission requirements
    if (routeConfig.permissions && routeConfig.permissions.length > 0) {
      if (!this.hasAnyPermission(routeConfig.permissions)) return false;
    }

    return true;
  }

  /**
   * Get route configuration (this would be defined elsewhere)
   */
  getRouteConfig(route) {
    const routeConfigs = {
      'dashboard': {
        requiresAuth: true,
        permissions: ['VIEW_DASHBOARD']
      },
      'students': {
        requiresAuth: true,
        permissions: ['VIEW_STUDENTS', 'MANAGE_STUDENTS']
      },
      'users': {
        requiresAuth: true,
        permissions: ['VIEW_USERS', 'MANAGE_USERS']
      },
      'grades': {
        requiresAuth: true,
        permissions: ['VIEW_GRADES', 'MANAGE_GRADES']
      },
      'reports': {
        requiresAuth: true,
        permissions: ['VIEW_REPORTS', 'VIEW_BASIC_REPORTS']
      },
      'settings': {
        requiresAuth: true,
        roles: ['ADMIN', 'SUPER_ADMIN']
      }
    };

    return routeConfigs[route];
  }

  /**
   * Filter menu items based on permissions
   */
  filterMenuItems(menuItems) {
    return menuItems.filter(item => {
      if (item.route && !this.canAccessRoute(item.route)) return false;
      if (item.permissions && !this.hasAnyPermission(item.permissions)) return false;
      if (item.roles && !this.hasAnyRole(item.roles)) return false;
      
      // Filter sub-items recursively
      if (item.children) {
        item.children = this.filterMenuItems(item.children);
      }
      
      return true;
    });
  }

  /**
   * Show/hide UI elements based on permissions
   */
  applyUIPermissions() {
    // Hide elements that require specific permissions
    document.querySelectorAll('[data-requires-permission]').forEach(element => {
      const requiredPermissions = element.dataset.requiresPermission.split(',');
      if (!this.hasAnyPermission(requiredPermissions)) {
        element.style.display = 'none';
      } else {
        element.style.display = '';
      }
    });

    // Hide elements that require specific roles
    document.querySelectorAll('[data-requires-role]').forEach(element => {
      const requiredRoles = element.dataset.requiresRole.split(',');
      if (!this.hasAnyRole(requiredRoles)) {
        element.style.display = 'none';
      } else {
        element.style.display = '';
      }
    });

    // Show elements only for specific permissions
    document.querySelectorAll('[data-show-for-permission]').forEach(element => {
      const permissions = element.dataset.showForPermission.split(',');
      if (this.hasAnyPermission(permissions)) {
        element.style.display = '';
      } else {
        element.style.display = 'none';
      }
    });

    // Show elements only for specific roles
    document.querySelectorAll('[data-show-for-role]').forEach(element => {
      const roles = element.dataset.showForRole.split(',');
      if (this.hasAnyRole(roles)) {
        element.style.display = '';
      } else {
        element.style.display = 'none';
      }
    });
  }

  /**
   * Get user's role display name
   */
  getUserRoleDisplayName() {
    const user = this.getCurrentUser();
    if (!user) return 'Guest';

    const userRoles = user.roles || [user.role].filter(Boolean);
    if (userRoles.length === 0) return 'User';

    // Return the highest role
    const roleOrder = ['SUPER_ADMIN', 'ADMIN', 'TEACHER', 'STAFF', 'STUDENT'];
    for (const role of roleOrder) {
      if (userRoles.includes(role)) {
        const roleData = this.roles.get(role);
        return roleData ? roleData.name : role;
      }
    }

    return userRoles[0];
  }

  /**
   * Check if user can perform action on resource
   */
  canPerformAction(action, resource, resourceOwnerId = null) {
    const user = this.getCurrentUser();
    if (!user) return false;

    // Super admin can do everything
    if (this.hasRole('SUPER_ADMIN')) return true;

    // Check if user owns the resource
    const isOwner = resourceOwnerId && user.id === resourceOwnerId;

    // Define action-permission mappings
    const actionPermissions = {
      'view': {
        'student': ['VIEW_STUDENTS', 'MANAGE_STUDENTS'],
        'user': ['VIEW_USERS', 'MANAGE_USERS'],
        'grade': ['VIEW_GRADES', 'MANAGE_GRADES'],
        'report': ['VIEW_REPORTS', 'VIEW_BASIC_REPORTS']
      },
      'create': {
        'student': ['CREATE_STUDENT', 'MANAGE_STUDENTS'],
        'user': ['CREATE_USER', 'MANAGE_USERS'],
        'grade': ['EDIT_GRADES', 'MANAGE_GRADES']
      },
      'edit': {
        'student': ['EDIT_STUDENT', 'MANAGE_STUDENTS'],
        'user': ['EDIT_USER', 'MANAGE_USERS'],
        'grade': ['EDIT_GRADES', 'MANAGE_GRADES']
      },
      'delete': {
        'student': ['DELETE_STUDENT', 'MANAGE_STUDENTS'],
        'user': ['DELETE_USER', 'MANAGE_USERS']
      }
    };

    // Special cases for own resources
    if (isOwner) {
      const ownResourcePermissions = {
        'view': {
          'profile': ['VIEW_OWN_PROFILE'],
          'grade': ['VIEW_OWN_GRADES'],
          'schedule': ['VIEW_OWN_SCHEDULE']
        }
      };

      const ownPermissions = ownResourcePermissions[action]?.[resource];
      if (ownPermissions && this.hasAnyPermission(ownPermissions)) {
        return true;
      }
    }

    // Check general permissions
    const requiredPermissions = actionPermissions[action]?.[resource];
    if (requiredPermissions) {
      return this.hasAnyPermission(requiredPermissions);
    }

    return false;
  }

  /**
   * Get permission groups for UI display
   */
  getPermissionGroups() {
    const groups = {};
    
    this.permissions.forEach((permission, key) => {
      if (!groups[permission.group]) {
        groups[permission.group] = [];
      }
      groups[permission.group].push({
        key,
        ...permission
      });
    });

    return groups;
  }

  /**
   * Get available roles
   */
  getAvailableRoles() {
    return Array.from(this.roles.entries()).map(([key, role]) => ({
      key,
      ...role
    }));
  }

  /**
   * Debug current user permissions
   */
  debugUserPermissions() {
    const user = this.getCurrentUser();
    if (!user) {
      logger.debug('No authenticated user');
      return;
    }

    logger.group('User Permissions Debug');
    logger.debug('User:', user);
    logger.debug('Roles:', user.roles || [user.role]);
    logger.debug('Direct Permissions:', user.permissions || []);
    logger.debug('Effective Permissions:', this.getUserPermissions());
    logger.debug('Role Display Name:', this.getUserRoleDisplayName());
    logger.groupEnd();
  }
}

// Create singleton instance
export const rbacManager = new RBACManager();

// Utility functions for easy access
export const hasPermission = (permission) => rbacManager.hasPermission(permission);
export const hasAnyPermission = (permissions) => rbacManager.hasAnyPermission(permissions);
export const hasAllPermissions = (permissions) => rbacManager.hasAllPermissions(permissions);
export const hasRole = (role) => rbacManager.hasRole(role);
export const hasAnyRole = (roles) => rbacManager.hasAnyRole(roles);
export const canAccessRoute = (route) => rbacManager.canAccessRoute(route);
export const canPerformAction = (action, resource, ownerId) => rbacManager.canPerformAction(action, resource, ownerId);

// Apply permissions when authentication state changes
stateManager.subscribe('auth.isAuthenticated', (isAuthenticated) => {
  if (isAuthenticated) {
    // Apply UI permissions when user logs in
    setTimeout(() => rbacManager.applyUIPermissions(), 100);
  }
});

stateManager.subscribe('auth.user', (user) => {
  if (user) {
    // Reapply UI permissions when user data changes
    setTimeout(() => rbacManager.applyUIPermissions(), 100);
  }
});