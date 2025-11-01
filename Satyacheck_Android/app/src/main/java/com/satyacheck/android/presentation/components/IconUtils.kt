package com.satyacheck.android.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.People
import androidx.compose.ui.graphics.vector.ImageVector

fun getIconForNavItem(iconName: String): ImageVector {
    return when (iconName) {
        "analyze" -> Icons.Filled.Search
        "dashboard" -> Icons.Filled.Dashboard
        "community" -> Icons.Outlined.People
        "educate" -> Icons.Outlined.Book
        "settings" -> Icons.Filled.Settings
        "map" -> Icons.Filled.Map
        "alerts" -> Icons.Filled.Notifications
        else -> Icons.Filled.Search
    }
}
