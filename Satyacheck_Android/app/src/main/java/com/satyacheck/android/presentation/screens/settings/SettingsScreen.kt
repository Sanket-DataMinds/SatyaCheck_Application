package com.satyacheck.android.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.satyacheck.android.R
import com.satyacheck.android.presentation.components.AppHeader
import com.satyacheck.android.presentation.components.MainLayout
import com.satyacheck.android.presentation.components.openAppDrawer
import com.satyacheck.android.presentation.theme.ThemeMode
import com.satyacheck.android.presentation.theme.ThemeViewModel
import com.satyacheck.android.utils.LanguageManager

@Composable
fun SettingsScreen(
    onNavigateToAnalyze: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToEducate: () -> Unit,
    onNavigateToLanguageSettings: () -> Unit,
    onNavigateToMap: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val currentTheme by themeViewModel.themeMode.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    MainLayout(
        currentRoute = "settings",
        onNavigate = { route ->
            when (route) {
                "analyze" -> onNavigateToAnalyze()
                "dashboard" -> onNavigateToDashboard()
                "community" -> onNavigateToCommunity()
                "educate" -> onNavigateToEducate()
                "map" -> onNavigateToMap()
            }
        },
        snackbarHostState = snackbarHostState,
        drawerState = drawerState,
        topBar = {
            AppHeader(
                title = stringResource(id = R.string.nav_settings),
                showMenuButton = true,
                onMenuClicked = { openAppDrawer(drawerState, scope) }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Language settings card
            SettingsCard(
                title = stringResource(id = R.string.language_settings_title),
                subtitle = stringResource(id = R.string.language_settings_subtitle),
                icon = Icons.Default.Language,
                onClick = onNavigateToLanguageSettings
            )
            
            // Other settings options can be added here
            SettingsCard(
                title = "Account",
                subtitle = "Manage your profile and account settings",
                icon = Icons.Default.AccountCircle,
                onClick = { /* Navigate to account settings */ }
            )
            
            SettingsCard(
                title = "Notifications",
                subtitle = "Configure alerts and notification preferences",
                icon = Icons.Default.Notifications,
                onClick = { /* Navigate to notification settings */ }
            )
            
            SettingsCard(
                title = "Privacy & Security",
                subtitle = "Manage your data and security settings",
                icon = Icons.Default.Shield,
                onClick = { /* Navigate to privacy settings */ }
            )
            
            // Theme settings
            ThemeSettingsCard(
                currentTheme = currentTheme,
                onThemeSelected = { newTheme -> themeViewModel.setThemeMode(newTheme) }
            )
            
            // Add spacer at the bottom to prevent content from being hidden behind the navigation bar
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun ThemeSettingsCard(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DarkMode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = "Change the app appearance (${currentTheme.name.lowercase().capitalize()})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select Theme",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            
            // Dropdown menu for theme selection
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                // Light theme option
                DropdownMenuItem(
                    text = { Text("Light") },
                    onClick = {
                        onThemeSelected(ThemeMode.LIGHT)
                        expanded = false
                    },
                    leadingIcon = {
                        if (currentTheme == ThemeMode.LIGHT) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
                
                // Dark theme option
                DropdownMenuItem(
                    text = { Text("Dark") },
                    onClick = {
                        onThemeSelected(ThemeMode.DARK)
                        expanded = false
                    },
                    leadingIcon = {
                        if (currentTheme == ThemeMode.DARK) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
                
                // System default option
                DropdownMenuItem(
                    text = { Text("System Default") },
                    onClick = {
                        onThemeSelected(ThemeMode.SYSTEM)
                        expanded = false
                    },
                    leadingIcon = {
                        if (currentTheme == ThemeMode.SYSTEM) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        }
    }
}

// Extension function to capitalize first letter of a string
private fun String.capitalize(): String {
    return this.replaceFirstChar { 
        if (it.isLowerCase()) it.titlecase() else it.toString() 
    }
}
