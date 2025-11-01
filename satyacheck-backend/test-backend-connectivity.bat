@echo off
echo Testing SatyaCheck Backend Connectivity...
echo.

echo [1/4] Testing MongoDB connection...
mongosh --eval "db.serverStatus()" mongodb://localhost:27017/satyacheck
if %ERRORLEVEL% NEQ 0 (
    echo MongoDB connection failed! Make sure MongoDB is running.
    pause
    exit /b 1
)
echo MongoDB connection successful!
echo.

echo [2/4] Testing if backend port is open...
netstat -ano | findstr "8080"
if %ERRORLEVEL% NEQ 0 (
    echo Port 8080 is not in use. Backend is not running.
    echo.
    echo Please start the backend server first using docker-compose up -d or run-app.bat
    pause
    exit /b 1
)
echo Backend port check successful!
echo.

echo [3/4] Testing backend health endpoint...
curl -s http://localhost:8080/actuator/health
if %ERRORLEVEL% NEQ 0 (
    echo Backend health endpoint is not responding. Backend may be starting up or not configured correctly.
    pause
    exit /b 1
)
echo.
echo Backend health check successful!
echo.

echo [4/4] Testing API connection from a device perspective...
echo Note: This test should match the URL you're using in your Android app
echo Testing connection to http://192.168.231.254:8080/actuator/health...
curl -s http://192.168.231.254:8080/actuator/health
if %ERRORLEVEL% NEQ 0 (
    echo Backend is not accessible from external devices.
    echo This may be due to firewall settings or network configuration.
    echo Try disabling your firewall or adding an exception for port 8080.
    pause
    exit /b 1
)
echo.
echo Backend is accessible from external devices!

echo.
echo ============================================================
echo All connectivity tests passed! Your backend should be accessible.
echo.
echo For Android devices: Use BASE_URL = "http://192.168.231.254:8080/"
echo For Android emulator: Use BASE_URL = "http://10.0.2.2:8080/"
echo ============================================================
echo.

pause