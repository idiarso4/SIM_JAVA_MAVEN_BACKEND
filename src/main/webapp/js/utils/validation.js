/**
 * Validation Utility
 * Form validation and data validation functions
 */

export class Validator {
  constructor() {
    this.rules = new Map();
    this.messages = new Map();
    this.setupDefaultRules();
    this.setupDefaultMessages();
  }

  /**
   * Setup default validation rules
   */
  setupDefaultRules() {
    this.addRule('required', (value) => {
      if (Array.isArray(value)) return value.length > 0;
      if (typeof value === 'string') return value.trim().length > 0;
      return value !== null && value !== undefined && value !== '';
    });

    this.addRule('email', (value) => {
      if (!value) return true; // Allow empty for optional fields
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return emailRegex.test(value);
    });

    this.addRule('min', (value, min) => {
      if (!value) return true;
      if (typeof value === 'string') return value.length >= min;
      if (typeof value === 'number') return value >= min;
      if (Array.isArray(value)) return value.length >= min;
      return true;
    });

    this.addRule('max', (value, max) => {
      if (!value) return true;
      if (typeof value === 'string') return value.length <= max;
      if (typeof value === 'number') return value <= max;
      if (Array.isArray(value)) return value.length <= max;
      return true;
    });

    this.addRule('minLength', (value, min) => {
      if (!value) return true;
      return value.toString().length >= min;
    });

    this.addRule('maxLength', (value, max) => {
      if (!value) return true;
      return value.toString().length <= max;
    });

    this.addRule('pattern', (value, pattern) => {
      if (!value) return true;
      const regex = new RegExp(pattern);
      return regex.test(value);
    });

    this.addRule('numeric', (value) => {
      if (!value) return true;
      return !isNaN(value) && !isNaN(parseFloat(value));
    });

    this.addRule('integer', (value) => {
      if (!value) return true;
      return Number.isInteger(Number(value));
    });

    this.addRule('phone', (value) => {
      if (!value) return true;
      const phoneRegex = /^[\+]?[1-9][\d]{0,15}$/;
      return phoneRegex.test(value.replace(/[\s\-\(\)]/g, ''));
    });

    this.addRule('url', (value) => {
      if (!value) return true;
      try {
        new URL(value);
        return true;
      } catch {
        return false;
      }
    });

    this.addRule('date', (value) => {
      if (!value) return true;
      const date = new Date(value);
      return date instanceof Date && !isNaN(date);
    });

    this.addRule('dateAfter', (value, afterDate) => {
      if (!value) return true;
      const date = new Date(value);
      const after = new Date(afterDate);
      return date > after;
    });

    this.addRule('dateBefore', (value, beforeDate) => {
      if (!value) return true;
      const date = new Date(value);
      const before = new Date(beforeDate);
      return date < before;
    });

    this.addRule('confirmed', (value, confirmValue) => {
      return value === confirmValue;
    });

    this.addRule('unique', async (value, existingValues) => {
      if (!value) return true;
      if (Array.isArray(existingValues)) {
        return !existingValues.includes(value);
      }
      if (typeof existingValues === 'function') {
        return !(await existingValues(value));
      }
      return true;
    });

    this.addRule('fileSize', (file, maxSize) => {
      if (!file) return true;
      return file.size <= maxSize;
    });

    this.addRule('fileType', (file, allowedTypes) => {
      if (!file) return true;
      const types = Array.isArray(allowedTypes) ? allowedTypes : [allowedTypes];
      return types.some(type => {
        if (type.startsWith('.')) {
          return file.name.toLowerCase().endsWith(type.toLowerCase());
        }
        return file.type === type;
      });
    });
  }

