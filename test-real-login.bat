@echo off
echo ========================================
echo   SIM Real Login Test
echo ========================================
echo.

echo Starting full stack application...
echo.

REM Start backend in background
echo [1/2] Starting backend server...
start "SIM Backend" cmd /c "cd /d %~dp0 && start-backend-real.bat"

REM Wait for backend to start
echo Waiting for backend to initialize...
timeout /t 10 /nobreak >nul

REM Start frontend
echo [2/2] Starting frontend server...
cd src\main\webapp
start "SIM Frontend" cmd /c "python serve-test.py"

REM Wait for frontend to start
timeout /t 3 /nobreak >nul

echo.
echo ========================================
echo   Real Login Test Ready!
echo ========================================
echo.
echo Backend:  http://localhost:8080
echo Frontend: http://localhost:3001
echo.
echo Login Page: http://localhost:3001/login.html
echo.
echo Test Credentials:
echo - Username: admin    Password: admin123
echo - Username: teacher  Password: teacher123
echo.
echo Backend Status Check:
curl -s http://localhost:8080/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Backend is running
) else (
    echo ❌ Backend not responding - check backend window
)
echo.
echo Opening login page...
start http://localhost:3001/login.html

echo.
echo Both servers are running. Check the separate windows for logs.
echo Press any key to exit (this will NOT stop the servers)...
pause >nul