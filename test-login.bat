@echo off
echo ========================================
echo   SIM Login Testing
echo ========================================
echo.

echo Starting frontend server for login testing...
echo.

REM Check if Python is available
where python >nul 2>&1
if %errorlevel% neq 0 (
    echo Python not found. Please install Python first.
    pause
    exit /b 1
)

REM Start frontend server
cd src\main\webapp
start "SIM Frontend" cmd /c "python serve-test.py"

REM Wait for server to start
timeout /t 3 /nobreak >nul

echo.
echo ========================================
echo   Login Test Pages Available
echo ========================================
echo.
echo 1. Mock Backend Test (Recommended):
echo    http://localhost:3001/test-login-mock.html
echo.
echo 2. Real Backend Test (Requires backend running):
echo    http://localhost:3001/login.html
echo.
echo 3. Advanced Auth Test:
echo    http://localhost:3001/test-auth-simple.html
echo.
echo Test Credentials:
echo - Username: admin    Password: admin123
echo - Username: teacher  Password: teacher123
echo.
echo Opening mock login test in browser...
start http://localhost:3001/test-login-mock.html

echo.
echo Press any key to exit...
pause >nul