package com.satyacheck.backend.service

import com.satyacheck.backend.model.dto.AnalysisRequest
import com.satyacheck.backend.model.dto.AnalysisResult
import com.satyacheck.backend.model.dto.ContentCategory
import com.satyacheck.backend.model.dto.EnhancedAnalysisResult
import com.satyacheck.backend.model.dto.ExtractedTopic

/**
 * Enhanced interface for text analysis service with additional advanced features
 */
interface EnhancedAnalysisService {
    /**
     * Performs a basic text analysis
     */
    suspend fun analyzeText(request: AnalysisRequest): AnalysisResult
    
    /**
     * Performs enhanced text analysis with sentiment, entities, and more
     */
    suspend fun analyzeTextEnhanced(request: AnalysisRequest): EnhancedAnalysisResult
    
    /**
     * Categorizes content into topics
     */
    suspend fun categorizeContent(content: String, language: String = "en"): ContentCategory
    
    /**
     * Extracts main topics from content
     */
    suspend fun extractTopics(content: String, language: String = "en"): List<ExtractedTopic>
}