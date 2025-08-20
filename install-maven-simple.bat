@echo off
echo Installing Maven (Portable Version)...
echo.

REM Create Maven directory
mkdir maven-portable 2>nul
cd maven-portable

echo Downloading Maven 3.8.8 (smaller version)...
curl -L -o maven.zip "https://archive.apache.org/dist/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.zip"

if not exist maven.zip (
    echo Download failed. Trying alternative method...
    powershell -Command "try { Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.zip' -OutFile 'maven.zip' } catch { Write-Host 'Download failed' }"
)

if exist maven.zip (
    echo Extracting Maven...
    powershell -Command "try { Expand-Archive -Path 'maven.zip' -DestinationPath '.' -Force } catch { Write-Host 'Extraction failed' }"
    
    if exist apache-maven-3.8.8 (
        echo Maven extracted successfully!
        cd ..
        
        REM Set environment variables for current session
        set "MAVEN_HOME=%CD%\maven-portable\apache-maven-3.8.8"
        set "PATH=%MAVEN_HOME%\bin;%PATH%"
        
        echo Testing Maven...
        mvn --version
        
        if %ERRORLEVEL% EQU 0 (
            echo.
            echo Maven is working! Starting Spring Boot application...
            echo.
            mvn clean spring-boot:run
        ) else (
            echo Maven test failed.
        )
    ) else (
        echo Extraction failed.
        cd ..
    )
) else (
    echo Download failed.
    cd ..
)

pause
