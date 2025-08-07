@echo off
echo ========================================
echo   SIM Frontend Development Server
echo ========================================
echo.

REM Check if Python is available
where python >nul 2>&1
if %errorlevel% neq 0 (
    echo Python is not installed or not in PATH
    echo Please install Python first
    echo.
    echo Alternative methods:
    echo 1. Open login.html directly in your browser
    echo 2. Use any other HTTP server
    echo.
    pause
    exit /b 1
)

echo Starting Python HTTP server...
echo Server will be available at: http://localhost:3000
echo.
echo Available pages:
echo - Login Page: http://localhost:3000/login.html
echo - Main App: http://localhost:3000/index.html
echo - Auth Test: http://localhost:3000/test-auth-simple.html
echo - Login Test: http://localhost:3000/test-login.html
echo.
echo Press Ctrl+C to stop the server
echo.

python serve-test.py

pause