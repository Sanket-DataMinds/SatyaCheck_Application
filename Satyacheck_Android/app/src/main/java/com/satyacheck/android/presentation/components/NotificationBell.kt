package com.satyacheck.android.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBell(
    unreadCount: Int,
    onBellClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(8.dp)
    ) {
        if (unreadCount > 0) {
            // Icon with badge
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ) {
                        // Show count if less than 10, otherwise show "9+"
                        val displayCount = if (unreadCount < 10) unreadCount.toString() else "9+"
                        Text(
                            text = displayCount,
                            fontSize = 10.sp
                        )
                    }
                },
                modifier = Modifier.clickable(onClick = onBellClick)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            // Icon without badge
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onBellClick),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
