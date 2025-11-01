package com.satyacheck.android.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.widget.Toast

/**
 * Helper class for managing accessibility service
 */
object AccessibilityHelper {
    private const val TAG = "AccessibilityHelper"
    private const val ACCESSIBILITY_SERVICE_CLASS = "com.satyacheck.android.service.MisinformationAccessibilityService"
    
    /**
     * Checks if the SatyaCheck accessibility service is enabled
     *
     * @param context The context to use
     * @return Boolean indicating if the service is enabled
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        try {
            // Method 1: Using AccessibilityManager
            val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            )
            
            val packageName = context.packageName
            for (service in enabledServices) {
                val serviceInfo = service.resolveInfo.serviceInfo
                if (serviceInfo.packageName == packageName && 
                    serviceInfo.name == ACCESSIBILITY_SERVICE_CLASS) {
                    Log.d(TAG, "Service is enabled: $ACCESSIBILITY_SERVICE_CLASS")
                    return true
                }
            }
            
            // Method 2: Using Settings.Secure
            val enabledServicesString = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            
            Log.d(TAG, "Enabled accessibility services: $enabledServicesString")
            
            val expectedServiceName = "$packageName/$ACCESSIBILITY_SERVICE_CLASS"
            return enabledServicesString.split(':')
                .any { it == expectedServiceName }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking accessibility service status", e)
            return false
        }
    }
    
    /**
     * Opens the accessibility settings screen with clear instructions on how to enable the service
     *
     * @param context The context to use
     */
    fun openAccessibilitySettings(context: Context) {
        // Show a toast with clear instructions
        Toast.makeText(
            context,
            "Please enable the 'SatyaCheck Misinformation Analyzer' in the Accessibility settings to analyze screen content",
            Toast.LENGTH_LONG
        ).show()
        
        // Open the accessibility settings screen for this specific service if possible
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            val componentName = ACCESSIBILITY_SERVICE_CLASS
            
            // Try to directly open the specific service settings
            val bundle = android.os.Bundle()
            bundle.putString(":settings:fragment_args_key", componentName)
            intent.putExtra(":settings:fragment_args_key", componentName)
            intent.putExtra(":settings:show_fragment_args", bundle)
            
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            
            // Log for debugging
            Log.d(TAG, "Opening accessibility settings for: $componentName")
        } catch (e: Exception) {
            // Fallback to general accessibility settings
            Log.e(TAG, "Error opening specific accessibility settings", e)
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
    
    /**
     * Shows a dialog explaining why the accessibility service is needed and how to enable it
     *
     * @param context The context to use
     * @param onProceed Callback when user decides to proceed with enabling
     */
    fun showAccessibilityInstructionsDialog(context: Context, onProceed: () -> Unit) {
        // Note: This method is implemented in the ViewModel to avoid UI in a util class
        onProceed()
    }
}
