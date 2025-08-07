# School Information Management System (SIM) Backend

This is the backend API for the School Information Management System built with Spring Boot.

## 🚀 Quick Start

### Prerequisites

- Java 11 or higher
- Maven 3.6+
- MySQL 8.0+ (optional - can use H2 for development)

### Running the Application

#### Option 1: Using the provided scripts

**Windows:**
```bash
run.bat
```

**Linux/Mac:**
```bash
chmod +x run.sh
./run.sh
```

#### Option 2: Manual Maven commands

```bash
# Compile the project
mvn clean compile -DskipTests

# Run the application
mvn spring-boot:run -Dspring-boot.run.profiles=development
```

### Access the Application

Once running, you can access:

- **Home Page**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **Application Info**: http://localhost:8080/info

## 📋 Features

- ✅ User Authentication & Authorization (JWT)
- ✅ Student Management with Excel Import/Export
- ✅ Attendance Tracking & Reporting
- ✅ Assessment & Grading System
- ✅ Academic Reporting & Transcripts
- ✅ Schedule Management & Timetables
- ✅ Role-based Access Control
- ✅ Database Migration Support
- ✅ Comprehensive API Documentation

## 🛠 Technology Stack

- **Framework**: Spring Boot 2.7.18
- **Database**: MySQL 8.0 / H2 (development)
- **Cache**: Redis
- **Security**: Spring Security with JWT
- **Documentation**: OpenAPI 3 (Swagger)
- **Build Tool**: Maven
- **Java Version**: 11

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/school/sim/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── request/    # Request DTOs
│   │   │   └── response/   # Response DTOs
│   │   ├── entity/         # JPA entities
│   │   ├── exception/      # Custom exceptions
│   │   ├── repository/     # Data repositories
│   │   ├── security/       # Security components
│   │   └── service/        # Business logic services
│   │       └── impl/       # Service implementations
│   └── resources/
│       ├── application.yml # Application configuration
│       └── db/migration/   # Database migration scripts
└── test/                   # Test classes
```

## 🔧 Configuration

### Development Mode (Default)

The application runs in development mode by default with:
- H2 in-memory database (no setup required)
- Debug logging enabled
- Auto-reload on code changes

### Database Configuration

For MySQL (production):
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sim_db
    username: your_username
    password: your_password
```

### Environment Variables

- `DB_USERNAME`: Database username (default: root)
- `DB_PASSWORD`: Database password (default: password)
- `JWT_SECRET`: JWT secret key
- `REDIS_HOST`: Redis host (default: localhost)
- `REDIS_PORT`: Redis port (default: 6379)

## 📚 API Documentation

### Main Endpoints

#### Authentication
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/refresh` - Refresh token
- `POST /api/v1/auth/logout` - User logout

#### User Management
- `GET /api/v1/users` - Get all users
- `POST /api/v1/users` - Create user
- `GET /api/v1/users/{id}` - Get user by ID
- `PUT /api/v1/users/{id}` - Update user

#### Student Management
- `GET /api/v1/students` - Get all students
- `POST /api/v1/students` - Create student
- `GET /api/v1/students/search` - Search students
- `POST /api/v1/students/excel/import` - Import from Excel

#### Attendance
- `GET /api/v1/attendance` - Get attendance records
- `POST /api/v1/attendance` - Record attendance
- `GET /api/v1/attendance/reports` - Generate reports

#### Assessment & Grading
- `GET /api/v1/assessments` - Get assessments
- `POST /api/v1/assessments` - Create assessment
- `POST /api/v1/assessments/{id}/grade` - Grade assessment

#### Academic Reports
- `POST /api/v1/academic/reports` - Generate academic reports
- `GET /api/v1/academic/reports/transcript/student/{id}` - Student transcript
- `GET /api/v1/academic/reports/gpa/student/{id}` - Calculate GPA

#### Schedule Management
- `GET /api/v1/schedules` - Get schedules
- `POST /api/v1/schedules` - Create schedule
- `GET /api/v1/schedules/timetable/class/{id}` - Class timetable
- `GET /api/v1/schedules/conflicts` - Check conflicts

## 🧪 Testing

Run tests with:
```bash
mvn test
```

## 📝 Development Notes

### Implemented Features

1. **Authentication System** - JWT-based with role management
2. **User Management** - Complete CRUD with role assignment
3. **Student Management** - Lifecycle management with Excel integration
4. **Attendance System** - Recording, tracking, and comprehensive reporting
5. **Assessment System** - Grading, evaluation, and rubric management
6. **Academic Reporting** - GPA calculation, transcripts, and analytics
7. **Schedule Management** - Timetable creation with conflict detection

### Database Schema

The application uses JPA entities with proper relationships:
- Users, Roles, and Permissions for security
- Students, Classes, Majors, and Departments for academic structure
- Assessments and StudentAssessments for grading
- Attendance records with status tracking
- Schedules for timetable management

### Security

- JWT-based authentication
- Role-based access control
- Method-level security annotations
- CORS configuration for frontend integration

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License.