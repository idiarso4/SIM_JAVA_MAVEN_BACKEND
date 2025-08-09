@echo off
echo Testing SIM API...
echo.

echo 1. System Status:
curl -s -X GET http://localhost:8080/api/test/system/status
echo.
echo.

echo 2. Dashboard Data:
curl -s -X GET http://localhost:8080/api/test/dashboard-data
echo.
echo.

echo 3. Students:
curl -s -X GET "http://localhost:8080/api/test/students/sample?page=0&size=3"
echo.
echo.

echo Test complete!
pause