package com.satyacheck.android.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Helper class for programmatic permission handling (non-Composable contexts)
 */
@Singleton
class PermissionHelpers @Inject constructor(private val context: Context) {
    
    // Registry to store callbacks for permission requests
    private val permissionCallbacks = mutableMapOf<String, PermissionRequestCallback>()
    
    // Modern permission launcher
    private var multiplePermissionsLauncher: ActivityResultLauncher<Array<String>>? = null
    
    /**
     * Set the permission launcher from the Activity
     */
    fun setPermissionLauncher(launcher: ActivityResultLauncher<Array<String>>) {
        multiplePermissionsLauncher = launcher
    }
    
    /**
     * Check if a permission is granted
     */
    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Request a permission programmatically and return result
     * This should be used from ViewModels or other non-composable code
     */
    suspend fun requestPermission(permission: String): Boolean {
        if (isPermissionGranted(permission)) {
            return true
        }
        
        // Create a CompletableDeferred to get the result asynchronously
        val deferred = CompletableDeferred<Boolean>()
        
        // Create and register a PermissionRequestCallback
        val callback = object : PermissionRequestCallback {
            override fun onPermissionResult(granted: Boolean) {
                deferred.complete(granted)
            }
        }
        
        // Store the callback in the registry
        permissionCallbacks[permission] = callback
        
        // Request the permission through the activity
        val activity = context.findActivity()
        activity?.let {
            ActivityCompat.requestPermissions(
                it,
                arrayOf(permission),
                getPermissionRequestCode(permission)
            )
        } ?: run {
            deferred.complete(false)
        }
        
        return deferred.await()
    }
    
    /**
     * Interface for permission request callbacks
     */
    private interface PermissionRequestCallback {
        fun onPermissionResult(granted: Boolean)
    }
    
    // Get a unique request code for each permission
    private fun getPermissionRequestCode(permission: String): Int {
        return permission.hashCode() and 0xffff
    }
    
    // Extension function to find the activity from a context
    private fun Context.findActivity(): Activity? {
        var context = this
        while (context is android.content.ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }
    
    /**
     * Handle permission result (should be called from Activity.onRequestPermissionsResult)
     */
    fun handlePermissionResult(
        @Suppress("UNUSED_PARAMETER") requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissions.forEachIndexed { index, permission ->
            val granted = grantResults.getOrNull(index) == PackageManager.PERMISSION_GRANTED
            permissionCallbacks[permission]?.onPermissionResult(granted)
            permissionCallbacks.remove(permission)
        }
    }
    
    /**
     * Handle permission results from ActivityResultCallback API
     */
    fun handleActivityResultPermissions(results: Map<String, Boolean>) {
        results.forEach { (permission, isGranted) ->
            permissionCallbacks[permission]?.onPermissionResult(isGranted)
            permissionCallbacks.remove(permission)
        }
    }
}

/**
 * Extension for the PermissionManager with additional composables
 */
object PermissionHelper {
    
    /**
     * Composable for requesting permission with rationale
     */
    @Composable
    fun RequestPermissionWithRationale(
        permission: String,
        rationaleText: String,
        onPermissionResult: (Boolean) -> Unit
    ) {
        val permissionName = remember(permission) {
            when (permission) {
                Manifest.permission.CAMERA -> "Camera"
                Manifest.permission.RECORD_AUDIO -> "Microphone"
                Manifest.permission.READ_EXTERNAL_STORAGE -> "Storage"
                Manifest.permission.READ_MEDIA_IMAGES -> "Photos"
                Manifest.permission.READ_MEDIA_AUDIO -> "Audio Files"
                Manifest.permission.ACCESS_COARSE_LOCATION -> "Location"
                Manifest.permission.POST_NOTIFICATIONS -> "Notifications"
                else -> "Permission"
            }
        }
        
        RequestPermission(
            permission = permission,
            rationaleMessage = rationaleText,
            permanentlyDeniedMessage = "You've denied $permissionName permission permanently. " +
                    "Please enable it in app settings to use this feature.",
            onPermissionResult = onPermissionResult
        )
    }
}
