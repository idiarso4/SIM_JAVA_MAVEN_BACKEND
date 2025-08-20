@echo off
echo Installing Maven...

REM Create Maven directory
if not exist "C:\Maven" mkdir "C:\Maven"

REM Download Maven (using PowerShell)
echo Downloading Maven 3.9.9...
powershell -Command "& {Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip' -OutFile 'C:\Maven\maven.zip'}"

REM Extract Maven
echo Extracting Maven...
powershell -Command "& {Expand-Archive -Path 'C:\Maven\maven.zip' -DestinationPath 'C:\Maven' -Force}"

REM Clean up
del "C:\Maven\maven.zip"

REM Set environment variables for current session
set "MAVEN_HOME=C:\Maven\apache-maven-3.9.9"
set "PATH=%MAVEN_HOME%\bin;%PATH%"

echo Maven installation completed!
echo MAVEN_HOME: %MAVEN_HOME%
echo.
echo Testing Maven installation...
mvn --version

pause
