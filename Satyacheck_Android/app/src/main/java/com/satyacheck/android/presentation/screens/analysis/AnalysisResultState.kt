package com.satyacheck.android.presentation.screens.analysis

import com.satyacheck.android.domain.model.AnalysisResult

data class AnalysisResultState(
    val isLoading: Boolean = false,
    val analysisResult: AnalysisResult? = null,
    val originalContent: String? = null,
    val contentType: String? = null,
    val recentAnalyses: List<AnalysisResult> = emptyList(),
    val error: String? = null
)
