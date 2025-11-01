# Fixing SatyaCheck Android App Connectivity Issues

The issue with your SatyaCheck Android app is that it's trying to connect to `https://api.satyacheck.com/` which is not available. We need to update the app to connect to your local backend server running on your computer.

## Steps to Fix Connectivity

1. **Update the API Client** in your Android app:

   Open `app/src/main/java/com/satyacheck/android/network/SatyaCheckApiClient.kt` (or similar file) and change the `BASE_URL` constant:

   ```kotlin
   // Change from:
   private const val BASE_URL = "https://api.satyacheck.com/"
   
   // To:
   private const val BASE_URL = "http://10.0.2.2:8080/"  // For Android emulator
   // OR
   private const val BASE_URL = "http://192.168.231.254:8080/"  // For real device (use your computer's IP)
   ```

   - Use `10.0.2.2` if testing on an Android emulator (this is a special address that connects to your computer's localhost)
   - Use your actual IP address (`192.168.231.254` in your case) if testing on a physical device

2. **Update Network Security Configuration** to allow HTTP connections:

   Create a file at `app/src/main/res/xml/network_security_config.xml` if it doesn't exist:
   
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <network-security-config>
       <domain-config cleartextTrafficPermitted="true">
           <domain includeSubdomains="true">10.0.2.2</domain>
           <domain includeSubdomains="true">192.168.231.254</domain>
           <domain includeSubdomains="true">localhost</domain>
       </domain-config>
   </network-security-config>
   ```

3. **Update AndroidManifest.xml** to use the network security config:

   Add or update the `android:networkSecurityConfig` attribute in your `<application>` tag:
   
   ```xml
   <application
       ...
       android:networkSecurityConfig="@xml/network_security_config"
       ... >
   ```

4. **Add INTERNET permission** if not already present in AndroidManifest.xml:

   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   ```

5. **Rebuild and install** the app:

   ```
   cd Satyacheck_Android
   .\optimized-install.bat
   ```

## Troubleshooting

If you're still experiencing connectivity issues, check the following:

1. Make sure your backend server is actually running on port 8080
2. Ensure your computer's firewall allows incoming connections on port 8080
3. Verify the device and computer are on the same network
4. Try using the Android Logcat to see network error messages:
   ```
   adb logcat | findstr "OkHttp"
   ```

## Testing Backend Connection

You can test if the backend is accessible using:

```
curl http://localhost:8080/actuator/health
```

Or from another device:

```
curl http://192.168.231.254:8080/actuator/health
```

This should return a JSON response indicating the server status.