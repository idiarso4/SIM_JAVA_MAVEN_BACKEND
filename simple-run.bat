@echo off
echo Simple Spring Boot Application Runner
echo =====================================

REM Check if Java is available
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 11 or higher
    pause
    exit /b 1
)

echo Java is available!
java -version

echo.
echo Attempting to run the application...
echo.

REM Try to find Maven in common locations
set MAVEN_FOUND=0

if exist "C:\Program Files\Apache\Maven\bin\mvn.cmd" (
    set "MAVEN_HOME=C:\Program Files\Apache\Maven"
    set MAVEN_FOUND=1
)

if exist "C:\apache-maven\bin\mvn.cmd" (
    set "MAVEN_HOME=C:\apache-maven"
    set MAVEN_FOUND=1
)

if exist "C:\tools\apache-maven\bin\mvn.cmd" (
    set "MAVEN_HOME=C:\tools\apache-maven"
    set MAVEN_FOUND=1
)

if %MAVEN_FOUND% EQU 1 (
    echo Found Maven at: %MAVEN_HOME%
    set "PATH=%MAVEN_HOME%\bin;%PATH%"
    echo Testing Maven...
    mvn --version
    if %ERRORLEVEL% EQU 0 (
        echo Starting Spring Boot application with Maven...
        mvn spring-boot:run
        goto :end
    )
)

echo Maven not found or not working.
echo.
echo Alternative options:
echo 1. Install Maven manually from https://maven.apache.org/download.cgi
echo 2. Use an IDE like IntelliJ IDEA or Eclipse
echo 3. Use VS Code with Java Extension Pack
echo.
echo For VS Code:
echo 1. Install Java Extension Pack
echo 2. Open this folder in VS Code
echo 3. Use Ctrl+Shift+P and search for "Java: Run"
echo.

:end
pause
