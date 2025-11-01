package com.satyacheck.android.utils

import com.satyacheck.android.domain.model.AnalysisResult

interface TextAnalyzer {
    
    /**
     * Analyzes the provided text for misinformation
     *
     * @param text The text to analyze
     * @return AnalysisResult containing verdict and explanation in the default language
     */
    suspend fun analyzeText(text: String): AnalysisResult
    
    /**
     * Analyzes the provided text for misinformation in the specified language
     *
     * @param text The text to analyze
     * @param language The ISO language code (e.g., "en", "hi", "mr")
     * @return AnalysisResult containing verdict and explanation in the specified language
     */
    suspend fun analyzeText(text: String, language: String): AnalysisResult
}
