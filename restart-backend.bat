@echo off
echo ========================================
echo   Restarting SIM Backend Server
echo ========================================
echo.

REM Kill any existing Java processes for SIM
echo Stopping existing backend processes...
taskkill /F /IM java.exe /FI "WINDOWTITLE eq SIM Backend*" 2>nul

REM Wait a moment
timeout /t 2 /nobreak >nul

echo Starting backend server...
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
        pause
        exit /b 1
    )
)

echo.
echo Backend server is starting...
echo Check the backend window for startup logs.
echo.
echo Backend will be available at: http://localhost:8080
echo API Documentation: http://localhost:8080/swagger-ui.html
echo H2 Console: http://localhost:8080/h2-console
echo.
echo Default login credentials:
echo - Username: admin
echo - Password: admin123
echo.
pause