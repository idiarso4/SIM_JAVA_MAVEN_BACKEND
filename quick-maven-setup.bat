@echo off
echo Quick Maven Setup...

REM Try to use existing Maven installation
if exist "C:\Program Files\Apache\Maven\bin\mvn.cmd" (
    set "MAVEN_HOME=C:\Program Files\Apache\Maven"
    set "PATH=%MAVEN_HOME%\bin;%PATH%"
    echo Found Maven at: %MAVEN_HOME%
    goto :test_maven
)

if exist "C:\apache-maven-3.9.9\bin\mvn.cmd" (
    set "MAVEN_HOME=C:\apache-maven-3.9.9"
    set "PATH=%MAVEN_HOME%\bin;%PATH%"
    echo Found Maven at: %MAVEN_HOME%
    goto :test_maven
)

if exist "C:\Maven\apache-maven-3.9.9\bin\mvn.cmd" (
    set "MAVEN_HOME=C:\Maven\apache-maven-3.9.9"
    set "PATH=%MAVEN_HOME%\bin;%PATH%"
    echo Found Maven at: %MAVEN_HOME%
    goto :test_maven
)

echo Maven not found. Trying to download a portable version...
mkdir temp_maven 2>nul
cd temp_maven

REM Download a smaller Maven distribution
echo Downloading Maven...
curl -L -o maven.zip "https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip"

if exist maven.zip (
    echo Extracting Maven...
    powershell -Command "Expand-Archive -Path 'maven.zip' -DestinationPath '.' -Force"
    
    set "MAVEN_HOME=%CD%\apache-maven-3.9.9"
    set "PATH=%MAVEN_HOME%\bin;%PATH%"
    
    cd ..
    echo Maven setup completed!
) else (
    echo Failed to download Maven.
    cd ..
    goto :manual_compile
)

:test_maven
echo Testing Maven...
mvn --version
if %ERRORLEVEL% EQU 0 (
    echo Maven is working! Starting Spring Boot application...
    mvn spring-boot:run
) else (
    echo Maven test failed.
    goto :manual_compile
)
goto :end

:manual_compile
echo Attempting manual compilation...
echo This requires all dependencies to be available, which is unlikely to work.
echo Please install Maven manually or use an IDE.

:end
pause
