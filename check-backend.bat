@echo off
echo.
echo ========================================
echo   Checking Backend Status
echo ========================================
echo.

echo Testing API endpoints...
echo.

echo 1. Testing system status:
curl -s http://localhost:8080/api/test/system/status
echo.
echo.

echo 2. Testing dashboard data:
curl -s http://localhost:8080/api/test/dashboard-data
echo.
echo.

echo 3. Testing students data:
curl -s http://localhost:8080/api/test/students/sample
echo.
echo.

echo ========================================
echo   Backend Check Complete
echo ========================================
echo.
echo If you see JSON responses above, backend is ready!
echo If you see errors, backend is still starting up.
echo.
pause