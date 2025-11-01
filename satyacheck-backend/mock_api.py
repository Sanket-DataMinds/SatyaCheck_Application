from http.server import BaseHTTPRequestHandler, HTTPServer
import json
import time
import datetime
import urllib.parse

HOST = "0.0.0.0"  # Bind to all interfaces so it's accessible from other devices
PORT = 8080

# Set to True for more detailed logging
VERBOSE_LOGGING = True

class MockApiHandler(BaseHTTPRequestHandler):
    def _set_headers(self, status_code=200):
        self.send_response(status_code)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')  # Allow cross-origin requests
        self.end_headers()

    def log_request_details(self):
        """Log detailed information about the request"""
        client_ip = self.client_address[0]
        timestamp = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        method = self.command
        path = self.path
        
        # Log the request headers if verbose logging is enabled
        headers_log = ""
        if VERBOSE_LOGGING:
            headers_log = "\n  Headers:"
            for header, value in self.headers.items():
                headers_log += f"\n    {header}: {value}"
        
        # Print the basic request info
        print(f"\n==== REQUEST DETAILS ====")
        print(f"Time: {timestamp}")
        print(f"Client IP: {client_ip}")
        print(f"Method: {method}")
        print(f"Path: {path}")
        
        # Print headers if verbose
        if VERBOSE_LOGGING:
            print(headers_log)
        
        # For GET requests, parse and log query parameters
        if method == "GET" and "?" in path:
            base_path, query_string = path.split("?", 1)
            query_params = urllib.parse.parse_qs(query_string)
            print(f"  Query Parameters:")
            for param, values in query_params.items():
                print(f"    {param}: {', '.join(values)}")
                
        print("=======================\n")

    def do_GET(self):
        self.log_request_details()
        
        if self.path == "/":
            self._set_headers()
            response = {
                "status": "success",
                "message": "SatyaCheck API Mock Server is running!"
            }
            self.wfile.write(json.dumps(response).encode())
        elif self.path == "/actuator/health":
            self._set_headers()
            response = {
                "status": "UP",
                "components": {
                    "db": {"status": "UP"},
                    "diskSpace": {"status": "UP"},
                    "ping": {"status": "UP"}
                }
            }
            self.wfile.write(json.dumps(response).encode())
        else:
            self._set_headers(404)
            response = {
                "status": "error",
                "message": "Not found"
            }
            self.wfile.write(json.dumps(response).encode())
            
    def do_POST(self):
        content_length = int(self.headers['Content-Length'])
        post_data = self.rfile.read(content_length).decode('utf-8')
        
        # Log the basic request info
        self.log_request_details()
        
        # For POST requests, log the request body
        print("==== REQUEST BODY ====")
        try:
            # Try to parse as JSON for pretty printing
            json_data = json.loads(post_data)
            print(json.dumps(json_data, indent=2))
        except:
            # If not valid JSON, print as plain text
            print(post_data)
        print("=====================\n")
        
        if self.path == "/api/analyze/text" or self.path == "/api/analyze":
            self._set_headers()
            # Simulate processing time
            time.sleep(1)
            
            # Format a better response based on the content
            # For demonstration purposes, we'll provide different verdicts based on keywords
            if "fake" in post_data.lower() or "false" in post_data.lower():
                verdict = "HIGH_MISINFORMATION_RISK"
                explanation = "This content contains claims that are known to be false or highly suspicious."
            elif "misleading" in post_data.lower() or "exaggerated" in post_data.lower():
                verdict = "POTENTIALLY_MISLEADING"
                explanation = "This content appears to contain exaggerated or potentially misleading information."
            elif "scam" in post_data.lower() or "fraud" in post_data.lower():
                verdict = "SCAM_ALERT"
                explanation = "Warning: This content matches patterns commonly seen in fraudulent schemes."
            else:
                verdict = "CREDIBLE"
                explanation = "This content appears to be credible based on our analysis."
                
            response = {
                "status": "success",
                "analysis": {
                    "verdict": verdict,
                    "explanation": explanation,
                    "confidence": 0.85,
                    "source": "mock-api",
                    "processingTime": 1024
                }
            }
            self.wfile.write(json.dumps(response).encode())
        else:
            self._set_headers(404)
            response = {
                "status": "error",
                "message": "Endpoint not found"
            }
            self.wfile.write(json.dumps(response).encode())
    
    def do_OPTIONS(self):
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        self.end_headers()

def run_server():
    server_address = (HOST, PORT)
    httpd = HTTPServer(server_address, MockApiHandler)
    print(f"Starting mock API server on {HOST}:{PORT}")
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("Server stopped.")

if __name__ == "__main__":
    run_server()