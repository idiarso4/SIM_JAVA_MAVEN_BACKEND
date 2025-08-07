/**
 * State Management Utility
 * Simple state management for application data
 */

export class StateManager {
  constructor() {
    this.state = new Map();
    this.listeners = new Map();
    this.middleware = [];
  }

  /**
   * Get state value
   */
  get(key) {
    return this.state.get(key);
  }

  /**
   * Set state value
   */
  set(key, value) {
    const oldValue = this.state.get(key);
    
    // Apply middleware
    let newValue = value;
    for (const middleware of this.middleware) {
      newValue = middleware(key, newValue, oldValue);
    }
    
    this.state.set(key, newValue);
    
    // Notify listeners
    this.notifyListeners(key, newValue, oldValue);
    
    return newValue;
  }

  /**
   * Update state value
   */
  update(key, updater) {
    const currentValue = this.get(key);
    const newValue = typeof updater === 'function' ? updater(currentValue) : updater;
    return this.set(key, newValue);
  }

  /**
   * Delete state value
   */
  delete(key) {
    const oldValue = this.state.get(key);
    const deleted = this.state.delete(key);
    
    if (deleted) {
      this.notifyListeners(key, undefined, oldValue);
    }
    
    return deleted;
  }

  /**
   * Check if state has key
   */
  has(key) {
    return this.state.has(key);
  }

  /**
   * Clear all state
   */
  clear() {
    const keys = Array.from(this.state.keys());
    this.state.clear();
    
    // Notify all listeners
    keys.forEach(key => {
      this.notifyListeners(key, undefined, this.state.get(key));
    });
  }

  /**
   * Subscribe to state changes
   */
  subscribe(key, listener) {
    if (!this.listeners.has(key)) {
      this.listeners.set(key, new Set());
    }
    
    this.listeners.get(key).add(listener);
    
    // Return unsubscribe function
    return () => {
      const keyListeners = this.listeners.get(key);
      if (keyListeners) {
        keyListeners.delete(listener);
        if (keyListeners.size === 0) {
          this.listeners.delete(key);
        }
      }
    };
  }

  /**
   * Subscribe to multiple keys
   */
  subscribeMultiple(keys, listener) {
    const unsubscribers = keys.map(key => this.subscribe(key, listener));
    
    return () => {
      unsubscribers.forEach(unsubscribe => unsubscribe());
    };
  }

  /**
   * Notify listeners of state changes
   */
  notifyListeners(key, newValue, oldValue) {
    const keyListeners = this.listeners.get(key);
    if (keyListeners) {
      keyListeners.forEach(listener => {
        try {
          listener(newValue, oldValue, key);
        } catch (error) {
          console.error('Error in state listener:', error);
        }
      });
    }
  }

  /**
   * Add middleware
   */
  addMiddleware(middleware) {
    this.middleware.push(middleware);
  }

  /**
   * Remove middleware
   */
  removeMiddleware(middleware) {
    const index = this.middleware.indexOf(middleware);
    if (index > -1) {
      this.middleware.splice(index, 1);
    }
  }

  /**
   * Get all state as object
   */
  getAll() {
    return Object.fromEntries(this.state);
  }

  /**
   * Set multiple state values
   */
  setMultiple(values) {
    Object.entries(values).forEach(([key, value]) => {
      this.set(key, value);
    });
  }

  /**
   * Persist state to localStorage
   */
  persist(key, storageKey = null) {
    const actualStorageKey = storageKey || `sim-state-${key}`;
    
    // Save current value
    const value = this.get(key);
    if (value !== undefined) {
      try {
        localStorage.setItem(actualStorageKey, JSON.stringify(value));
      } catch (error) {
        console.warn('Failed to persist state:', error);
      }
    }
    
    // Subscribe to future changes
    return this.subscribe(key, (newValue) => {
      try {
        if (newValue !== undefined) {
          localStorage.setItem(actualStorageKey, JSON.stringify(newValue));
        } else {
          localStorage.removeItem(actualStorageKey);
        }
      } catch (error) {
        console.warn('Failed to persist state:', error);
      }
    });
  }

  /**
   * Restore state from localStorage
   */
  restore(key, storageKey = null) {
    const actualStorageKey = storageKey || `sim-state-${key}`;
    
    try {
      const stored = localStorage.getItem(actualStorageKey);
      if (stored !== null) {
        const value = JSON.parse(stored);
        this.set(key, value);
        return value;
      }
    } catch (error) {
      console.warn('Failed to restore state:', error);
    }
    
    return undefined;
  }

  /**
   * Create computed state
   */
  computed(key, dependencies, computer) {
    const compute = () => {
      const values = dependencies.map(dep => this.get(dep));
      return computer(...values);
    };
    
    // Set initial value
    this.set(key, compute());
    
    // Subscribe to dependencies
    const unsubscribers = dependencies.map(dep => 
      this.subscribe(dep, () => {
        this.set(key, compute());
      })
    );
    
    // Return cleanup function
    return () => {
      unsubscribers.forEach(unsubscribe => unsubscribe());
      this.delete(key);
    };
  }

  /**
   * Create action
   */
  createAction(name, handler) {
    return (...args) => {
      try {
        const result = handler(this, ...args);
        
        // Handle async actions
        if (result && typeof result.then === 'function') {
          return result.catch(error => {
            console.error(`Error in action ${name}:`, error);
            throw error;
          });
        }
        
        return result;
      } catch (error) {
        console.error(`Error in action ${name}:`, error);
        throw error;
      }
    };
  }

  /**
   * Batch state updates
   */
  batch(updater) {
    const originalNotify = this.notifyListeners;
    const batchedNotifications = [];
    
    // Temporarily disable notifications
    this.notifyListeners = (key, newValue, oldValue) => {
      batchedNotifications.push({ key, newValue, oldValue });
    };
    
    try {
      updater(this);
    } finally {
      // Restore original notify function
      this.notifyListeners = originalNotify;
      
      // Send batched notifications
      batchedNotifications.forEach(({ key, newValue, oldValue }) => {
        this.notifyListeners(key, newValue, oldValue);
      });
    }
  }

  /**
   * Debug state
   */
  debug() {
    console.group('State Manager Debug');
    console.log('Current State:', this.getAll());
    console.log('Listeners:', Object.fromEntries(
      Array.from(this.listeners.entries()).map(([key, listeners]) => [key, listeners.size])
    ));
    console.log('Middleware:', this.middleware.length);
    console.groupEnd();
  }
}

// Middleware functions
export const loggerMiddleware = (key, newValue, oldValue) => {
  console.log(`State change: ${key}`, { oldValue, newValue });
  return newValue;
};

export const validationMiddleware = (validators) => (key, newValue, oldValue) => {
  const validator = validators[key];
  if (validator && !validator(newValue)) {
    console.warn(`Validation failed for ${key}:`, newValue);
    return oldValue; // Keep old value if validation fails
  }
  return newValue;
};

export const immutableMiddleware = (key, newValue, oldValue) => {
  // Deep clone objects to prevent mutations
  if (typeof newValue === 'object' && newValue !== null) {
    return JSON.parse(JSON.stringify(newValue));
  }
  return newValue;
};

// Create singleton instance
export const stateManager = new StateManager();

// Add default middleware in development
if (process.env.NODE_ENV === 'development') {
  stateManager.addMiddleware(loggerMiddleware);
  stateManager.addMiddleware(immutableMiddleware);
}