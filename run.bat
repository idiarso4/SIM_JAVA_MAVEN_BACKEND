@echo off
echo Starting School Information Management System...
echo.

REM Check if Maven is installed
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Maven is not installed or not in PATH
    echo Please install Maven first
    pause
    exit /b 1
)

echo Compiling and running the application...
echo.

REM Clean and compile the project
mvn clean compile -DskipTests

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Starting Spring Boot application...
echo The application will be available at:
echo - Home: http://localhost:8080
echo - API Documentation: http://localhost:8080/swagger-ui.html
echo - Health Check: http://localhost:8080/actuator/health
echo.

REM Run the application
mvn spring-boot:run -Dspring-boot.run.profiles=development

pause