# SIM Login Testing Guide

## 🚀 Quick Start

### 1. Start Backend Server
```bash
# Option 1: Use restart script
restart-backend.bat

# Option 2: Manual start with Maven
mvn spring-boot:run

# Option 3: Manual start with JAR
java -jar target/sim-backend-1.0.0.jar
```

### 2. Start Frontend Server
```bash
# Navigate to webapp directory
cd src/main/webapp

# Start Python server
python serve-test.py
```

### 3. Test Login
Open browser and go to: http://localhost:3001/login.html

## 🔐 Test Credentials

### Admin User
- **Username**: `admin`
- **Email**: `admin@sim.edu`
- **Password**: `admin123`
- **Roles**: ADMIN
- **Permissions**: All permissions

### Teacher User
- **Username**: `teacher`
- **Email**: `teacher@sim.edu`
- **Password**: `teacher123`
- **Roles**: TEACHER
- **Permissions**: Limited (view students, manage grades, etc.)

## 🔍 Troubleshooting

### Backend Issues

**1. Database Tables Not Found**
- **Problem**: `Table "USERS" not found` error
- **Solution**: Backend will now auto-create tables and data using `data.sql`
- **Check**: Visit http://localhost:8080/h2-console to verify tables exist

**2. Backend Not Starting**
- **Check Java**: Ensure Java 17+ is installed
- **Check Port**: Ensure port 8080 is not in use
- **Check Logs**: Look at backend console for error messages

**3. Authentication Fails**
- **Check Database**: Verify users exist in H2 console
- **Check Passwords**: Passwords are BCrypt encoded
- **Check Logs**: Look for authentication errors in backend logs

### Frontend Issues

**1. Cannot Connect to Backend**
- **Check Backend**: Ensure backend is running on port 8080
- **Check CORS**: Backend should allow requests from frontend port
- **Check Network**: Try accessing http://localhost:8080 directly

**2. Login Form Not Working**
- **Check Console**: Open browser dev tools for JavaScript errors
- **Check Network**: Look at network tab for failed API calls
- **Check Auth Service**: Verify auth service is loaded correctly

## 🛠️ Development Tools

### H2 Database Console
- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:simdb`
- **Username**: `sa`
- **Password**: `password`

### API Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

### Health Check
- **Backend Health**: http://localhost:8080/actuator/health
- **Frontend**: http://localhost:3001

## 📊 Database Schema

### Users Table
```sql
SELECT * FROM users;
-- Should show admin and teacher users
```

### Roles Table
```sql
SELECT * FROM roles;
-- Should show ADMIN, TEACHER, STUDENT roles
```

### User Roles
```sql
SELECT u.username, r.name as role 
FROM users u 
JOIN user_roles ur ON u.id = ur.user_id 
JOIN roles r ON ur.role_id = r.id;
```

## 🧪 Testing Scenarios

### 1. Valid Login Test
1. Go to login page
2. Enter: `admin` / `admin123`
3. Click "Sign In"
4. Should redirect to dashboard

### 2. Invalid Login Test
1. Enter wrong credentials
2. Should show error message
3. Should not redirect

### 3. Account Lockout Test
1. Try wrong password 5 times
2. Account should be locked for 15 minutes
3. Should show lockout message

### 4. Role-Based Access Test
1. Login as teacher
2. Try to access admin-only features
3. Should show access denied

## 📝 API Testing

### Login API Test
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "admin",
    "password": "admin123",
    "rememberMe": false
  }'
```

Expected Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "refresh_token_here",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "name": "System Administrator",
    "email": "admin@sim.edu",
    "userType": "ADMIN",
    "roles": ["ADMIN"],
    "permissions": ["..."]
  }
}
```

## 🔧 Configuration

### Backend Configuration
- **Port**: 8080
- **Database**: H2 in-memory
- **JWT Secret**: Configured in application.yml
- **CORS**: Allows frontend origins

### Frontend Configuration
- **Port**: 3001
- **API Base URL**: http://localhost:8080/api/v1
- **Token Storage**: localStorage
- **Auto-refresh**: Enabled

## 📞 Support

If you encounter issues:

1. **Check Logs**: Both backend and frontend console logs
2. **Verify Ports**: Ensure both servers are running on correct ports
3. **Database**: Check H2 console for data integrity
4. **Network**: Verify API calls in browser dev tools

---

**Last Updated**: January 2025  
**Version**: 1.0.0