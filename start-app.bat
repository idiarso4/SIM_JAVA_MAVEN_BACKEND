@echo off
echo Changing to project directory...
cd /d "C:\xampp\htdocs\SIM2\SIM"

echo Setting environment variables...
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.14.7-hotspot
set PATH=C:\Maven\apache-maven-3.9.9\bin;%PATH%

echo Starting Spring Boot application...
echo The application will be available at:
echo - Home: http://localhost:8080
echo - API Documentation: http://localhost:8080/swagger-ui.html
echo - Health Check: http://localhost:8080/actuator/health
echo.

mvn spring-boot:run -Dspring-boot.run.profiles=development

pause