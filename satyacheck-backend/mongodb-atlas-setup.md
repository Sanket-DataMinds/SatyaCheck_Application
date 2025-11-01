# MongoDB Atlas Setup Guide

This guide will help you set up a free MongoDB Atlas cluster to use with the Satyacheck backend application.

## Step 1: Create a MongoDB Atlas Account

1. Go to [MongoDB Atlas](https://www.mongodb.com/cloud/atlas/register)
2. Sign up for a free account or log in if you already have one

## Step 2: Create a Cluster

1. Click on "Build a Database"
2. Choose the "FREE" tier option
3. Select your preferred cloud provider (AWS, Google Cloud, or Azure) and a region near you
4. Click "Create Cluster" (this may take a few minutes to complete)

## Step 3: Configure Network Access

1. While the cluster is being created, click on "Network Access" in the left menu
2. Click "Add IP Address"
3. For development purposes, you can add `0.0.0.0/0` to allow access from anywhere (not recommended for production)
4. Alternatively, add your specific IP address
5. Click "Confirm"

## Step 4: Create a Database User

1. Click on "Database Access" in the left menu
2. Click "Add New Database User"
3. Choose "Password" for authentication method
4. Enter a username and a secure password
5. Under "Database User Privileges", select "Read and Write to Any Database"
6. Click "Add User"

## Step 5: Get Your Connection String

1. Go back to your cluster by clicking "Database" in the left menu
2. Click "Connect" on your cluster
3. Choose "Connect your application"
4. Select "Java" and version "4.3 or later"
5. Copy the connection string (it will look like `mongodb+srv://username:<password>@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority`)
6. Replace `<password>` with your database user's password
7. Add your database name at the end of the connection string: `mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/satyacheck?retryWrites=true&w=majority`

## Step 6: Update Application Configuration

1. Update the `application.properties` file in your Spring Boot application with the new connection string:

```
spring.data.mongodb.uri=mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/satyacheck?retryWrites=true&w=majority
```

Replace `username`, `password`, and the cluster URL with your actual values.

## Step 7: Verify Connection

1. Run the test-mongodb.bat script:
   ```
   .\test-mongodb.bat
   ```
2. Check the logs to ensure that it connects to MongoDB Atlas successfully
3. Look for the message: "Successfully added 3 sample articles to the database" which confirms that the connection is working

## Troubleshooting

If you encounter connection issues:

1. **Network Problems**: Make sure your IP address is in the Atlas IP whitelist
2. **Username/Password Issues**: Verify the username and password in the connection string
3. **Connection String Format**: Ensure the connection string format is correct
4. **Firewall Issues**: Check if any firewall is blocking the connection
5. **MongoDB Logs**: Check the MongoDB logs in the Atlas UI for any errors