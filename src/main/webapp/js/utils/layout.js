/**
 * Layout Utility
 * Handles responsive layout, theme management, and UI state
 */

export class LayoutManager {
  constructor() {
    this.currentTheme = 'light';
    this.breakpoints = {
      xs: 0,
      sm: 576,
      md: 768,
      lg: 992,
      xl: 1200,
      xxl: 1400
    };
    this.init();
  }

  /**
   * Initialize layout manager
   */
  init() {
    this.loadTheme();
    this.setupThemeToggle();
    this.setupResponsiveHandlers();
    this.setupAccessibility();
    this.setupOfflineDetection();
  }

  /**
   * Load saved theme
   */
  loadTheme() {
    const savedTheme = localStorage.getItem('sim-theme') || 'light';
    this.setTheme(savedTheme);
  }

  /**
   * Set theme
   */
  setTheme(theme) {
    this.currentTheme = theme;
    document.documentElement.setAttribute('data-bs-theme', theme);
    localStorage.setItem('sim-theme', theme);
    
    // Update theme toggle icon
    const themeToggle = document.getElementById('theme-toggle');
    if (themeToggle) {
      const icon = themeToggle.querySelector('i');
      if (icon) {
        icon.className = theme === 'dark' ? 'fas fa-sun' : 'fas fa-moon';
      }
    }

    // Dispatch theme change event
    window.dispatchEvent(new CustomEvent('themeChanged', { detail: { theme } }));
  }

  /**
   * Toggle theme
   */
  toggleTheme() {
    const newTheme = this.currentTheme === 'light' ? 'dark' : 'light';
    this.setTheme(newTheme);
  }

  /**
   * Setup theme toggle button
   */
  setupThemeToggle() {
    const themeToggle = document.getElementById('theme-toggle');
    if (themeToggle) {
      themeToggle.addEventListener('click', () => {
        this.toggleTheme();
      });
    }
  }

  /**
   * Setup responsive handlers
   */
  setupResponsiveHandlers() {
    // Handle window resize
    let resizeTimeout;
    window.addEventListener('resize', () => {
      clearTimeout(resizeTimeout);
      resizeTimeout = setTimeout(() => {
        this.handleResize();
      }, 250);
    });

    // Initial resize handling
    this.handleResize();
  }

  /**
   * Handle window resize
   */
  handleResize() {
    const width = window.innerWidth;
    const currentBreakpoint = this.getCurrentBreakpoint(width);
    
    // Update body class for current breakpoint
    document.body.className = document.body.className.replace(/\bbreakpoint-\w+\b/g, '');
    document.body.classList.add(`breakpoint-${currentBreakpoint}`);

    // Handle mobile navigation
    this.handleMobileNavigation(currentBreakpoint);

    // Handle table responsiveness
    this.handleTableResponsiveness(currentBreakpoint);

    // Dispatch resize event
    window.dispatchEvent(new CustomEvent('layoutResize', { 
      detail: { width, breakpoint: currentBreakpoint } 
    }));
  }

  /**
   * Get current breakpoint
   */
  getCurrentBreakpoint(width) {
    if (width >= this.breakpoints.xxl) return 'xxl';
    if (width >= this.breakpoints.xl) return 'xl';
    if (width >= this.breakpoints.lg) return 'lg';
    if (width >= this.breakpoints.md) return 'md';
    if (width >= this.breakpoints.sm) return 'sm';
    return 'xs';
  }

  /**
   * Handle mobile navigation
   */
  handleMobileNavigation(breakpoint) {
    const navbar = document.querySelector('.navbar-collapse');
    if (navbar && ['xs', 'sm'].includes(breakpoint)) {
      // Auto-collapse navbar on mobile after navigation
      const navLinks = navbar.querySelectorAll('.nav-link[data-route]');
      navLinks.forEach(link => {
        link.addEventListener('click', () => {
          const bsCollapse = bootstrap.Collapse.getInstance(navbar);
          if (bsCollapse) {
            bsCollapse.hide();
          }
        });
      });
    }
  }

