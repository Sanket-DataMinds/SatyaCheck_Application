package com.satyacheck.android.presentation.screens.analysis

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satyacheck.android.data.repository.AnalysisRepository
import com.satyacheck.android.domain.model.AnalysisResult
import com.satyacheck.android.domain.model.Result
import com.satyacheck.android.domain.model.Verdict
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalysisResultViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analysisRepository: AnalysisRepository
) : ViewModel() {
    
    var state by mutableStateOf(AnalysisResultState())
        private set
    
    fun loadAnalysisResult() {
        state = state.copy(isLoading = true)
        
        try {
            // First load from local preferences
            val prefs = context.getSharedPreferences("satya_check_prefs", Context.MODE_PRIVATE)
            
            val verdictString = prefs.getString("verdict", null)
            val explanation = prefs.getString("explanation", null)
            val originalContent = prefs.getString("original_content", null)
            val contentType = prefs.getString("content_type", null)
            
            if (verdictString != null && explanation != null) {
                val verdict = try {
                    Verdict.valueOf(verdictString)
                } catch (e: IllegalArgumentException) {
                    Verdict.CREDIBLE
                }
                
                val result = AnalysisResult(
                    verdict = verdict,
                    explanation = explanation
                )
                
                state = state.copy(
                    isLoading = false,
                    analysisResult = result,
                    originalContent = originalContent,
                    contentType = contentType
                )
                
                // Also fetch analysis history from the server in the background
                fetchAnalysisHistory()
            } else {
                state = state.copy(
                    isLoading = false,
                    error = "No analysis result found. Please try analyzing some content first."
                )
            }
        } catch (e: Exception) {
            state = state.copy(
                isLoading = false,
                error = e.message ?: "An error occurred while loading the analysis result."
            )
        }
    }
    
    private fun fetchAnalysisHistory() {
        viewModelScope.launch {
            try {
                analysisRepository.getAnalysisHistory(0, 1).collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            result.data?.let { analyses ->
                                if (analyses.isNotEmpty()) {
                                    // Update with the most recent analysis from the server
                                    state = state.copy(
                                        recentAnalyses = analyses
                                    )
                                }
                            }
                        }
                        is Result.Error -> {
                            // Just log error, don't update UI as we already have local results
                            println("Error fetching analysis history: ${result.message}")
                        }
                        is Result.Loading -> {
                            // Don't show loading state as we already have local results
                        }
                    }
                }
            } catch (e: Exception) {
                // Just log the error, don't update UI
                println("Exception fetching analysis history: ${e.message}")
            }
        }
    }
    
    fun shareAnalysisResult() {
        // TODO: Implement sharing functionality
    }
}