  /**
   * Setup default error messages
   */
  setupDefaultMessages() {
    this.addMessage('required', 'This field is required');
    this.addMessage('email', 'Please enter a valid email address');
    this.addMessage('min', 'Value must be at least {0}');
    this.addMessage('max', 'Value must be at most {0}');
    this.addMessage('minLength', 'Must be at least {0} characters');
    this.addMessage('maxLength', 'Must be at most {0} characters');
    this.addMessage('pattern', 'Invalid format');
    this.addMessage('numeric', 'Must be a number');
    this.addMessage('integer', 'Must be a whole number');
    this.addMessage('phone', 'Please enter a valid phone number');
    this.addMessage('url', 'Please enter a valid URL');
    this.addMessage('date', 'Please enter a valid date');
    this.addMessage('dateAfter', 'Date must be after {0}');
    this.addMessage('dateBefore', 'Date must be before {0}');
    this.addMessage('confirmed', 'Values do not match');
    this.addMessage('unique', 'This value already exists');
    this.addMessage('fileSize', 'File size must be less than {0}');
    this.addMessage('fileType', 'Invalid file type');
  }

  /**
   * Add validation rule
   */
  addRule(name, validator) {
    this.rules.set(name, validator);
  }

  /**
   * Add error message
   */
  addMessage(rule, message) {
    this.messages.set(rule, message);
  }

  /**
   * Validate single value
   */
  async validateValue(value, rules) {
    const errors = [];

    for (const rule of rules) {
      const { name, params, message } = this.parseRule(rule);
      const validator = this.rules.get(name);

      if (!validator) {
        console.warn(`Unknown validation rule: ${name}`);
        continue;
      }

      try {
        const isValid = await validator(value, ...params);
        if (!isValid) {
          const errorMessage = message || this.formatMessage(name, params);
          errors.push(errorMessage);
        }
      } catch (error) {
        console.error(`Validation error for rule ${name}:`, error);
        errors.push('Validation failed');
      }
    }

    return errors;
  }

  /**
   * Validate form data
   */
  async validateForm(data, schema) {
    const errors = {};
    const promises = [];

    Object.entries(schema).forEach(([field, rules]) => {
      const value = data[field];
      const promise = this.validateValue(value, rules).then(fieldErrors => {
        if (fieldErrors.length > 0) {
          errors[field] = fieldErrors;
        }
      });
      promises.push(promise);
    });

    await Promise.all(promises);
    return errors;
  }

  /**
   * Parse validation rule
   */
  parseRule(rule) {
    if (typeof rule === 'string') {
      const parts = rule.split(':');
      const name = parts[0];
      const params = parts[1] ? parts[1].split(',') : [];
      return { name, params, message: null };
    }

    if (typeof rule === 'object') {
      return {
        name: rule.rule,
        params: rule.params || [],
        message: rule.message
      };
    }

    return { name: rule, params: [], message: null };
  }

  /**
   * Format error message
   */
  formatMessage(ruleName, params) {
    const template = this.messages.get(ruleName) || 'Validation failed';
    return template.replace(/\{(\d+)\}/g, (match, index) => {
      return params[index] || match;
    });
  }

  /**
   * Validate form element
   */
  async validateElement(element) {
    const rules = this.getElementRules(element);
    const value = this.getElementValue(element);
    const errors = await this.validateValue(value, rules);

    this.displayElementErrors(element, errors);
    return errors.length === 0;
  }

  /**
   * Get validation rules from element
   */
  getElementRules(element) {
    const rules = [];

    // Required
    if (element.required) {
      rules.push('required');
    }

    // Type-based rules
    if (element.type === 'email') {
      rules.push('email');
    }

    if (element.type === 'url') {
      rules.push('url');
    }

    if (element.type === 'tel') {
      rules.push('phone');
    }

    if (element.type === 'number') {
      rules.push('numeric');
      if (element.min) rules.push(`min:${element.min}`);
      if (element.max) rules.push(`max:${element.max}`);
    }

    if (element.type === 'date') {
      rules.push('date');
    }

    // Length constraints
    if (element.minLength) {
      rules.push(`minLength:${element.minLength}`);
    }

    if (element.maxLength) {
      rules.push(`maxLength:${element.maxLength}`);
    }

    // Pattern
    if (element.pattern) {
      rules.push(`pattern:${element.pattern}`);
    }

    // Custom rules from data attributes
    const customRules = element.dataset.validate;
    if (customRules) {
      rules.push(...customRules.split('|'));
    }

    return rules;
  }

