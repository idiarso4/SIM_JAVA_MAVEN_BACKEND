/**
 * Development Utilities
 * Helper functions for development and debugging
 */

/**
 * Development logger with different levels
 */
export class DevLogger {
  constructor() {
    this.isDevelopment = (typeof process !== 'undefined' && process.env && process.env.NODE_ENV === 'development') || true;
    this.logLevel = (typeof process !== 'undefined' && process.env && process.env.LOG_LEVEL) || 'info';
    this.levels = {
      debug: 0,
      info: 1,
      warn: 2,
      error: 3
    };
  }

  /**
   * Check if logging is enabled for level
   */
  shouldLog(level) {
    if (!this.isDevelopment && level !== 'error') {
      return false;
    }
    return this.levels[level] >= this.levels[this.logLevel];
  }

  /**
   * Debug logging
   */
  debug(...args) {
    if (this.shouldLog('debug')) {
      console.log('%c[DEBUG]', 'color: #888; font-weight: bold;', ...args);
    }
  }

  /**
   * Info logging
   */
  info(...args) {
    if (this.shouldLog('info')) {
      console.log('%c[INFO]', 'color: #007bff; font-weight: bold;', ...args);
    }
  }

  /**
   * Warning logging
   */
  warn(...args) {
    if (this.shouldLog('warn')) {
      console.warn('%c[WARN]', 'color: #ffc107; font-weight: bold;', ...args);
    }
  }

  /**
   * Error logging
   */
  error(...args) {
    if (this.shouldLog('error')) {
      console.error('%c[ERROR]', 'color: #dc3545; font-weight: bold;', ...args);
    }
  }

  /**
   * Group logging
   */
  group(label, collapsed = false) {
    if (this.isDevelopment) {
      if (collapsed) {
        console.groupCollapsed(label);
      } else {
        console.group(label);
      }
    }
  }

  /**
   * End group logging
   */
  groupEnd() {
    if (this.isDevelopment) {
      console.groupEnd();
    }
  }

  /**
   * Table logging
   */
  table(data) {
    if (this.isDevelopment && this.shouldLog('debug')) {
      console.table(data);
    }
  }

  /**
   * Time logging
   */
  time(label) {
    if (this.isDevelopment && this.shouldLog('debug')) {
      console.time(label);
    }
  }

  /**
   * End time logging
   */
  timeEnd(label) {
    if (this.isDevelopment && this.shouldLog('debug')) {
      console.timeEnd(label);
    }
  }
}

/**
 * Performance monitoring utilities
 */
export class PerformanceMonitor {
  constructor() {
    this.isEnabled = (typeof process !== 'undefined' && process.env && process.env.ENABLE_PERFORMANCE_MONITORING === 'true') || false;
    this.marks = new Map();
    this.measures = new Map();
  }

  /**
   * Mark a performance point
   */
  mark(name) {
    if (this.isEnabled && performance.mark) {
      performance.mark(name);
      this.marks.set(name, performance.now());
    }
  }

  /**
   * Measure performance between two marks
   */
  measure(name, startMark, endMark) {
    if (this.isEnabled && performance.measure) {
      try {
        performance.measure(name, startMark, endMark);
        const measure = performance.getEntriesByName(name, 'measure')[0];
        this.measures.set(name, measure.duration);
        return measure.duration;
      } catch (error) {
        console.warn('Performance measurement failed:', error);
        return null;
      }
    }
    return null;
  }

  /**
   * Get all performance measures
   */
  getMeasures() {
    return Object.fromEntries(this.measures);
  }

  /**
   * Clear performance data
   */
  clear() {
    if (this.isEnabled) {
      performance.clearMarks();
      performance.clearMeasures();
      this.marks.clear();
      this.measures.clear();
    }
  }

  /**
   * Log performance summary
   */
  logSummary() {
    if (this.isEnabled && this.measures.size > 0) {
      console.group('Performance Summary');
      console.table(this.getMeasures());
      console.groupEnd();
    }
  }
}



/**
 * Development tools initialization
 */
export function initDevTools() {
  if ((typeof process !== 'undefined' && process.env && process.env.NODE_ENV === 'development') || true) {
    // Add development tools to window for debugging
    window.devTools = {
      logger: new DevLogger(),
      performance: new PerformanceMonitor()
    };

    // Add helpful debugging functions
    window.debugApp = () => {
      console.group('Application Debug Info');
      console.log('App State:', window.SIMApp ? window.SIMApp.getCurrentUser() : 'Not initialized');
      console.log('Current Route:', window.SIMApp ? window.SIMApp.router.getCurrentRoute() : 'No router');
      console.log('Environment:', {
        NODE_ENV: (typeof process !== 'undefined' && process.env && process.env.NODE_ENV) || 'development',
        API_BASE_URL: (typeof process !== 'undefined' && process.env && process.env.API_BASE_URL) || 'http://localhost:8080/api/v1',
        APP_VERSION: (typeof process !== 'undefined' && process.env && process.env.APP_VERSION) || '1.0.0'
      });
      console.groupEnd();
    };

    console.log('%c🚀 Development tools loaded!', 'color: #28a745; font-size: 14px; font-weight: bold;');
    console.log('Available tools: window.devTools, window.debugApp()');
  }
}

// Create singleton instances
export const logger = new DevLogger();
export const performance = new PerformanceMonitor();