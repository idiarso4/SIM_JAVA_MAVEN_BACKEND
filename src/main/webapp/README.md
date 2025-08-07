# SIM Frontend

Maven-based frontend web application for the School Information Management System.

## Overview

This is a modern, responsive web application built with:
- HTML5, CSS3, and JavaScript (ES6+)
- Bootstrap 5 for responsive design
- Chart.js for data visualization
- Webpack for module bundling
- Maven for build management

## Project Structure

```
src/main/webapp/
├── index.html              # Main HTML template
├── css/
│   └── main.css            # Main stylesheet
├── js/
│   ├── main.js             # Application entry point
│   ├── services/           # Service layer
│   │   ├── auth.js         # Authentication service
│   │   ├── api.js          # API service
│   │   ├── notification.js # Notification service
│   │   └── loading.js      # Loading service
│   ├── components/         # UI components
│   │   ├── dashboard.js    # Dashboard component
│   │   ├── students.js     # Students component
│   │   ├── users.js        # Users component
│   │   ├── grades.js       # Grades component
│   │   ├── reports.js      # Reports component
│   │   ├── profile.js      # Profile component
│   │   └── settings.js     # Settings component
│   └── utils/              # Utility functions
│       └── router.js       # Client-side router
├── assets/                 # Static assets
├── package.json            # NPM dependencies
├── webpack.config.js       # Webpack configuration
├── babel.config.js         # Babel configuration
├── postcss.config.js       # PostCSS configuration
├── .eslintrc.js           # ESLint configuration
└── .prettierrc            # Prettier configuration
```

## Build Commands

### Maven Commands

```bash
# Install dependencies and build
mvn clean compile

# Run tests
mvn test

# Package for production
mvn clean package

# Development build with watch
mvn compile -Pdevelopment

# Production build
mvn compile -Pproduction
```

### NPM Commands (when working directly in webapp directory)

```bash
# Install dependencies
npm install

# Development build with watch
npm run dev

# Production build
npm run build:prod

# Run tests
npm test

# Run linting
npm run lint

# Format code
npm run format

# Start development server
npm run serve
```

## Features

### Implemented
- ✅ Responsive HTML5 layout with Bootstrap 5
- ✅ Authentication system with JWT support
- ✅ Client-side routing (SPA)
- ✅ Dashboard with statistics and charts
- ✅ Notification system with toast messages
- ✅ Loading states and spinners
- ✅ Error handling and validation
- ✅ Maven build integration
- ✅ Webpack bundling and optimization
- ✅ ESLint and Prettier configuration
- ✅ Basic test setup with Jest

### To Be Implemented
- ⏳ Student management interface
- ⏳ User management interface
- ⏳ Grade and assessment management
- ⏳ Reporting and analytics
- ⏳ Excel import/export functionality
- ⏳ Profile and settings management
- ⏳ Advanced search and filtering
- ⏳ Mobile optimization
- ⏳ Comprehensive testing
- ⏳ Performance optimization

## API Integration

The frontend integrates with the Spring Boot backend API at `http://localhost:8080/api/v1`.

### Authentication Endpoints
- `POST /auth/login` - User login
- `POST /auth/logout` - User logout
- `POST /auth/refresh` - Token refresh
- `GET /auth/me` - Get current user

### Student Management Endpoints
- `GET /students` - List students
- `POST /students` - Create student
- `PUT /students/{id}` - Update student
- `DELETE /students/{id}` - Delete student

### User Management Endpoints
- `GET /users` - List users
- `POST /users` - Create user
- `PUT /users/{id}` - Update user
- `DELETE /users/{id}` - Delete user

## Development

### Prerequisites
- Node.js 18+ and NPM 9+
- Java 17+
- Maven 3.8+

### Getting Started

1. Navigate to the webapp directory:
   ```bash
   cd src/main/webapp
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start development server:
   ```bash
   npm run serve
   ```

4. Or use Maven for full build:
   ```bash
   mvn clean compile
   ```

### Code Style

The project uses ESLint and Prettier for code formatting:
- Run `npm run lint` to check for issues
- Run `npm run format` to format code
- Configure your editor to format on save

### Testing

Tests are written using Jest and Testing Library:
- Run `npm test` to execute tests
- Run `npm run test:coverage` for coverage report
- Tests are located in `src/test/javascript/`

## Deployment

### Development
- Built assets are served from `dist/` directory
- Hot reloading enabled for rapid development
- Source maps included for debugging

### Production
- Assets are minified and optimized
- Code splitting for better performance
- Gzip compression enabled
- Source maps available for debugging

## Browser Support

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Contributing

1. Follow the established code style
2. Write tests for new features
3. Update documentation as needed
4. Use meaningful commit messages

## License

This project is part of the School Information Management System.