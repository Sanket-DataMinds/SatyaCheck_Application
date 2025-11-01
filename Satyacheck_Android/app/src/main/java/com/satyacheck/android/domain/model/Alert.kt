package com.satyacheck.android.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Date

/**
 * Data model for Alert notifications
 */
data class Alert(
    val id: Int,
    val title: String,
    val description: String,
    val type: AlertType,
    val icon: AlertIcon,
    val date: Date
)

/**
 * Alert types for categorization and styling
 */
enum class AlertType {
    HIGH_RISK,
    SCAM_ALERT,
    MISLEADING,
    INFORMATION;

    fun getDisplayName(): String {
        return when (this) {
            HIGH_RISK -> "High Risk"
            SCAM_ALERT -> "Scam Alert"
            MISLEADING -> "Misleading"
            INFORMATION -> "Information"
        }
    }

    fun isHighPriority(): Boolean {
        return this == HIGH_RISK || this == SCAM_ALERT
    }
}

/**
 * Alert icons for visual representation
 */
enum class AlertIcon {
    SHIELD_ALERT,
    WARNING,
    INFO;

    fun getIcon(): ImageVector {
        return when (this) {
            SHIELD_ALERT -> Icons.Default.Shield
            WARNING -> Icons.Default.Warning
            INFO -> Icons.Default.Info
        }
    }
}
