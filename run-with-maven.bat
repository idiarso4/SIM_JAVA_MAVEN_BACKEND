@echo off
echo Running Spring Boot Application with Maven...
echo.

REM Check if Maven was extracted
if exist "maven-portable\apache-maven-3.8.8\bin\mvn.cmd" (
    echo Found Maven installation!
    
    REM Set Maven environment
    set "MAVEN_HOME=%CD%\maven-portable\apache-maven-3.8.8"
    set "PATH=%MAVEN_HOME%\bin;%PATH%"
    
    echo MAVEN_HOME: %MAVEN_HOME%
    echo.
    
    echo Testing Maven...
    "%MAVEN_HOME%\bin\mvn.cmd" --version
    
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo Maven is working! Starting Spring Boot application...
        echo This may take a few minutes for first run (downloading dependencies)...
        echo.
        
        REM Run the Spring Boot application
        "%MAVEN_HOME%\bin\mvn.cmd" clean spring-boot:run
    ) else (
        echo Maven test failed.
    )
) else (
    echo Maven not found. Please run install-maven-simple.bat first.
)

pause
