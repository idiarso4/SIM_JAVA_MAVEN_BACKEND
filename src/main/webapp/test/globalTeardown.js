/**
 * Jest global teardown
 * Runs once after all tests
 */

module.exports = async () => {
  console.log('Cleaning up test environment...');
  
  // Clean up any global resources
  // This is where you would close database connections,
  // stop test servers, etc.
  
  console.log('Test environment cleanup complete');
};