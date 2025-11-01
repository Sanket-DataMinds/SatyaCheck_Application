package com.satyacheck.backend.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * Entity representing user feedback on analysis results
 */
@Document(collection = "feedback")
data class AnalysisFeedback(
    @Id
    val id: String? = null,
    
    // Who provided the feedback (optional, can be anonymous)
    val userId: String? = null,
    
    // The analysis ID that was evaluated
    val analysisId: String,
    
    // Type of analysis that received feedback
    val analysisType: String,
    
    // Content that was analyzed (can be text or URL)
    val analyzedContent: String,
    
    // Language of the analyzed content
    val contentLanguage: String,
    
    // User rating (1-5 scale)
    val rating: Int,
    
    // Was the analysis correct according to the user?
    val wasCorrect: Boolean? = null,
    
    // User's evaluation of the analysis
    val userEvaluation: String? = null,
    
    // Optional reason codes for the feedback
    val reasonCodes: List<String> = emptyList(),
    
    // Optional user comment
    val comment: String? = null,
    
    // Device information
    val deviceInfo: Map<String, Any> = emptyMap(),
    
    // Timestamps
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)