  /**
   * Get element value
   */
  getElementValue(element) {
    if (element.type === 'checkbox') {
      return element.checked;
    }

    if (element.type === 'radio') {
      const form = element.closest('form');
      const checked = form.querySelector(`input[name="${element.name}"]:checked`);
      return checked ? checked.value : null;
    }

    if (element.type === 'file') {
      return element.files[0] || null;
    }

    if (element.tagName === 'SELECT' && element.multiple) {
      return Array.from(element.selectedOptions).map(option => option.value);
    }

    return element.value;
  }

  /**
   * Display element errors
   */
  displayElementErrors(element, errors) {
    // Remove existing error state
    element.classList.remove('is-invalid');
    const existingFeedback = element.parentNode.querySelector('.invalid-feedback');
    if (existingFeedback) {
      existingFeedback.remove();
    }

    // Add error state if there are errors
    if (errors.length > 0) {
      element.classList.add('is-invalid');
      
      const feedback = document.createElement('div');
      feedback.className = 'invalid-feedback';
      feedback.textContent = errors[0]; // Show first error
      
      element.parentNode.appendChild(feedback);
    }
  }

  /**
   * Validate entire form
   */
  async validateFormElement(form) {
    const elements = form.querySelectorAll('input, select, textarea');
    const promises = Array.from(elements).map(element => this.validateElement(element));
    const results = await Promise.all(promises);
    
    return results.every(result => result);
  }

  /**
   * Setup real-time validation
   */
  setupRealTimeValidation(form) {
    const elements = form.querySelectorAll('input, select, textarea');
    
    elements.forEach(element => {
      // Validate on blur
      element.addEventListener('blur', () => {
        this.validateElement(element);
      });

      // Clear errors on input
      element.addEventListener('input', () => {
        if (element.classList.contains('is-invalid')) {
          element.classList.remove('is-invalid');
          const feedback = element.parentNode.querySelector('.invalid-feedback');
          if (feedback) {
            feedback.remove();
          }
        }
      });
    });

    // Validate on form submit
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const isValid = await this.validateFormElement(form);
      
      if (isValid) {
        // Form is valid, allow submission
        form.dispatchEvent(new CustomEvent('validSubmit', {
          detail: { form, data: new FormData(form) }
        }));
      } else {
        // Focus first invalid field
        const firstInvalid = form.querySelector('.is-invalid');
        if (firstInvalid) {
          firstInvalid.focus();
        }
      }
    });
  }

  /**
   * Create validation schema
   */
  createSchema(fields) {
    return fields;
  }

  /**
   * Common validation schemas
   */
  static get schemas() {
    return {
      login: {
        identifier: ['required', 'minLength:3'],
        password: ['required', 'minLength:6']
      },
      
      student: {
        firstName: ['required', 'minLength:2', 'maxLength:50'],
        lastName: ['required', 'minLength:2', 'maxLength:50'],
        email: ['required', 'email'],
        phone: ['phone'],
        studentId: ['required', 'pattern:^STU\\d{4}$']
      },
      
      user: {
        username: ['required', 'minLength:3', 'maxLength:20', 'pattern:^[a-zA-Z0-9_]+$'],
        email: ['required', 'email'],
        firstName: ['required', 'minLength:2', 'maxLength:50'],
        lastName: ['required', 'minLength:2', 'maxLength:50'],
        password: ['required', 'minLength:8'],
        confirmPassword: ['required']
      },
      
      changePassword: {
        currentPassword: ['required'],
        newPassword: ['required', 'minLength:8'],
        confirmPassword: ['required']
      }
    };
  }
}

// Utility functions
export const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

export const validatePhone = (phone) => {
  const phoneRegex = /^[\+]?[1-9][\d]{0,15}$/;
  return phoneRegex.test(phone.replace(/[\s\-\(\)]/g, ''));
};

export const validatePassword = (password) => {
  return password.length >= 8 &&
         /[a-z]/.test(password) &&
         /[A-Z]/.test(password) &&
         /\d/.test(password);
};

export const validateStudentId = (studentId) => {
  return /^STU\d{4}$/.test(studentId);
};

export const sanitizeInput = (input) => {
  if (typeof input !== 'string') return input;
  
  return input
    .replace(/[<>]/g, '') // Remove potential HTML tags
    .trim(); // Remove leading/trailing whitespace
};

export const escapeHtml = (text) => {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
};

// Create singleton instance
export const validator = new Validator();