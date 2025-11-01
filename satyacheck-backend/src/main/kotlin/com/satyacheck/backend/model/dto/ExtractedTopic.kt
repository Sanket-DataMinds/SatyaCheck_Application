package com.satyacheck.backend.model.dto

/**
 * Data class for topic extraction result
 */
data class ExtractedTopic(
    val topic: String,
    val relevance: Double,
    val subtopics: List<String> = emptyList(),
    val keywords: List<String> = emptyList()
)