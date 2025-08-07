/**
 * Login Form Component
 * Handles user authentication with validation and error handling
 */

import { validator } from '../utils/validation.js';
import { layoutManager } from '../utils/layout.js';
import { logger } from '../utils/dev.js';

export class LoginForm {
  constructor(container, options = {}) {
    this.container = container;
    this.options = {
      showRememberMe: true,
      showForgotPassword: true,
      autoFocus: true,
      ...options
    };
    
    this.form = null;
    this.isSubmitting = false;
    this.validationSchema = {
      identifier: ['required', 'minLength:3'],
      password: ['required', 'minLength:6']
    };
    
    this.init();
  }

  /**
   * Initialize login form
   */
  init() {
    this.render();
    this.setupEventListeners();
    this.setupValidation();
    
    if (this.options.autoFocus) {
      this.focusFirstField();
    }
  }

  /**
   * Render login form
   */
  render() {
    const formHtml = `
      <div class="login-form-container">
        <div class="text-center mb-4">
          <div class="login-logo mb-3">
            <i class="fas fa-graduation-cap text-primary" style="font-size: 3rem;"></i>
          </div>
          <h4 class="login-title">Welcome Back</h4>
          <p class="text-muted login-subtitle">Sign in to your account</p>
        </div>

        <form id="login-form" class="login-form" novalidate>
          <!-- Alert Container -->
          <div id="login-alert-container" class="mb-3">
            <!-- Login alerts will be displayed here -->
          </div>

          <!-- Identifier Field -->
          <div class="form-floating mb-3">
            <input 
              type="text" 
              class="form-control" 
              id="login-identifier" 
              name="identifier" 
              placeholder="Email or Username"
              autocomplete="username"
              required
              data-validate="required|minLength:3"
            >
            <label for="login-identifier">
              <i class="fas fa-user me-2"></i>Email or Username
            </label>
            <div class="invalid-feedback"></div>
          </div>

          <!-- Password Field -->
          <div class="form-floating mb-3">
            <input 
              type="password" 
              class="form-control" 
              id="login-password" 
              name="password" 
              placeholder="Password"
              autocomplete="current-password"
              required
              data-validate="required|minLength:6"
            >
            <label for="login-password">
              <i class="fas fa-lock me-2"></i>Password
            </label>
            <div class="invalid-feedback"></div>
            <div class="password-toggle">
              <button type="button" class="btn btn-link btn-sm p-0 password-toggle-btn" 
                      aria-label="Toggle password visibility">
                <i class="fas fa-eye"></i>
              </button>
            </div>
          </div>

          <!-- Remember Me & Forgot Password -->
          <div class="d-flex justify-content-between align-items-center mb-4">
            ${this.options.showRememberMe ? `
              <div class="form-check">
                <input type="checkbox" class="form-check-input" id="remember-me" name="rememberMe">
                <label class="form-check-label" for="remember-me">
                  Remember me
                </label>
              </div>
            ` : '<div></div>'}
            
            ${this.options.showForgotPassword ? `
              <a href="#" class="text-decoration-none forgot-password-link" 
                 data-bs-toggle="modal" data-bs-target="#forgotPasswordModal">
                Forgot password?
              </a>
            ` : ''}
          </div>

          <!-- Submit Button -->
          <div class="d-grid mb-3">
            <button type="submit" class="btn btn-primary btn-lg login-submit-btn" id="login-submit-btn">
              <span class="spinner-border spinner-border-sm me-2 d-none login-spinner"></span>
              <i class="fas fa-sign-in-alt me-2 login-icon"></i>
              <span class="login-text">Sign In</span>
            </button>
          </div>

          <!-- Additional Options -->
          <div class="text-center">
            <small class="text-muted">
              Having trouble? <a href="#support" class="text-decoration-none">Contact Support</a>
            </small>
          </div>
        </form>

        <!-- Login Attempts Warning -->
        <div id="login-attempts-warning" class="alert alert-warning mt-3 d-none">
          <i class="fas fa-exclamation-triangle me-2"></i>
          <strong>Security Notice:</strong> Multiple failed login attempts detected. 
          Your account may be temporarily locked for security.
        </div>
      </div>
    `;

    this.container.innerHTML = formHtml;
    this.form = this.container.querySelector('#login-form');
  }

  /**
   * Setup event listeners
   */
  setupEventListeners() {
    // Form submission
    this.form.addEventListener('submit', (e) => this.handleSubmit(e));

    // Password toggle
    const passwordToggle = this.container.querySelector('.password-toggle-btn');
    if (passwordToggle) {
      passwordToggle.addEventListener('click', () => this.togglePasswordVisibility());
    }

    // Real-time validation
    const inputs = this.form.querySelectorAll('input[data-validate]');
    inputs.forEach(input => {
      input.addEventListener('blur', () => this.validateField(input));
      input.addEventListener('input', () => this.clearFieldError(input));
    });

    // Keyboard shortcuts
    this.form.addEventListener('keydown', (e) => this.handleKeydown(e));

    // Forgot password link
    const forgotLink = this.container.querySelector('.forgot-password-link');
    if (forgotLink) {
      forgotLink.addEventListener('click', (e) => this.handleForgotPassword(e));
    }
  }

