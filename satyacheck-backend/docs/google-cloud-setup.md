# Google Cloud API Setup Guide

This guide walks you through setting up the necessary Google Cloud APIs for the Satyacheck backend.

## Step 1: Create a Google Cloud Project

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Click on the project dropdown at the top of the page
3. Click "New Project"
4. Enter "satyacheck-backend" as the project name
5. Click "Create"
6. Wait for the project to be created and select it

## Step 2: Enable APIs

1. In the Google Cloud Console, go to "APIs & Services" > "Library"
2. Search for and enable the following APIs:
   - "Google Cloud Natural Language API"
   - "Vertex AI API" (contains Gemini API)
   - "Cloud Storage" (may be needed for some operations)

## Step 3: Create API Keys

1. Go to "APIs & Services" > "Credentials"
2. Click "Create Credentials" > "API Key"
3. Copy the generated API key - this will be your Natural Language API key
4. Rename it to "Natural Language API Key" for clarity
5. Repeat steps 2-4 to create another API key for Gemini API

## Step 4: Create Service Account

1. Go to "IAM & Admin" > "Service Accounts"
2. Click "Create Service Account"
3. Enter "satyacheck-backend-service" as the service account name
4. Click "Create and Continue"
5. Assign the following roles:
   - "Natural Language API User"
   - "Vertex AI User"
   - "Storage Object Viewer" (if needed)
6. Click "Continue" and then "Done"

## Step 5: Create and Download Service Account Key

1. From the Service Accounts list, click on your newly created service account
2. Go to the "Keys" tab
3. Click "Add Key" > "Create new key"
4. Select "JSON" as the key type
5. Click "Create"
6. The key file will be automatically downloaded to your computer
7. Rename it to "satyacheck-backend-key.json" for clarity

## Step 6: Configure the Application

1. Move the downloaded service account key file to the backend project directory
2. Update the `application.properties` file with your API keys and service account key path:

```properties
# Google Cloud credentials
google.cloud.credentials.path=<path-to-your-service-account-key-file>
google.cloud.project-id=satyacheck-backend

# API Keys
google.cloud.natural-language.api-key=<your-natural-language-api-key>
google.cloud.gemini.api-key=<your-gemini-api-key>
```

## Step 7: Verify Configuration

1. Run the application with `./gradlew bootRun`
2. Test the analyze endpoint with a sample request
3. Check the logs to ensure that the Google Cloud APIs are being called successfully

## Troubleshooting

- **API Quota Issues**: By default, new Google Cloud projects have conservative quotas. If you encounter quota issues, you may need to request a quota increase.
- **Authentication Errors**: Ensure that the service account key file is correctly formatted and accessible to the application.
- **API Key Restrictions**: If you've set restrictions on your API keys, ensure they allow requests from your application's IP address.

## Security Best Practices

- Never commit API keys or service account keys to version control
- Use environment variables or a secure vault for storing credentials in production
- Restrict API keys to specific IPs or services when possible
- Regularly rotate API keys and service account keys