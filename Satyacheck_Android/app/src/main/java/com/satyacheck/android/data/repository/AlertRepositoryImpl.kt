package com.satyacheck.android.data.repository

import com.satyacheck.android.domain.model.Alert
import com.satyacheck.android.domain.model.AlertIcon
import com.satyacheck.android.domain.model.AlertType
import com.satyacheck.android.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepositoryImpl @Inject constructor() : AlertRepository {
    
    // Mock data for alerts - would be replaced with real API in production
    private val mockAlerts = listOf(
        Alert(
            id = 1,
            title = "New Phishing Scam Alert",
            description = "Beware of fake bank SMS messages asking for personal information or requesting to click on suspicious links.",
            type = AlertType.HIGH_RISK,
            icon = AlertIcon.SHIELD_ALERT,
            date = getDateBefore(hours = 2)
        ),
        Alert(
            id = 2,
            title = "Misleading Information Detected",
            description = "Recent social media posts about health cures contain unverified claims. Check verified sources for accurate health information.",
            type = AlertType.MISLEADING,
            icon = AlertIcon.WARNING,
            date = getDateBefore(days = 1)
        ),
        Alert(
            id = 3,
            title = "Fake UPI Payment Requests",
            description = "Scammers are sending fake UPI payment requests. Always verify the recipient before confirming any payment.",
            type = AlertType.SCAM_ALERT,
            icon = AlertIcon.SHIELD_ALERT,
            date = getDateBefore(days = 3)
        ),
        Alert(
            id = 4,
            title = "Digital Safety Workshop",
            description = "Join our online workshop on digital safety practices. Learn to protect yourself and your family online.",
            type = AlertType.INFORMATION,
            icon = AlertIcon.INFO,
            date = getDateBefore(days = 5)
        ),
        Alert(
            id = 5,
            title = "QR Code Scam Warning",
            description = "Be cautious when scanning QR codes from unknown sources. Scammers are using them to steal personal information.",
            type = AlertType.HIGH_RISK,
            icon = AlertIcon.SHIELD_ALERT,
            date = getDateBefore(days = 7)
        )
    )
    
    // Track read status of alerts
    private val readAlertIds = MutableStateFlow<Set<Int>>(emptySet())
    
    // All alerts
    private val _alerts = MutableStateFlow(mockAlerts)
    
    override fun getAlerts(): Flow<List<Alert>> = _alerts
    
    override fun getUnreadAlertsCount(): Flow<Int> = readAlertIds.map { readIds ->
        _alerts.value.count { it.id !in readIds }
    }
    
    override suspend fun markAlertAsRead(alertId: Int) {
        readAlertIds.value = readAlertIds.value + alertId
    }
    
    override suspend fun markAllAlertsAsRead() {
        readAlertIds.value = _alerts.value.map { it.id }.toSet()
    }
    
    // Helper function to generate dates relative to current time
    private fun getDateBefore(days: Int = 0, hours: Int = 0): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        calendar.add(Calendar.HOUR_OF_DAY, -hours)
        return calendar.time
    }
}
