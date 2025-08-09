@echo off
echo Inserting test data to H2 database...
echo.

echo Creating test users and students via API...

echo 1. Creating admin user via direct database insert...
curl -X POST "http://localhost:8080/api/v1/migration/init-data" ^
     -H "Content-Type: application/json"

echo.
echo.

echo Test data insertion completed!
pause