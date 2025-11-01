package com.satyacheck.android.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.satyacheck.android.components.ui.ThemeSelector
import com.satyacheck.android.presentation.theme.ThemeMode
import com.satyacheck.android.utils.LegalDocumentScreen
import com.satyacheck.android.utils.PermissionManager

/**
 * Settings screen for the app where users can manage permissions and view legal documents
 */
@Composable
fun SettingsScreen(
    @Suppress("UNUSED_PARAMETER") navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showTermsOfService by remember { mutableStateOf(false) }
    var showThemeSelector by remember { mutableStateOf(false) }
    
    if (showPrivacyPolicy) {
        LegalDocumentScreen(
            title = "Privacy Policy",
            assetFileName = "privacy_policy.md",
            requireConsent = false,
            onBackPressed = {
                showPrivacyPolicy = false
            }
        )
    } else if (showTermsOfService) {
        LegalDocumentScreen(
            title = "Terms of Service",
            assetFileName = "terms_of_service.md",
            requireConsent = false,
            onBackPressed = {
                showTermsOfService = false
            }
        )
    } else if (showThemeSelector) {
        Dialog(onDismissRequest = { showThemeSelector = false }) {
            ThemeSelector(
                currentTheme = uiState.themeMode,
                onThemeSelected = { themeMode ->
                    viewModel.setThemeMode(themeMode)
                    showThemeSelector = false
                },
                modifier = Modifier.padding(16.dp)
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Settings content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Permissions section
                SettingsSectionHeader(title = "Permissions")
                
                // Camera permission
                PermissionSettingItem(
                    title = "Camera",
                    description = "Access your camera to analyze images for misinformation",
                    icon = Icons.Default.PhotoCamera,
                    isGranted = uiState.cameraPermissionGranted,
                    onRequestPermission = {
                        viewModel.requestPermission(android.Manifest.permission.CAMERA)
                    }
                )
                
                // Microphone permission
                PermissionSettingItem(
                    title = "Microphone",
                    description = "Access your microphone to analyze audio for misinformation",
                    icon = Icons.Default.Mic,
                    isGranted = uiState.microphonePermissionGranted,
                    onRequestPermission = {
                        viewModel.requestPermission(android.Manifest.permission.RECORD_AUDIO)
                    }
                )
                
                // Notification permission
                PermissionSettingItem(
                    title = "Notifications",
                    description = "Receive notifications about analysis results and important updates",
                    icon = Icons.Default.Notifications,
                    isGranted = uiState.notificationPermissionGranted,
                    onRequestPermission = {
                        viewModel.requestPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // App settings section
                SettingsSectionHeader(title = "App Settings")
                
                // Language setting
                SettingItem(
                    title = "Language",
                    description = "Change the app's language",
                    icon = Icons.Default.Language,
                    onClick = {
                        // Navigate to language selection
                    }
                )
                
                // Theme setting
                SettingItem(
                    title = "Theme",
                    description = "Change the app's appearance (Light, Dark, Follow System)",
                    icon = Icons.Default.DarkMode,
                    onClick = {
                        // Show theme selector dialog
                        showThemeSelector = true
                    }
                )
                
                // Notification settings
                SettingItem(
                    title = "Notification Settings",
                    description = "Manage notification preferences",
                    icon = Icons.Default.NotificationsActive,
                    onClick = {
                        // Navigate to notification settings
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Legal section
                SettingsSectionHeader(title = "Legal")
                
                // Privacy Policy
                SettingItem(
                    title = "Privacy Policy",
                    description = "View our privacy policy",
                    icon = Icons.Default.Shield,
                    onClick = {
                        showPrivacyPolicy = true
                    }
                )
                
                // Terms of Service
                SettingItem(
                    title = "Terms of Service",
                    description = "View our terms of service",
                    icon = Icons.Default.Assignment,
                    onClick = {
                        showTermsOfService = true
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // About section
                SettingsSectionHeader(title = "About")
                
                // App version
                SettingItem(
                    title = "Version",
                    description = "1.0.0",
                    icon = Icons.Default.Info,
                    onClick = { }
                )
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PermissionSettingItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isGranted: Boolean,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Permission granted",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Button(
                    onClick = onRequestPermission,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Grant")
                }
            }
        }
    }
}
