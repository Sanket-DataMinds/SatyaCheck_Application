package com.satyacheck.backend.model.dto

/**
 * Data class representing the enhanced analysis result with additional information
 */
data class EnhancedAnalysisResult(
    val basic: AnalysisResult,
    val sentiment: Map<String, Any>? = null,
    val entities: List<Map<String, Any>>? = null,
    val category: ContentCategory? = null,
    val topics: List<ExtractedTopic>? = null
)