  /**
   * Setup form validation
   */
  setupValidation() {
    validator.setupRealTimeValidation(this.form);
    
    // Custom validation for login attempts
    this.form.addEventListener('validSubmit', (e) => {
      this.submitForm(e.detail.data);
    });
  }

  /**
   * Handle form submission
   */
  async handleSubmit(e) {
    e.preventDefault();
    
    if (this.isSubmitting) return;
    
    // Clear previous alerts
    this.clearAlerts();
    
    // Validate form
    const isValid = await validator.validateFormElement(this.form);
    if (!isValid) {
      this.showAlert('danger', 'Please correct the errors below');
      return;
    }
    
    // Submit form
    const formData = new FormData(this.form);
    await this.submitForm(formData);
  }

  /**
   * Submit form data
   */
  async submitForm(formData) {
    try {
      this.setSubmittingState(true);
      
      const loginData = {
        identifier: formData.get('identifier'),
        password: formData.get('password'),
        rememberMe: formData.get('rememberMe') === 'on'
      };

      logger.debug('Attempting login with:', { identifier: loginData.identifier });

      // Dispatch login event
      const loginEvent = new CustomEvent('loginAttempt', {
        detail: { loginData },
        bubbles: true
      });
      
      this.form.dispatchEvent(loginEvent);

    } catch (error) {
      logger.error('Login form submission error:', error);
      this.handleLoginError(error);
    } finally {
      this.setSubmittingState(false);
    }
  }

  /**
   * Handle login success
   */
  handleLoginSuccess(userData) {
    logger.info('Login successful for user:', userData.username);
    
    this.showAlert('success', 'Login successful! Redirecting...');
    
    // Add success animation
    this.form.classList.add('login-success');
    
    // Clear form
    setTimeout(() => {
      this.form.reset();
      this.clearAllErrors();
    }, 1000);

    // Dispatch success event
    const successEvent = new CustomEvent('loginSuccess', {
      detail: { userData },
      bubbles: true
    });
    
    this.form.dispatchEvent(successEvent);
  }

  /**
   * Handle login error
   */
  handleLoginError(error) {
    logger.error('Login error:', error);
    
    let message = 'Login failed. Please try again.';
    let type = 'danger';
    
    if (error.status === 401) {
      message = 'Invalid email/username or password.';
    } else if (error.status === 429) {
      message = 'Too many login attempts. Please try again later.';
      this.showAttemptsWarning();
    } else if (error.status === 423) {
      message = 'Account is temporarily locked. Please contact support.';
      type = 'warning';
    } else if (error.message) {
      message = error.message;
    }
    
    this.showAlert(type, message);
    
    // Focus back to identifier field
    this.focusFirstField();
    
    // Add shake animation
    this.form.classList.add('login-error');
    setTimeout(() => {
      this.form.classList.remove('login-error');
    }, 500);

    // Dispatch error event
    const errorEvent = new CustomEvent('loginError', {
      detail: { error },
      bubbles: true
    });
    
    this.form.dispatchEvent(errorEvent);
  }

  /**
   * Set submitting state
   */
  setSubmittingState(isSubmitting) {
    this.isSubmitting = isSubmitting;
    
    const submitBtn = this.container.querySelector('#login-submit-btn');
    const spinner = this.container.querySelector('.login-spinner');
    const icon = this.container.querySelector('.login-icon');
    const text = this.container.querySelector('.login-text');
    
    if (isSubmitting) {
      submitBtn.disabled = true;
      spinner.classList.remove('d-none');
      icon.classList.add('d-none');
      text.textContent = 'Signing In...';
      
      // Disable form inputs
      const inputs = this.form.querySelectorAll('input, button');
      inputs.forEach(input => {
        if (input !== submitBtn) {
          input.disabled = true;
        }
      });
      
    } else {
      submitBtn.disabled = false;
      spinner.classList.add('d-none');
      icon.classList.remove('d-none');
      text.textContent = 'Sign In';
      
      // Re-enable form inputs
      const inputs = this.form.querySelectorAll('input, button');
      inputs.forEach(input => {
        input.disabled = false;
      });
    }
  }

