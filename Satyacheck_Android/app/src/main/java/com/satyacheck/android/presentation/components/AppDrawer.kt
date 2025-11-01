package com.satyacheck.android.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.satyacheck.android.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Defines an item in the navigation drawer
 */
data class DrawerItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

/**
 * A navigation drawer component that displays a list of navigation options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    drawerState: DrawerState,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showExitDialog by remember { mutableStateOf(false) }
    
    // Exit confirmation dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Application") },
            text = { Text("Are you sure you want to exit the app?") },
            confirmButton = {
                Button(
                    onClick = {
                        // Safely exit the app
                        android.os.Process.killProcess(android.os.Process.myPid())
                    }
                ) {
                    Text("Yes, Exit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    val drawerItems = listOf(
        DrawerItem(
            title = stringResource(id = R.string.nav_dashboard),
            route = "dashboard",
            icon = Icons.Default.Home
        ),
        DrawerItem(
            title = stringResource(id = R.string.nav_settings),
            route = "settings",
            icon = Icons.Default.Settings
        ),
        DrawerItem(
            title = "Report Misinformation",
            route = "report",
            icon = Icons.Default.Report
        )
    )
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerHeader()
                Spacer(modifier = Modifier.height(8.dp))
                
                // Draw each drawer item
                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { 
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text(text = item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                            }
                            onNavigate(item.route)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                // Exit app button at the bottom
                NavigationDrawerItem(
                    icon = { 
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Exit App",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(text = "Exit App") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }
                        showExitDialog = true
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                // App version at the bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SatyaCheck v1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        content = content
    )
}

@Composable
fun DrawerHeader() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "SatyaCheck",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Fighting misinformation together",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Helper function to close the drawer
 */
fun closeDrawer(scope: CoroutineScope, drawerState: DrawerState) {
    scope.launch {
        drawerState.close()
    }
}

/**
 * Helper function to open the drawer
 */
fun openDrawer(scope: CoroutineScope, drawerState: DrawerState) {
    scope.launch {
        drawerState.open()
    }
}
