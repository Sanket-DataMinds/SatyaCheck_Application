@echo off
SETLOCAL

REM Variables
SET PROJECT_ID=satyacheck-backend
SET SERVICE_NAME=satyacheck-backend
SET REGION=us-central1
SET CREDENTIALS_FILE=satyacheck-credentials.json

REM Check if gcloud is installed
WHERE gcloud >nul 2>nul
IF %ERRORLEVEL% NEQ 0 (
    echo Error: gcloud is not installed. Please install Google Cloud SDK first.
    exit /b 1
)

REM Check if credentials file exists
IF NOT EXIST %CREDENTIALS_FILE% (
    echo Error: %CREDENTIALS_FILE% not found. Please place your service account key file in the current directory.
    exit /b 1
)

REM Build the container image
echo Building container image...
gcloud builds submit --tag gcr.io/%PROJECT_ID%/%SERVICE_NAME%

REM Deploy to Cloud Run
echo Deploying to Cloud Run...
gcloud run deploy %SERVICE_NAME% ^
  --image gcr.io/%PROJECT_ID%/%SERVICE_NAME% ^
  --platform managed ^
  --region %REGION% ^
  --allow-unauthenticated ^
  --set-env-vars="GOOGLE_APPLICATION_CREDENTIALS=/app/satyacheck-credentials.json,SPRING_PROFILES_ACTIVE=prod"

REM Get the URL
echo Deployment complete! Service URL:
gcloud run services describe %SERVICE_NAME% --platform managed --region %REGION% --format "value(status.url)"

ENDLOCAL