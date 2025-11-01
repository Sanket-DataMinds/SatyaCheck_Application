package com.satyacheck.android.domain.repository

import com.satyacheck.android.domain.model.Alert
import kotlinx.coroutines.flow.Flow

interface AlertRepository {
    /**
     * Get all alerts as a flow
     */
    fun getAlerts(): Flow<List<Alert>>
    
    /**
     * Get count of unread alerts
     */
    fun getUnreadAlertsCount(): Flow<Int>
    
    /**
     * Mark an alert as read
     */
    suspend fun markAlertAsRead(alertId: Int)
    
    /**
     * Mark all alerts as read
     */
    suspend fun markAllAlertsAsRead()
}
