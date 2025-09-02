@echo off
echo.
echo ========================================
echo   Cleaning Dashboard HTML
echo ========================================
echo.

echo Removing all inline JavaScript from dashboard.html...

powershell -Command "$content = Get-Content 'dashboard.html' -Raw; $content = $content -replace '(?s)<script>.*?</script>', ''; Set-Content 'dashboard.html' $content"

echo.
echo Dashboard HTML cleaned!
echo All JavaScript code moved to modular files.
echo.
echo Use dashboard-modular.html for clean modular version.
echo.
pause