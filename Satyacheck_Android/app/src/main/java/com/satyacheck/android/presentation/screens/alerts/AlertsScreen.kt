package com.satyacheck.android.presentation.screens.alerts

import android.text.format.DateUtils
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.satyacheck.android.domain.model.Alert
import com.satyacheck.android.domain.model.AlertType
import com.satyacheck.android.presentation.components.AppHeader
import com.satyacheck.android.presentation.components.MainLayout
import com.satyacheck.android.presentation.components.openAppDrawer
import java.util.Date

@Composable
fun AlertsScreen(
    onNavigateToAnalyze: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToEducate: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val alerts by viewModel.alerts.collectAsState()
    
    MainLayout(
        currentRoute = "alerts",
        onNavigate = { route ->
            when (route) {
                "analyze" -> onNavigateToAnalyze()
                "dashboard" -> onNavigateToDashboard()
                "community" -> onNavigateToCommunity()
                "map" -> onNavigateToMap()
                "educate" -> onNavigateToEducate()
                "settings" -> onNavigateToSettings()
            }
        },
        snackbarHostState = snackbarHostState,
        drawerState = drawerState,
        topBar = {
            AppHeader(
                title = "Alerts",
                showMenuButton = true,
                onMenuClicked = { openAppDrawer(drawerState, scope) }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (alerts.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No alerts at this time",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "You'll be notified when new alerts are available",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // List of alerts
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(alerts) { alert ->
                        AlertItem(
                            alert = alert,
                            onClick = { viewModel.markAlertAsRead(alert.id) }
                        )
                    }
                    
                    // Add bottom padding
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun AlertItem(
    alert: Alert,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Icon(
                imageVector = alert.icon.getIcon(),
                contentDescription = null,
                tint = if (alert.type.isHighPriority()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = alert.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Metadata
            Column(
                horizontalAlignment = Alignment.End
            ) {
                AlertBadge(type = alert.type)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = getRelativeTimeSpanString(alert.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AlertBadge(type: AlertType) {
    Box(
        modifier = Modifier
            .background(
                color = when {
                    type.isHighPriority() -> MaterialTheme.colorScheme.errorContainer
                    type == AlertType.MISLEADING -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.secondaryContainer
                },
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = type.getDisplayName(),
            style = MaterialTheme.typography.labelSmall,
            color = when {
                type.isHighPriority() -> MaterialTheme.colorScheme.onErrorContainer
                type == AlertType.MISLEADING -> MaterialTheme.colorScheme.onTertiaryContainer
                else -> MaterialTheme.colorScheme.onSecondaryContainer
            }
        )
    }
}

// Helper function to get relative time string (e.g., "2 hours ago")
private fun getRelativeTimeSpanString(date: Date): String {
    return DateUtils.getRelativeTimeSpanString(
        date.time,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
}
