@echo off
echo ========================================
echo   SIM - School Information Management
echo ========================================
echo.

REM Check if we're in the correct directory
if not exist "src\main\webapp" (
    echo Error: Please run this script from the SIM project root directory
    echo Current directory: %CD%
    pause
    exit /b 1
)

echo Starting SIM Application...
echo.

REM Check if Python is available for frontend
where python >nul 2>&1
if %errorlevel% neq 0 (
    echo Warning: Python not found. Frontend server may not start.
    echo Please install Python or run frontend manually.
    echo.
)

REM Check if Java is available for backend
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo Warning: Java not found. Backend may not start.
    echo Please install Java JDK 17 or higher.
    echo.
)

echo ========================================
echo   Starting Frontend Server (Port 3001)
echo ========================================
echo.

REM Start frontend server in background
start "SIM Frontend" cmd /c "cd src\main\webapp && python serve-test.py"

REM Wait a moment for frontend to start
timeout /t 3 /nobreak >nul

echo ========================================
echo   Starting Backend Server (Port 8080)
echo ========================================
echo.

REM Check if Maven is available
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    echo Using Maven to start backend...
    start "SIM Backend" cmd /c "mvn spring-boot:run"
) else (
    echo Maven not found. Checking for compiled JAR...
    if exist "target\sim-backend-1.0.0.jar" (
        echo Starting backend from JAR file...
        start "SIM Backend" cmd /c "java -jar target\sim-backend-1.0.0.jar"
    ) else (
        echo Backend JAR not found. Please compile the project first:
        echo   mvn clean package
        echo.
        echo Or install Maven and run:
        echo   mvn spring-boot:run
        echo.
    )
)

REM Wait for servers to start
echo.
echo Waiting for servers to start...
timeout /t 5 /nobreak >nul

echo.
echo ========================================
echo   SIM Application Started!
echo ========================================
echo.
echo Frontend: http://localhost:3001
echo Backend:  http://localhost:8080
echo.
echo Available Pages:
echo - Login Page:    http://localhost:3001/login.html
echo - Main App:      http://localhost:3001/index.html
echo - Auth Test:     http://localhost:3001/test-auth-simple.html
echo.
echo Test Credentials (if backend not running):
echo - Email: admin@school.com
echo - Password: admin123
echo.
echo Press any key to open the application in your browser...
pause >nul

REM Open browser
start http://localhost:3001/login.html

echo.
echo Application opened in browser.
echo.
echo To stop the servers:
echo - Close the frontend and backend command windows
echo - Or press Ctrl+C in each window
echo.
echo Press any key to exit this launcher...
pause >nul