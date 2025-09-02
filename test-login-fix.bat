@echo off
echo.
echo ========================================
echo   Testing Login Fix
echo ========================================
echo.

echo Starting backend...
start /B mvn spring-boot:run

echo Waiting for backend to start...
timeout /t 15 /nobreak > nul

echo.
echo Opening login page...
start http://localhost:8080/auth-login.html

echo.
echo ========================================
echo   LOGIN TEST CREDENTIALS
echo ========================================
echo.
echo Valid Credentials:
echo   Username: admin@sim.edu
echo   Password: admin123
echo.
echo   Username: admin
echo   Password: admin123
echo.
echo   Username: teacher@sim.edu
echo   Password: teacher123
echo.
echo   Username: user@sim.edu
echo   Password: user123
echo.
echo ========================================
echo   TESTING INSTRUCTIONS
echo ========================================
echo.
echo 1. Try login with: admin@sim.edu / admin123
echo 2. Should redirect to dashboard successfully
echo 3. If still fails, check browser console
echo.
echo Press any key to stop backend...
pause > nul

echo Stopping backend...
taskkill /f /im java.exe 2>nul
echo Backend stopped.
pause