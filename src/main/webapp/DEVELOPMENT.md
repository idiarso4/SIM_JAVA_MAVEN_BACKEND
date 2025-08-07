# Development Guide

This guide covers the development workflow, tools, and best practices for the SIM Frontend project.

## Quick Start

1. **Install dependencies:**
   ```bash
   npm install
   ```

2. **Start development server:**
   ```bash
   npm run dev:serve
   ```

3. **Run tests:**
   ```bash
   npm test
   ```

## Development Workflow

### 1. Environment Setup

The project supports multiple environments:
- **Development**: `.env.development`
- **Production**: `.env.production`
- **Test**: `.env.test`

Environment variables are automatically loaded based on `NODE_ENV`.

### 2. Development Server

Start the development server with hot reloading:
```bash
npm run dev:serve
```

The server will start at `http://localhost:3000` and proxy API requests to `http://localhost:8080`.

### 3. Building

**Development build:**
```bash
npm run build
```

**Production build:**
```bash
npm run build:prod
```

**Analyze bundle size:**
```bash
npm run build:analyze
```

### 4. Testing

**Run all tests:**
```bash
npm test
```

**Watch mode:**
```bash
npm run test:watch
```

**Coverage report:**
```bash
npm run test:coverage
```

**CI mode:**
```bash
npm run test:ci
```

### 5. Code Quality

**Lint code:**
```bash
npm run lint
```

**Fix linting issues:**
```bash
npm run lint:fix
```

**Format code:**
```bash
npm run format
```

**Check formatting:**
```bash
npm run format:check
```

**Validate everything:**
```bash
npm run validate
```

## Development Tools

### 1. Browser DevTools

In development mode, additional debugging tools are available:

```javascript
// Access development tools
window.devTools.logger.debug('Debug message');
window.devTools.performance.mark('start-operation');
window.devTools.mockData.generateStudents(10);

// Debug application state
window.debugApp();
```

### 2. Performance Monitoring

Performance monitoring is enabled in development:

```javascript
// Mark performance points
performance.mark('component-render-start');
// ... component rendering
performance.mark('component-render-end');
performance.measure('component-render', 'component-render-start', 'component-render-end');
```

### 3. Mock Data

Mock data generation is available in development:

```javascript
const mockStudents = window.devTools.mockData.generateStudents(50);
const mockUsers = window.devTools.mockData.generateUsers(10);
const mockGrades = window.devTools.mockData.generateGrades(50, 5);
```

## Build Tools Configuration

### Webpack

The Webpack configuration supports:
- **Hot Module Replacement** for development
- **Code splitting** for optimized loading
- **Bundle analysis** for size optimization
- **Source maps** for debugging
- **Asset optimization** for production

Key features:
- Automatic vendor chunk splitting
- CSS extraction in production
- Image and font optimization
- Development proxy for API calls

### Babel

Babel configuration includes:
- **ES6+ transpilation** for browser compatibility
- **Runtime transformation** for polyfills
- **Module transformation** for bundling

### PostCSS

PostCSS processes CSS with:
- **Autoprefixer** for vendor prefixes
- **Modern CSS features** polyfills
- **CSS optimization** in production

### ESLint

ESLint configuration enforces:
- **Standard JavaScript style**
- **Import/export best practices**
- **Error prevention rules**
- **Code quality standards**

### Prettier

Prettier ensures consistent formatting:
- **Automatic code formatting**
- **Consistent style across team**
- **Integration with editors**

## Testing Strategy

### Unit Tests

Located in `test/` directory and `*.test.js` files:
- **Service layer testing** with mocked dependencies
- **Utility function testing** with edge cases
- **Component logic testing** with DOM manipulation

### Integration Tests

Test component interactions:
- **API integration** with mocked responses
- **User workflow testing** with simulated events
- **State management** across components

### Test Utilities

Available test utilities:
- **Mock data generators** for consistent test data
- **DOM helpers** for element interaction
- **API mocks** for service testing
- **Performance helpers** for timing tests

## Code Organization

### Directory Structure

```
js/
├── components/     # UI components
├── services/       # Business logic and API calls
├── utils/          # Utility functions and helpers
└── main.js         # Application entry point

test/
├── setup.js        # Test environment setup
├── globalSetup.js  # Global test configuration
└── **/*.test.js    # Test files
```

### Naming Conventions

- **Files**: kebab-case (`student-service.js`)
- **Classes**: PascalCase (`StudentService`)
- **Functions**: camelCase (`getStudentById`)
- **Constants**: UPPER_SNAKE_CASE (`API_BASE_URL`)

### Import/Export Patterns

```javascript
// Named exports for utilities
export { validateEmail, formatDate };

// Default exports for classes
export default class StudentService { }

// Import patterns
import StudentService from './services/student-service.js';
import { validateEmail, formatDate } from './utils/validation.js';
```

## Performance Best Practices

### 1. Code Splitting

Components are loaded dynamically:
```javascript
const component = () => import('./components/student-list.js');
```

### 2. Bundle Optimization

- **Tree shaking** removes unused code
- **Minification** reduces file sizes
- **Compression** enables gzip/brotli
- **Caching** with content hashes

### 3. Asset Optimization

- **Image optimization** with WebP support
- **Font loading** with display swap
- **CSS critical path** optimization
- **JavaScript chunking** for parallel loading

## Debugging

### Development Mode

Enable debug logging:
```javascript
localStorage.setItem('debug', 'sim:*');
```

### Network Debugging

API calls are logged in development:
```javascript
// Enable API debugging
window.devTools.logger.debug('API Request:', request);
```

### Performance Debugging

Monitor performance metrics:
```javascript
// Performance monitoring
window.devTools.performance.logSummary();
```

## Common Issues

### 1. Build Failures

**Symptom**: Webpack build fails
**Solution**: 
- Check for syntax errors with `npm run lint`
- Verify all imports are correct
- Clear cache with `npm run clean`

### 2. Hot Reload Not Working

**Symptom**: Changes don't reflect in browser
**Solution**:
- Restart development server
- Check browser console for errors
- Verify file watching permissions

### 3. Test Failures

**Symptom**: Tests fail unexpectedly
**Solution**:
- Run tests in isolation: `npm test -- --testNamePattern="test name"`
- Check mock setup in `test/setup.js`
- Verify test environment variables

### 4. API Connection Issues

**Symptom**: API calls fail in development
**Solution**:
- Verify backend server is running on port 8080
- Check proxy configuration in `webpack.config.js`
- Verify CORS settings on backend

## Contributing

### Before Committing

1. **Run validation:**
   ```bash
   npm run validate
   ```

2. **Check build:**
   ```bash
   npm run build:prod
   ```

3. **Run tests:**
   ```bash
   npm run test:ci
   ```

### Code Review Checklist

- [ ] Code follows style guidelines
- [ ] Tests are included and passing
- [ ] Documentation is updated
- [ ] Performance impact is considered
- [ ] Security implications are reviewed
- [ ] Accessibility standards are met

## Resources

- [Webpack Documentation](https://webpack.js.org/)
- [Babel Documentation](https://babeljs.io/)
- [Jest Testing Framework](https://jestjs.io/)
- [ESLint Rules](https://eslint.org/docs/rules/)
- [Prettier Configuration](https://prettier.io/docs/en/configuration.html)