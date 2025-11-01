@echo off
echo Testing Authentication API...

echo.
echo Step 1: Register a new user
echo.
curl -X POST http://localhost:8080/api/auth/register ^
     -H "Content-Type: application/json" ^
     -d "{\"username\":\"testuser\",\"password\":\"testpassword\",\"email\":\"test@example.com\",\"name\":\"Test User\"}"

echo.
echo.
echo Step 2: Login with the user credentials
echo.
curl -X POST http://localhost:8080/api/auth/login ^
     -H "Content-Type: application/json" ^
     -d "{\"username\":\"testuser\",\"password\":\"testpassword\"}"

echo.
echo.
echo Step 3: Use the access token to access a protected endpoint
echo.
echo Replace YOUR_ACCESS_TOKEN with the token from the login response
echo.
echo curl -X POST http://localhost:8080/api/analyze ^
echo      -H "Content-Type: application/json" ^
echo      -H "Authorization: Bearer YOUR_ACCESS_TOKEN" ^
echo      -d "{\"content\":\"This is sample text to analyze\",\"contentType\":\"TEXT\",\"language\":\"en\"}"

echo.
echo.
echo Step 4: Use the refresh token to get a new access token
echo.
echo Replace YOUR_REFRESH_TOKEN with the refresh token from the login response
echo.
echo curl -X POST http://localhost:8080/api/auth/refresh ^
echo      -H "Content-Type: application/json" ^
echo      -d "{\"refreshToken\":\"YOUR_REFRESH_TOKEN\"}"

pause