package com.satyacheck.android.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A notification bell icon with an optional badge indicating unread notifications.
 * Uses non-experimental APIs for stability.
 */
@Composable
fun NotificationBellWithBadge(
    unreadCount: Int = 0,
    onClick: () -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications"
            )
        }
        
        if (unreadCount > 0) {
            Surface(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp),
                color = MaterialTheme.colorScheme.error,
                shape = CircleShape
            ) {
                Text(
                    text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                    color = MaterialTheme.colorScheme.onError,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }
    }
}
