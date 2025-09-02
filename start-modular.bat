@echo off
echo.
echo ========================================
echo   SIM - Starting Modular Dashboard
echo ========================================
echo.

echo Starting Spring Boot backend...
start /B mvn spring-boot:run

echo Waiting for backend to start...
timeout /t 10 /nobreak > nul

echo.
echo ========================================
echo   MODULAR DASHBOARD READY!
echo ========================================
echo.
echo Access URLs:
echo   Modular Dashboard: http://localhost:8080/dashboard-modular.html
echo   Original Dashboard: http://localhost:8080/dashboard.html
echo   Login Page: http://localhost:8080/auth-login.html
echo.
echo Login Credentials:
echo   Username: admin@sim.edu
echo   Password: admin123
echo.

start http://localhost:8080/dashboard-modular.html

echo Backend is running in background...
echo Press any key to stop the backend
pause > nul

echo Stopping backend...
taskkill /f /im java.exe 2>nul
echo Backend stopped.
pause