  /**
   * Toggle password visibility
   */
  togglePasswordVisibility() {
    const passwordInput = this.container.querySelector('#login-password');
    const toggleBtn = this.container.querySelector('.password-toggle-btn');
    const icon = toggleBtn.querySelector('i');
    
    if (passwordInput.type === 'password') {
      passwordInput.type = 'text';
      icon.className = 'fas fa-eye-slash';
      toggleBtn.setAttribute('aria-label', 'Hide password');
    } else {
      passwordInput.type = 'password';
      icon.className = 'fas fa-eye';
      toggleBtn.setAttribute('aria-label', 'Show password');
    }
  }

  /**
   * Validate individual field
   */
  async validateField(field) {
    const rules = field.dataset.validate ? field.dataset.validate.split('|') : [];
    const value = field.value;
    const errors = await validator.validateValue(value, rules);
    
    if (errors.length > 0) {
      this.showFieldError(field, errors[0]);
      return false;
    } else {
      this.clearFieldError(field);
      return true;
    }
  }

  /**
   * Show field error
   */
  showFieldError(field, message) {
    field.classList.add('is-invalid');
    
    let feedback = field.parentNode.querySelector('.invalid-feedback');
    if (!feedback) {
      feedback = document.createElement('div');
      feedback.className = 'invalid-feedback';
      field.parentNode.appendChild(feedback);
    }
    
    feedback.textContent = message;
  }

  /**
   * Clear field error
   */
  clearFieldError(field) {
    field.classList.remove('is-invalid');
    const feedback = field.parentNode.querySelector('.invalid-feedback');
    if (feedback) {
      feedback.textContent = '';
    }
  }

  /**
   * Clear all field errors
   */
  clearAllErrors() {
    const invalidFields = this.form.querySelectorAll('.is-invalid');
    invalidFields.forEach(field => this.clearFieldError(field));
  }

  /**
   * Show alert
   */
  showAlert(type, message) {
    const alertContainer = this.container.querySelector('#login-alert-container');
    
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} alert-dismissible fade show`;
    alert.innerHTML = `
      <i class="fas ${this.getAlertIcon(type)} me-2"></i>
      ${message}
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    alertContainer.innerHTML = '';
    alertContainer.appendChild(alert);
    
    // Auto-dismiss success alerts
    if (type === 'success') {
      setTimeout(() => {
        if (alert.parentNode) {
          alert.remove();
        }
      }, 3000);
    }
  }

  /**
   * Clear alerts
   */
  clearAlerts() {
    const alertContainer = this.container.querySelector('#login-alert-container');
    alertContainer.innerHTML = '';
  }

  /**
   * Get alert icon
   */
  getAlertIcon(type) {
    const icons = {
      success: 'fa-check-circle',
      danger: 'fa-exclamation-circle',
      warning: 'fa-exclamation-triangle',
      info: 'fa-info-circle'
    };
    return icons[type] || icons.info;
  }

  /**
   * Show attempts warning
   */
  showAttemptsWarning() {
    const warning = this.container.querySelector('#login-attempts-warning');
    warning.classList.remove('d-none');
    
    setTimeout(() => {
      warning.classList.add('d-none');
    }, 10000);
  }

  /**
   * Focus first field
   */
  focusFirstField() {
    const firstField = this.form.querySelector('#login-identifier');
    if (firstField) {
      setTimeout(() => firstField.focus(), 100);
    }
  }

  /**
   * Handle keyboard shortcuts
   */
  handleKeydown(e) {
    // Enter key on any field submits form
    if (e.key === 'Enter' && !this.isSubmitting) {
      e.preventDefault();
      this.handleSubmit(e);
    }
    
    // Escape key clears form
    if (e.key === 'Escape') {
      this.form.reset();
      this.clearAllErrors();
      this.clearAlerts();
    }
  }

  /**
   * Handle forgot password
   */
  handleForgotPassword(e) {
    e.preventDefault();
    
    // Pre-fill email if available
    const identifier = this.form.querySelector('#login-identifier').value;
    if (identifier && identifier.includes('@')) {
      const forgotEmailField = document.querySelector('#reset-email');
      if (forgotEmailField) {
        forgotEmailField.value = identifier;
      }
    }
  }

  /**
   * Get form data
   */
  getFormData() {
    const formData = new FormData(this.form);
    return {
      identifier: formData.get('identifier'),
      password: formData.get('password'),
      rememberMe: formData.get('rememberMe') === 'on'
    };
  }

  /**
   * Reset form
   */
  reset() {
    this.form.reset();
    this.clearAllErrors();
    this.clearAlerts();
    this.setSubmittingState(false);
    
    const warning = this.container.querySelector('#login-attempts-warning');
    warning.classList.add('d-none');
  }

  /**
   * Destroy component
   */
  destroy() {
    if (this.form) {
      this.form.removeEventListener('submit', this.handleSubmit);
    }
    
    this.container.innerHTML = '';
  }
}

export default LoginForm;