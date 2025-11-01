#!/bin/bash

# Exit on error
set -e

# Variables
PROJECT_ID="satyacheck-backend"
SERVICE_NAME="satyacheck-backend"
REGION="us-central1"
CREDENTIALS_FILE="satyacheck-credentials.json"

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    echo "Error: gcloud is not installed. Please install Google Cloud SDK first."
    exit 1
fi

# Check if credentials file exists
if [ ! -f "$CREDENTIALS_FILE" ]; then
    echo "Error: $CREDENTIALS_FILE not found. Please place your service account key file in the current directory."
    exit 1
fi

# Build the container image
echo "Building container image..."
gcloud builds submit --tag gcr.io/$PROJECT_ID/$SERVICE_NAME

# Deploy to Cloud Run
echo "Deploying to Cloud Run..."
gcloud run deploy $SERVICE_NAME \
  --image gcr.io/$PROJECT_ID/$SERVICE_NAME \
  --platform managed \
  --region $REGION \
  --allow-unauthenticated \
  --set-env-vars="GOOGLE_APPLICATION_CREDENTIALS=/app/satyacheck-credentials.json,SPRING_PROFILES_ACTIVE=prod"

# Get the URL
echo "Deployment complete! Service URL:"
gcloud run services describe $SERVICE_NAME --platform managed --region $REGION --format 'value(status.url)'