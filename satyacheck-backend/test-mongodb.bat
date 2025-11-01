@echo off
echo Testing MongoDB Connection...

echo.
echo Step 1: Update your MongoDB Atlas connection string in application.properties
echo.
echo Before running this test, make sure you have:
echo 1. Created a MongoDB Atlas account
echo 2. Created a cluster
echo 3. Created a database user
echo 4. Added your IP address to the whitelist
echo 5. Updated the connection string in application.properties
echo.
echo Your current MongoDB connection string is:
findstr /c:"spring.data.mongodb.uri" src\main\resources\application.properties
echo.

choice /M "Have you updated the MongoDB Atlas connection string in application.properties"
if errorlevel 2 goto :exit

echo.
echo Step 2: Building and running the application to test MongoDB connectivity...
echo.
echo Please look for "Successfully added 3 sample articles to the database" in the logs
echo to confirm that MongoDB connection is working.
echo.
call ./gradlew bootRun --args="--spring.profiles.active=dev"

:exit
echo.
echo Test completed.