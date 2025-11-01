package com.satyacheck.android.presentation.screens.report

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Report Misinformation screen.
 * Handles the form state and submission logic.
 */
@HiltViewModel
class ReportMisinformationViewModel @Inject constructor(
    // In a real app, we would inject repositories here
    // private val reportRepository: ReportRepository
) : ViewModel() {

    // UI state
    private val _state = MutableStateFlow(ReportMisinformationState())
    val state: StateFlow<ReportMisinformationState> = _state.asStateFlow()
    
    // Snackbar for showing messages
    val snackbarHostState = SnackbarHostState()
    
    // Categories for the dropdown menu
    val categories = listOf(
        "Health Misinformation",
        "Financial Scam",
        "Political Fake News",
        "Social Media Rumor",
        "Other"
    )
    
    /**
     * Update the content link in the form
     */
    fun updateContentLink(link: String) {
        _state.update { it.copy(contentLink = link) }
    }
    
    /**
     * Update the description in the form
     */
    fun updateDescription(description: String) {
        _state.update { it.copy(description = description) }
    }
    
    /**
     * Update the selected category in the form
     */
    fun updateCategory(category: String) {
        _state.update { it.copy(category = category) }
    }
    
    /**
     * Submit the report
     */
    fun submitReport() {
        val currentState = _state.value
        
        // Basic validation
        if (currentState.description.isBlank()) {
            showError("Please provide a description of the misinformation")
            return
        }
        
        if (currentState.category.isBlank()) {
            showError("Please select a category")
            return
        }
        
        // Set loading state
        _state.update { it.copy(isSubmitting = true) }
        
        // In a real app, we would send this to a repository
        viewModelScope.launch {
            try {
                // Simulate API call
                kotlinx.coroutines.delay(1000)
                
                // In a real app, we would call repository here
                // reportRepository.submitReport(
                //     contentLink = currentState.contentLink,
                //     description = currentState.description,
                //     category = currentState.category
                // )
                
                // Show success message
                snackbarHostState.showSnackbar(
                    message = "Report submitted successfully",
                    duration = SnackbarDuration.Short
                )
                
                // Reset form
                _state.update {
                    ReportMisinformationState(isSubmissionSuccessful = true)
                }
            } catch (e: Exception) {
                // Show error message
                showError("Failed to submit report: ${e.message}")
            } finally {
                // Reset loading state
                _state.update { it.copy(isSubmitting = false) }
            }
        }
    }
    
    /**
     * Show an error message in the snackbar
     */
    private fun showError(message: String) {
        viewModelScope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }
    
    /**
     * State for the Report Misinformation screen
     */
    data class ReportMisinformationState(
        val contentLink: String = "",
        val description: String = "",
        val category: String = "",
        val isSubmitting: Boolean = false,
        val isSubmissionSuccessful: Boolean = false
    )
}
