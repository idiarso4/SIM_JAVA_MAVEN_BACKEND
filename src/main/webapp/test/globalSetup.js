/**
 * Jest global setup
 * Runs once before all tests
 */

module.exports = async () => {
  console.log('Setting up test environment...');
  
  // Set test environment variables
  process.env.NODE_ENV = 'test';
  process.env.API_BASE_URL = 'http://localhost:8080/api/v1';
  process.env.APP_VERSION = '1.0.0-test';
  
  // Suppress console warnings in tests
  const originalWarn = console.warn;
  console.warn = (...args) => {
    // Suppress specific warnings that are expected in test environment
    const message = args[0];
    if (
      typeof message === 'string' &&
      (message.includes('Warning: ReactDOM.render is deprecated') ||
       message.includes('Warning: componentWillMount has been renamed'))
    ) {
      return;
    }
    originalWarn.apply(console, args);
  };
  
  console.log('Test environment setup complete');
};