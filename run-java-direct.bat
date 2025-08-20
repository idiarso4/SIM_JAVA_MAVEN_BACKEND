@echo off
echo Attempting to run Spring Boot application directly with Java...
echo.

REM Create output directory
if not exist "target\classes" mkdir "target\classes"

echo Compiling Java files...
echo.

REM Try to compile the main application class
javac -cp "." -d "target\classes" "src\main\java\com\school\sim\SimBackendApplication.java"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Compilation failed. This is expected because we need Spring Boot dependencies.
    echo.
    echo Recommended solutions:
    echo 1. Use VS Code with Java Extension Pack (currently installing)
    echo 2. Install Maven and run: mvn spring-boot:run
    echo 3. Use IntelliJ IDEA or Eclipse IDE
    echo.
    goto :end
)

echo Compilation successful! Attempting to run...
java -cp "target\classes" com.school.sim.SimBackendApplication

:end
pause
