# SIM - Quick Start Guide

## 🚀 Cara Cepat Menjalankan Aplikasi

### 1. Frontend Web Application

#### Menjalankan Development Server

```bash
# Masuk ke direktori webapp
cd SIM/src/main/webapp

# Jalankan Python server (Recommended)
python serve-test.py

# Atau gunakan batch file di Windows
start-test-server.bat
```

#### Akses Aplikasi

- **Login Page**: http://localhost:3000/login.html
- **Main Application**: http://localhost:3000/index.html
- **Auth Testing**: http://localhost:3000/test-auth-simple.html

#### Test Credentials (Mock Mode)

```
Admin User:
- Email: admin@school.com
- Password: admin123

Teacher User:
- Email: teacher@school.com
- Password: teacher123
```

### 2. Backend Spring Boot (Optional)

Jika ingin menggunakan backend real (bukan mock):

```bash
# Masuk ke direktori SIM
cd SIM

# Compile dan jalankan dengan Maven (jika Maven terinstall)
mvn spring-boot:run

# Atau jalankan dengan Java
java -jar target/sim-backend-1.0.0.jar
```

Backend akan berjalan di: http://localhost:8080

### 3. Mobile App (Flutter) - Optional

```bash
# Masuk ke direktori frontend Flutter
cd "New folder/frontend"

# Install dependencies
flutter pub get

# Jalankan di emulator/device
flutter run
```

## 📱 Fitur yang Sudah Selesai

### ✅ Authentication System
- JWT-based authentication
- Secure token storage
- Automatic token refresh
- Account lockout protection
- Role-based access control

### ✅ Login Interface
- Modern responsive design
- Real-time form validation
- Password visibility toggle
- Loading states
- Error handling

### ✅ Security Features
- Rate limiting (5 attempts)
- Account lockout (15 minutes)
- Session management
- Secure token transmission

## 🔧 Troubleshooting

### Frontend Issues

**Port 3000 sudah digunakan:**
```bash
# Edit serve-test.py dan ubah PORT = 3001
python serve-test.py
```

**Python tidak terinstall:**
- Download Python dari https://python.org
- Atau buka file HTML langsung di browser

### Backend Issues

**Maven tidak terinstall:**
- Download Maven dari https://maven.apache.org
- Atau gunakan IDE seperti IntelliJ IDEA / Eclipse

**Port 8080 sudah digunakan:**
- Edit application.yml dan ubah server.port

### Flutter Issues

**Flutter SDK tidak ditemukan:**
- Download Flutter dari https://flutter.dev
- Set environment variable FLUTTER_ROOT

## 📂 Struktur Project

```
SIM2/
├── SIM/                          # Backend Spring Boot
│   ├── src/main/webapp/          # Frontend Web
│   │   ├── login.html           # Login page
│   │   ├── index.html           # Main app
│   │   ├── js/services/         # Services
│   │   └── css/                 # Styles
│   └── src/main/java/           # Backend code
├── New folder/frontend/          # Flutter mobile app
└── README files & docs
```

## 🎯 Next Steps

1. **✅ COMPLETED**: Authentication system
2. **🔄 CURRENT**: Dashboard implementation
3. **📋 TODO**: Student management
4. **📋 TODO**: User management
5. **📋 TODO**: Reporting system

## 📞 Support

Jika mengalami masalah:
1. Periksa console browser untuk error messages
2. Pastikan semua dependencies terinstall
3. Cek dokumentasi di file README
4. Lihat log server untuk backend issues

---

**Status**: ✅ Ready for Development  
**Last Updated**: January 2025