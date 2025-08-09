@echo off
echo ========================================
echo   Starting SIM Backend for Real Login
echo ========================================
echo.

REM Check if we're in the correct directory
if not exist "src\main\java" (
    echo Error: Please run this script from the SIM project root directory
    pause
    exit /b 1
)

echo Preparing backend for real login test...
echo.

REM Clean and compile
echo [1/4] Cleaning and compiling project...
call mvn clean compile -q
if %errorlevel% neq 0 (
    echo Failed to compile project
    pause
    exit /b 1
)

echo [2/4] Packaging application...
call mvn package -DskipTests -q
if %errorlevel% neq 0 (
    echo Failed to package application
    pause
    exit /b 1
)

echo [3/4] Starting Spring Boot application...
echo.
echo Backend will be available at: http://localhost:8080
echo H2 Console: http://localhost:8080/h2-console
echo API Docs: http://localhost:8080/swagger-ui.html
echo.
echo Default login credentials:
echo - Username: admin    Password: admin123
echo - Username: teacher  Password: teacher123
echo.
echo [4/4] Launching application...
echo.

REM Start the application
java -jar target\sim-backend-1.0.0.jar

echo.
echo Backend stopped.
pause