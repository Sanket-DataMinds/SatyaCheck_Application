package com.satyacheck.android.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.satyacheck.android.domain.network.NetworkType
import com.satyacheck.android.domain.network.NetworkQuality

/**
 * Network status indicator component
 */
@Composable
fun NetworkStatusIndicator(
    isOnline: Boolean,
    networkType: NetworkType = NetworkType.NONE,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isOnline) 
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
    else 
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
        
    val contentColor = if (isOnline)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onErrorContainer

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                contentDescription = if (isOnline) "Online" else "Offline",
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            
            Text(
                text = if (isOnline) "Online" else "Offline",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

/**
 * Cache status indicator
 */
@Composable
fun CacheStatusIndicator(
    fromCache: Boolean,
    cacheTimestamp: Long? = null,
    modifier: Modifier = Modifier
) {
    if (!fromCache) return
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Cached,
                contentDescription = "From Cache",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(16.dp)
            )
            
            Text(
                text = "Cached",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

/**
 * Offline mode banner
 */
@Composable
fun OfflineModeBanner(
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Offline Mode",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Offline Mode",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Showing cached results. Connect to internet for new analysis.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            if (onDismiss != null) {
                TextButton(onClick = onDismiss) {
                    Text("Dismiss", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Analysis result with smart indicators
 */
@Composable
fun SmartAnalysisResultCard(
    result: com.satyacheck.android.domain.analyzer.SmartAnalysisResult,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Status indicators row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (result.fromCache) {
                    CacheStatusIndicator(
                        fromCache = true,
                        cacheTimestamp = result.cacheTimestamp
                    )
                }
                
                if (result.networkRequired) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = "Network Required",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Network Required",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            
            // Error message if any
            result.error?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(8.dp)
                )
            }
        }
    }
}