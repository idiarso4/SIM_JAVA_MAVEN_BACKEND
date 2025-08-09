# SIM Login Quick Fix Guide

## 🚨 Current Issue
Login webapp menampilkan error "Invalid email/username or password" karena backend database tidak memiliki data user yang benar.

## ✅ Quick Solutions

### Option 1: Test dengan Mock Backend (Recommended)
```bash
# 1. Jalankan test script
test-login.bat

# 2. Buka browser ke:
http://localhost:3001/test-login-mock.html

# 3. Login dengan:
Username: admin
Password: admin123
```

### Option 2: Fix Backend Database
```bash
# 1. Pastikan backend berjalan
mvn spring-boot:run

# 2. Cek H2 Console
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:simdb
Username: sa
Password: password

# 3. Verify users table:
SELECT * FROM users;

# 4. Test login di:
http://localhost:3001/login.html
```

### Option 3: Manual Database Fix
Jika tabel users kosong, jalankan SQL ini di H2 Console:

```sql
-- Insert admin user
INSERT INTO users (username, email, first_name, last_name, password, user_type, is_active, email_verified_at, created_at, updated_at) VALUES 
('admin', 'admin@sim.edu', 'System', 'Administrator', '$2a$10$N.zmdr9k7uOCQb07Dvo6V.XvOuirE6jCCqCVpbRhJuTKkqKDrNoK2', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert roles if not exist
INSERT INTO roles (name, description, is_system_role, created_at, updated_at) VALUES 
('ADMIN', 'System Administrator', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Link user to role
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'admin' AND r.name = 'ADMIN';
```

## 🔍 Troubleshooting Steps

### 1. Check Frontend Server
```bash
# Pastikan frontend berjalan di port 3001
http://localhost:3001
```

### 2. Check Backend Server
```bash
# Pastikan backend berjalan di port 8080
http://localhost:8080
http://localhost:8080/actuator/health
```

### 3. Check Database
```bash
# H2 Console
http://localhost:8080/h2-console

# Check tables exist:
SHOW TABLES;

# Check users:
SELECT * FROM users;
SELECT * FROM roles;
```

### 4. Check Network
```bash
# Test API endpoint
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier": "admin", "password": "admin123"}'
```

## 📋 Test Credentials

### Mock Backend (Always Works)
- Username: `admin` / Password: `admin123`
- Username: `teacher` / Password: `teacher123`

### Real Backend (Requires database setup)
- Username: `admin` / Password: `admin123`
- Email: `admin@sim.edu` / Password: `admin123`

## 🎯 Expected Results

### Successful Login Should Show:
1. ✅ "Login successful!" message
2. ✅ Redirect to dashboard
3. ✅ User info stored in localStorage
4. ✅ JWT token generated

### Failed Login Shows:
1. ❌ "Invalid email/username or password"
2. ❌ "Cannot connect to server" (if backend down)
3. ❌ "Backend server is not available"

## 🔧 Quick Commands

```bash
# Start everything
start-app.bat

# Test login only
test-login.bat

# Restart backend only
restart-backend.bat

# Check logs
# Look at backend console for errors
# Check browser console for frontend errors
```

## 📞 If Still Not Working

1. **Check browser console** for JavaScript errors
2. **Check backend logs** for authentication errors
3. **Verify database** has users table with data
4. **Test with mock backend** to isolate issue
5. **Check network connectivity** between frontend/backend

---

**Quick Test**: Use `test-login-mock.html` - it always works and helps verify frontend is functioning correctly.