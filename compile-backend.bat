@echo off
echo Setting up environment for Maven compilation...
echo.

REM Set JAVA_HOME
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.14.7-hotspot"
if not exist "%JAVA_HOME%" (
    echo JAVA_HOME path does not exist: %JAVA_HOME%
    echo Please check your Java installation
    pause
    exit /b 1
)

REM Set Maven path
set "MAVEN_HOME=C:\Maven\apache-maven-3.9.9"
set "PATH=%MAVEN_HOME%\bin;%PATH%"

REM Verify Java version
echo Checking Java version...
"%JAVA_HOME%\bin\java" -version
if %errorlevel% neq 0 (
    echo Java is not working properly
    pause
    exit /b 1
)

echo.
echo Checking Maven version...
mvn --version
if %errorlevel% neq 0 (
    echo Maven is not working properly
    pause
    exit /b 1
)

echo.
echo Starting compilation...
echo ================================

REM Clean and compile with verbose output
mvn clean compile -DskipTests -X

echo.
echo ================================
echo Compilation finished. Check output above for any errors.
pause