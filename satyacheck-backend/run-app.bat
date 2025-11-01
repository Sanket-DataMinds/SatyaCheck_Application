@echo off
echo Starting SatyaCheck Backend Application...
echo.

:: Check if Java is installed and in the PATH
java -version > nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Error: Java is not installed or not in the PATH.
    echo Please install Java 17 or later and try again.
    pause
    exit /b 1
)

:: Set environment variables
set SPRING_PROFILES_ACTIVE=dev
set GOOGLE_APPLICATION_CREDENTIALS=D:/z+/SatyaApp/satyacheck-backend/satyacheck-backend-281deb66d0d0.json

:: Check if the application has been built
if exist build\libs\satyacheck-backend-0.0.1-SNAPSHOT.jar (
    echo Running the application with Java...
    java -jar build\libs\satyacheck-backend-0.0.1-SNAPSHOT.jar
) else (
    echo The application needs to be built first.
    echo.
    echo 1. Install Gradle if not already installed
    echo 2. Run: gradlew build
    echo 3. Run this script again
    echo.
    echo If you have Docker installed, you can also use:
    echo docker-compose up
    echo.
)

pause