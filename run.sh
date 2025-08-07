#!/bin/bash

echo "Starting School Information Management System..."
echo

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed or not in PATH"
    echo "Please install Maven first"
    exit 1
fi

echo "Compiling and running the application..."
echo

# Clean and compile the project
mvn clean compile -DskipTests

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

echo
echo "Starting Spring Boot application..."
echo "The application will be available at:"
echo "- Home: http://localhost:8080"
echo "- API Documentation: http://localhost:8080/swagger-ui.html"
echo "- Health Check: http://localhost:8080/actuator/health"
echo

# Run the application
mvn spring-boot:run -Dspring-boot.run.profiles=development