/**
 * Development Utilities
 * Helper functions for development and debugging
 */

/**
 * Development logger with different levels
 */
export class DevLogger {
  constructor() {
    this.isDevelopment = process.env.NODE_ENV === 'development';
    this.logLevel = process.env.LOG_LEVEL || 'info';
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
    this.isEnabled = process.env.ENABLE_PERFORMANCE_MONITORING === 'true';
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
 * Mock data generator for development
 */
export class MockDataGenerator {
  constructor() {
    this.isEnabled = process.env.ENABLE_MOCK_DATA === 'true';
  }

  /**
   * Generate mock student data
   */
  generateStudents(count = 10) {
    if (!this.isEnabled) return [];

    const firstNames = ['John', 'Jane', 'Michael', 'Sarah', 'David', 'Emily', 'Robert', 'Lisa', 'James', 'Maria'];
    const lastNames = ['Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Rodriguez', 'Martinez'];
    const majors = ['Computer Science', 'Mathematics', 'Physics', 'Chemistry', 'Biology', 'English', 'History', 'Art'];

    return Array.from({ length: count }, (_, i) => ({
      id: i + 1,
      studentId: `STU${String(i + 1).padStart(4, '0')}`,
      firstName: firstNames[Math.floor(Math.random() * firstNames.length)],
      lastName: lastNames[Math.floor(Math.random() * lastNames.length)],
      email: `student${i + 1}@example.com`,
      phone: `+1-555-${String(Math.floor(Math.random() * 10000)).padStart(4, '0')}`,
      major: majors[Math.floor(Math.random() * majors.length)],
      enrollmentDate: new Date(2023, Math.floor(Math.random() * 12), Math.floor(Math.random() * 28) + 1).toISOString(),
      status: Math.random() > 0.1 ? 'ACTIVE' : 'INACTIVE'
    }));
  }

  /**
   * Generate mock user data
   */
  generateUsers(count = 5) {
    if (!this.isEnabled) return [];

    const roles = ['ADMIN', 'TEACHER', 'STAFF'];
    const firstNames = ['Admin', 'Teacher', 'Staff', 'Manager', 'Director'];
    const lastNames = ['User', 'Account', 'Profile', 'System', 'Test'];

    return Array.from({ length: count }, (_, i) => ({
      id: i + 1,
      username: `user${i + 1}`,
      email: `user${i + 1}@school.edu`,
      firstName: firstNames[i % firstNames.length],
      lastName: lastNames[i % lastNames.length],
      role: roles[Math.floor(Math.random() * roles.length)],
      active: Math.random() > 0.2,
      createdAt: new Date(2023, Math.floor(Math.random() * 12), Math.floor(Math.random() * 28) + 1).toISOString()
    }));
  }

  /**
   * Generate mock grade data
   */
  generateGrades(studentCount = 10, subjectCount = 5) {
    if (!this.isEnabled) return [];

    const subjects = ['Mathematics', 'Science', 'English', 'History', 'Art'];
    const assessmentTypes = ['QUIZ', 'EXAM', 'ASSIGNMENT', 'PROJECT'];

    const grades = [];
    for (let studentId = 1; studentId <= studentCount; studentId++) {
      for (let subjectId = 1; subjectId <= subjectCount; subjectId++) {
        const gradeCount = Math.floor(Math.random() * 5) + 3; // 3-7 grades per subject
        for (let i = 0; i < gradeCount; i++) {
          grades.push({
            id: grades.length + 1,
            studentId,
            subjectId,
            subject: subjects[subjectId - 1],
            assessmentType: assessmentTypes[Math.floor(Math.random() * assessmentTypes.length)],
            score: Math.floor(Math.random() * 40) + 60, // 60-100
            maxScore: 100,
            date: new Date(2023, Math.floor(Math.random() * 12), Math.floor(Math.random() * 28) + 1).toISOString()
          });
        }
      }
    }
    return grades;
  }
}

/**
 * Development tools initialization
 */
export function initDevTools() {
  if (process.env.NODE_ENV === 'development') {
    // Add development tools to window for debugging
    window.devTools = {
      logger: new DevLogger(),
      performance: new PerformanceMonitor(),
      mockData: new MockDataGenerator()
    };

    // Add helpful debugging functions
    window.debugApp = () => {
      console.group('Application Debug Info');
      console.log('App State:', window.SIMApp ? window.SIMApp.getCurrentUser() : 'Not initialized');
      console.log('Current Route:', window.SIMApp ? window.SIMApp.router.getCurrentRoute() : 'No router');
      console.log('Environment:', {
        NODE_ENV: process.env.NODE_ENV,
        API_BASE_URL: process.env.API_BASE_URL,
        APP_VERSION: process.env.APP_VERSION
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
export const mockData = new MockDataGenerator();