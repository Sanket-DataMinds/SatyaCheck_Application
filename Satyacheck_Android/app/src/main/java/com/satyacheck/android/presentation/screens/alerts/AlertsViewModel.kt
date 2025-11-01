package com.satyacheck.android.presentation.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satyacheck.android.domain.model.Alert
import com.satyacheck.android.domain.repository.AlertRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val alertRepository: AlertRepository
) : ViewModel() {
    
    // Exposed state for the UI
    val alerts: StateFlow<List<Alert>> = alertRepository.getAlerts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val unreadAlertsCount: StateFlow<Int> = alertRepository.getUnreadAlertsCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    
    // Actions
    fun markAlertAsRead(alertId: Int) {
        viewModelScope.launch {
            alertRepository.markAlertAsRead(alertId)
        }
    }
    
    fun markAllAlertsAsRead() {
        viewModelScope.launch {
            alertRepository.markAllAlertsAsRead()
        }
    }
}
