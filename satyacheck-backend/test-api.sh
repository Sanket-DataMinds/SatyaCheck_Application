#!/bin/sh

echo "Creating a simple test endpoint with netcat..."
echo "This will allow us to test connectivity from the Android app"

# Create a simple HTTP response
cat > response.http << 'EOL'
HTTP/1.1 200 OK
Content-Type: application/json
Connection: close

{
  "status": "success",
  "message": "SatyaCheck Test API is running",
  "timestamp": "$(date +%s)"
}
EOL

# Install netcat to serve our simple response
apk update
apk add netcat-openbsd

echo "Starting simple HTTP server on port 8080..."
while true; do
  cat response.http | sed "s/\$(date +%s)/$(date +%s)/g" | nc -l -p 8080
  echo "Request handled, listening again..."
done