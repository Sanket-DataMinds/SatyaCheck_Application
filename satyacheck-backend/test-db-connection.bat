@echo off
echo Testing MongoDB Connection...

echo.
echo Checking MongoDB Atlas connection string...

echo.
echo Using connection string from application.properties:
findstr /c:"spring.data.mongodb.uri" src\main\resources\application.properties

echo.
echo Running connection test...
echo.

:: Run using Gradle to test the connection
call ./gradlew bootRun --args="--spring.profiles.active=test --spring.main.web-application-type=none --test-db-connection=true" --console=plain

echo.
if %ERRORLEVEL% == 0 (
  echo Database connection test completed successfully!
) else (
  echo Database connection test failed! Please check your connection string and network connectivity.
)

pause
