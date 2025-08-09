@echo off
echo Testing SIM Backend Dashboard API...
echo.

echo 1. Testing login with admin credentials...
curl -X POST "http://localhost:8080/api/v1/auth/login" ^
     -H "Content-Type: application/json" ^
     -d "{\"identifier\":\"admin\",\"password\":\"admin123\"}" ^
     -c cookies.txt

echo.
echo.

echo 2. Testing dashboard stats with authentication...
curl -X GET "http://localhost:8080/api/v1/dashboard/stats" ^
     -H "accept: application/json" ^
     -b cookies.txt

echo.
echo.

echo 3. Testing student stats...
curl -X GET "http://localhost:8080/api/v1/dashboard/students/stats" ^
     -H "accept: application/json" ^
     -b cookies.txt

echo.
echo.

echo 4. Testing user stats...
curl -X GET "http://localhost:8080/api/v1/dashboard/users/stats" ^
     -H "accept: application/json" ^
     -b cookies.txt

echo.
echo.

echo Test completed!
pause