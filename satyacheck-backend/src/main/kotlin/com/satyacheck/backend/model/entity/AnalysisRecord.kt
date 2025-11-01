package com.satyacheck.backend.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * Entity for recording analyzed content for trend analysis
 */
@Document(collection = "analysis_records")
data class AnalysisRecord(
    @Id
    val id: String? = null,
    
    // Content hash (used for deduplication)
    val contentHash: String,
    
    // Small sample of the content (for reference)
    val contentSample: String,
    
    // Original content length
    val contentLength: Int,
    
    // Source of the content (URL, user input, etc.)
    val contentSource: String? = null,
    
    // Content type (news, social media, etc.)
    val contentType: String? = null,
    
    // Content language
    val language: String,
    
    // Was this identified as misinformation
    val isMisinformation: Boolean,
    
    // Confidence score of the analysis (0.0 - 1.0)
    val confidenceScore: Double,
    
    // Misinformation risk level (if applicable)
    val riskLevel: String? = null,
    
    // Categories assigned to the content
    val categories: List<String> = emptyList(),
    
    // Misinformation techniques detected
    val misinformationTechniques: List<String> = emptyList(),
    
    // Keywords extracted from the content
    val keywords: List<String> = emptyList(),
    
    // Topics extracted from the content
    val topics: List<String> = emptyList(),
    
    // Sentiment score (-1.0 to 1.0)
    val sentimentScore: Double? = null,
    
    // User geographic region (if available)
    val userRegion: String? = null,
    
    // Device type used for analysis request
    val deviceType: String? = null,
    
    // Time of analysis
    val analyzedAt: Instant = Instant.now(),
    
    // Has this record been processed for trends
    val processedForTrends: Boolean = false,
    
    // When this record was last processed for trends
    val trendProcessedAt: Instant? = null
)