package com.satyacheck.android.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Utility class to handle permission requests in a privacy-friendly manner
 */
@Singleton
class PermissionManager @Inject constructor(private val context: Context) {

    // Permission groups
    val basicPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf()
    }
    
    val analysisPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )
    
    val storagePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
    
    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    
    /**
     * Check if a permission is granted
     */
    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if all permissions in a group are granted
     */
    fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all { hasPermission(it) }
    }
    
    /**
     * Check if we should show rationale for a permission
     */
    fun shouldShowRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
    
    /**
     * Opens app settings to allow the user to enable permissions
     */
    fun openAppSettings() {
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).also {
            context.startActivity(it)
        }
    }
    
    /**
     * Get human-readable names for permissions
     */
    fun getPermissionName(permission: String): String {
        return when (permission) {
            Manifest.permission.CAMERA -> "Camera"
            Manifest.permission.RECORD_AUDIO -> "Microphone"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "Storage"
            Manifest.permission.READ_MEDIA_IMAGES -> "Photos"
            Manifest.permission.READ_MEDIA_AUDIO -> "Audio Files"
            Manifest.permission.ACCESS_COARSE_LOCATION -> "Approximate Location"
            Manifest.permission.POST_NOTIFICATIONS -> "Notifications"
            else -> "Unknown Permission"
        }
    }
    
    /**
     * Get rationale explanation for permissions
     */
    fun getPermissionRationale(permission: String): String {
        return when (permission) {
            Manifest.permission.CAMERA -> "Camera access is needed to analyze images for misinformation."
            Manifest.permission.RECORD_AUDIO -> "Microphone access is needed to analyze audio for misinformation."
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES -> "Storage access is needed to analyze images from your gallery."
            Manifest.permission.READ_MEDIA_AUDIO -> "Access to audio files is needed to analyze them for misinformation."
            Manifest.permission.ACCESS_COARSE_LOCATION -> "Location is used to provide alerts about misinformation in your area."
            Manifest.permission.POST_NOTIFICATIONS -> "Notifications are used to alert you about important misinformation threats."
            else -> "This permission is required for app functionality."
        }
    }
}

/**
 * Composable function to request a single permission with explanation
 */
@Composable
fun RequestPermission(
    permission: String,
    rationaleMessage: String,
    permanentlyDeniedMessage: String,
    onPermissionResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val permissionManager = remember { PermissionManager(context) }
    var showRationale by remember { mutableStateOf(false) }
    var permanentlyDenied by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionResult(true)
        } else {
            val activity = context as? Activity
            if (activity != null && !permissionManager.shouldShowRationale(activity, permission)) {
                permanentlyDenied = true
            } else {
                showRationale = true
            }
            onPermissionResult(false)
        }
    }
    
    // Check if we should request permission
    LaunchedEffect(permission) {
        if (!permissionManager.hasPermission(permission)) {
            val activity = context as? Activity
            if (activity != null && permissionManager.shouldShowRationale(activity, permission)) {
                showRationale = true
            } else {
                permissionLauncher.launch(permission)
            }
        } else {
            onPermissionResult(true)
        }
    }
    
    // Show rationale dialog
    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Permission Required") },
            text = { Text(rationaleMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showRationale = false
                        permissionLauncher.launch(permission)
                    }
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        onPermissionResult(false)
                    }
                ) {
                    Text("Not Now")
                }
            }
        )
    }
    
    // Show permanently denied dialog
    if (permanentlyDenied) {
        AlertDialog(
            onDismissRequest = { permanentlyDenied = false },
            title = { Text("Permission Required") },
            text = { Text(permanentlyDeniedMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        permanentlyDenied = false
                        permissionManager.openAppSettings()
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        permanentlyDenied = false
                        onPermissionResult(false)
                    }
                ) {
                    Text("Not Now")
                }
            }
        )
    }
}

/**
 * Composable function to request multiple permissions with explanation
 */
@Composable
fun RequestMultiplePermissions(
    permissions: Array<String>,
    rationaleTitle: String,
    rationaleMessage: String,
    onAllPermissionsResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val permissionManager = remember { PermissionManager(context) }
    var showRationale by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    
    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        val allGranted = permissionsResult.all { it.value }
        if (allGranted) {
            onAllPermissionsResult(true)
        } else {
            val activity = context as? Activity
            val somePermissionsPermanentlyDenied = permissionsResult.any { 
                !it.value && activity != null && !permissionManager.shouldShowRationale(activity, it.key)
            }
            
            if (somePermissionsPermanentlyDenied) {
                showSettings = true
            } else {
                showRationale = true
            }
            onAllPermissionsResult(false)
        }
    }
    
    // Check if we should request permissions
    LaunchedEffect(permissions) {
        if (!permissionManager.hasPermissions(permissions)) {
            val activity = context as? Activity
            val shouldShowRationale = permissions.any { 
                activity != null && permissionManager.shouldShowRationale(activity, it)
            }
            
            if (shouldShowRationale) {
                showRationale = true
            } else {
                multiplePermissionsLauncher.launch(permissions)
            }
        } else {
            onAllPermissionsResult(true)
        }
    }
    
    // Show rationale dialog
    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text(rationaleTitle) },
            text = { Text(rationaleMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showRationale = false
                        multiplePermissionsLauncher.launch(permissions)
                    }
                ) {
                    Text("Grant Permissions")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        onAllPermissionsResult(false)
                    }
                ) {
                    Text("Not Now")
                }
            }
        )
    }
    
    // Show settings dialog
    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Permissions Required") },
            text = { 
                Text("Some permissions have been permanently denied. Please enable them in app settings to use this feature.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSettings = false
                        permissionManager.openAppSettings()
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSettings = false
                        onAllPermissionsResult(false)
                    }
                ) {
                    Text("Not Now")
                }
            }
        )
    }
}

/**
 * Composable for a permission explanation item with checkbox for user consent
 */
@Composable
fun PermissionExplanationItem(
    permissionName: String,
    explanation: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = permissionName,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