  /**
   * Handle table responsiveness
   */
  handleTableResponsiveness(breakpoint) {
    const tables = document.querySelectorAll('.data-table');
    tables.forEach(table => {
      const wrapper = table.closest('.table-responsive');
      if (wrapper) {
        if (['xs', 'sm'].includes(breakpoint)) {
          wrapper.classList.add('table-responsive-sm');
        } else {
          wrapper.classList.remove('table-responsive-sm');
        }
      }
    });
  }

  /**
   * Setup accessibility features
   */
  setupAccessibility() {
    // Skip to content functionality
    const skipLink = document.querySelector('.visually-hidden-focusable');
    if (skipLink) {
      skipLink.addEventListener('click', (e) => {
        e.preventDefault();
        const target = document.querySelector(skipLink.getAttribute('href'));
        if (target) {
          target.focus();
          target.scrollIntoView({ behavior: 'smooth' });
        }
      });
    }

    // Keyboard navigation for dropdowns
    this.setupKeyboardNavigation();

    // Focus management for modals
    this.setupModalFocusManagement();

    // ARIA live regions for dynamic content
    this.setupAriaLiveRegions();
  }

  /**
   * Setup keyboard navigation
   */
  setupKeyboardNavigation() {
    // Handle escape key for modals and dropdowns
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') {
        // Close open dropdowns
        const openDropdowns = document.querySelectorAll('.dropdown-menu.show');
        openDropdowns.forEach(dropdown => {
          const toggle = dropdown.previousElementSibling;
          if (toggle) {
            bootstrap.Dropdown.getInstance(toggle)?.hide();
          }
        });
      }
    });

    // Arrow key navigation for menus
    document.addEventListener('keydown', (e) => {
      if (['ArrowUp', 'ArrowDown'].includes(e.key)) {
        const activeElement = document.activeElement;
        if (activeElement.classList.contains('dropdown-item')) {
          e.preventDefault();
          const items = Array.from(activeElement.closest('.dropdown-menu').querySelectorAll('.dropdown-item'));
          const currentIndex = items.indexOf(activeElement);
          
          let nextIndex;
          if (e.key === 'ArrowDown') {
            nextIndex = currentIndex < items.length - 1 ? currentIndex + 1 : 0;
          } else {
            nextIndex = currentIndex > 0 ? currentIndex - 1 : items.length - 1;
          }
          
          items[nextIndex].focus();
        }
      }
    });
  }

  /**
   * Setup modal focus management
   */
  setupModalFocusManagement() {
    document.addEventListener('shown.bs.modal', (e) => {
      const modal = e.target;
      const firstFocusable = modal.querySelector('input, select, textarea, button, [tabindex]:not([tabindex="-1"])');
      if (firstFocusable) {
        firstFocusable.focus();
      }
    });
  }

  /**
   * Setup ARIA live regions
   */
  setupAriaLiveRegions() {
    // Create live region for announcements
    if (!document.getElementById('aria-live-region')) {
      const liveRegion = document.createElement('div');
      liveRegion.id = 'aria-live-region';
      liveRegion.setAttribute('aria-live', 'polite');
      liveRegion.setAttribute('aria-atomic', 'true');
      liveRegion.className = 'visually-hidden';
      document.body.appendChild(liveRegion);
    }
  }

  /**
   * Announce message to screen readers
   */
  announce(message) {
    const liveRegion = document.getElementById('aria-live-region');
    if (liveRegion) {
      liveRegion.textContent = message;
      setTimeout(() => {
        liveRegion.textContent = '';
      }, 1000);
    }
  }

  /**
   * Setup offline detection
   */
  setupOfflineDetection() {
    const updateOnlineStatus = () => {
      const offlineBanner = document.getElementById('offline-banner');
      if (offlineBanner) {
        if (navigator.onLine) {
          offlineBanner.classList.add('d-none');
        } else {
          offlineBanner.classList.remove('d-none');
        }
      }
    };

    window.addEventListener('online', updateOnlineStatus);
    window.addEventListener('offline', updateOnlineStatus);
    updateOnlineStatus();
  }

  /**
   * Update page header
   */
  updatePageHeader(title, description = '', actions = []) {
    const titleElement = document.getElementById('page-title');
    const descriptionElement = document.getElementById('page-description');
    const toolbarElement = document.getElementById('page-toolbar');

    if (titleElement) {
      titleElement.textContent = title;
      document.title = `${title} - SIM`;
    }

    if (descriptionElement) {
      descriptionElement.textContent = description;
    }

    if (toolbarElement) {
      toolbarElement.innerHTML = '';
      actions.forEach(action => {
        const button = document.createElement('button');
        button.className = `btn ${action.class || 'btn-primary'}`;
        button.innerHTML = `${action.icon ? `<i class="${action.icon} me-1"></i>` : ''}${action.text}`;
        if (action.onClick) {
          button.addEventListener('click', action.onClick);
        }
        toolbarElement.appendChild(button);
      });
    }
  }

  /**
   * Show loading overlay
   */
  showLoadingOverlay(container, message = 'Loading...') {
    const overlay = document.createElement('div');
    overlay.className = 'loading-overlay';
    overlay.innerHTML = `
      <div class="text-center">
        <div class="spinner-border text-primary mb-2" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
        <div class="text-muted">${message}</div>
      </div>
    `;
    
    container.style.position = 'relative';
    container.appendChild(overlay);
    
    return overlay;
  }

  /**
   * Hide loading overlay
   */
  hideLoadingOverlay(container) {
    const overlay = container.querySelector('.loading-overlay');
    if (overlay) {
      overlay.remove();
    }
  }

  /**
   * Show alert in alert container
   */
  showAlert(type, title, message, dismissible = true) {
    const alertContainer = document.getElementById('alert-container');
    if (!alertContainer) return;

    const alert = document.createElement('div');
    alert.className = `alert alert-${type} ${dismissible ? 'alert-dismissible' : ''} fade show`;
    alert.setAttribute('role', 'alert');

    const icon = this.getAlertIcon(type);
    alert.innerHTML = `
      <div class="d-flex align-items-center">
        <i class="${icon} me-2"></i>
        <div class="flex-grow-1">
          <strong>${title}</strong>
          <div>${message}</div>
        </div>
      </div>
      ${dismissible ? '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>' : ''}
    `;

    alertContainer.appendChild(alert);

    // Auto-dismiss after 5 seconds for non-error alerts
    if (type !== 'danger' && dismissible) {
      setTimeout(() => {
        if (alert.parentNode) {
          const bsAlert = bootstrap.Alert.getInstance(alert);
          if (bsAlert) {
            bsAlert.close();
          }
        }
      }, 5000);
    }

    return alert;
  }

  /**
   * Get alert icon
   */
  getAlertIcon(type) {
    const icons = {
      success: 'fas fa-check-circle text-success',
      danger: 'fas fa-exclamation-circle text-danger',
      warning: 'fas fa-exclamation-triangle text-warning',
      info: 'fas fa-info-circle text-info',
      primary: 'fas fa-info-circle text-primary'
    };
    return icons[type] || icons.info;
  }

  /**
   * Clear all alerts
   */
  clearAlerts() {
    const alertContainer = document.getElementById('alert-container');
    if (alertContainer) {
      alertContainer.innerHTML = '';
    }
  }

  /**
   * Update notification badge
   */
  updateNotificationBadge(count) {
    const badge = document.getElementById('notification-badge');
    if (badge) {
      if (count > 0) {
        badge.textContent = count > 99 ? '99+' : count.toString();
        badge.classList.remove('d-none');
      } else {
        badge.classList.add('d-none');
      }
    }
  }

  /**
   * Get current theme
   */
  getCurrentTheme() {
    return this.currentTheme;
  }

  /**
   * Check if mobile
   */
  isMobile() {
    return window.innerWidth < this.breakpoints.md;
  }

  /**
   * Check if tablet
   */
  isTablet() {
    const width = window.innerWidth;
    return width >= this.breakpoints.md && width < this.breakpoints.lg;
  }

  /**
   * Check if desktop
   */
  isDesktop() {
    return window.innerWidth >= this.breakpoints.lg;
  }
}

// Create singleton instance
export const layoutManager = new LayoutManager();