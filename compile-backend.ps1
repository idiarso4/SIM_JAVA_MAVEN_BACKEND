# PowerShell script to compile backend with proper environment
Write-Host "Setting up environment for Maven compilation..." -ForegroundColor Green

# Set environment variables
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.14.7-hotspot"
$env:MAVEN_HOME = "C:\Maven\apache-maven-3.9.9"
$env:PATH = "$env:MAVEN_HOME\bin;$env:PATH"

# Verify Java
Write-Host "`nChecking Java version..." -ForegroundColor Yellow
& "$env:JAVA_HOME\bin\java" -version
if ($LASTEXITCODE -ne 0) {
    Write-Host "Java is not working properly" -ForegroundColor Red
    exit 1
}

# Verify Maven
Write-Host "`nChecking Maven version..." -ForegroundColor Yellow
& mvn --version
if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven is not working properly" -ForegroundColor Red
    exit 1
}

Write-Host "`nStarting compilation..." -ForegroundColor Green
Write-Host "================================" -ForegroundColor Cyan

# Clean and compile
& mvn clean compile -DskipTests

Write-Host "`n================================" -ForegroundColor Cyan
if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful!" -ForegroundColor Green
} else {
    Write-Host "Compilation failed. Check errors above." -ForegroundColor Red
}

Write-Host "`nPress any key to continue..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")