# PowerShell script to run backend with proper environment
Write-Host "Starting SIM Backend Application..." -ForegroundColor Green

# Set environment variables
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.14.7-hotspot"
$env:MAVEN_HOME = "C:\Maven\apache-maven-3.9.9"
$env:PATH = "$env:MAVEN_HOME\bin;$env:PATH"

Write-Host "`nEnvironment configured:" -ForegroundColor Yellow
Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Cyan
Write-Host "MAVEN_HOME: $env:MAVEN_HOME" -ForegroundColor Cyan

Write-Host "`nStarting Spring Boot application..." -ForegroundColor Green
Write-Host "The application will be available at:" -ForegroundColor Yellow
Write-Host "- Home: http://localhost:8080" -ForegroundColor Cyan
Write-Host "- API Documentation: http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
Write-Host "- Health Check: http://localhost:8080/actuator/health" -ForegroundColor Cyan
Write-Host ""

# Run the application
& mvn spring-boot:run "-Dspring-boot.run.profiles=development"

Write-Host "`nApplication stopped." -ForegroundColor Yellow