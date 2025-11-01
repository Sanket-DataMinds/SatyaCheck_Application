package com.satyacheck.backend.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.time.LocalDate

/**
 * Entity for tracking misinformation trends over time
 */
@Document(collection = "misinformation_trends")
data class MisinformationTrend(
    @Id
    val id: String? = null,
    
    // Date of this trend record
    val date: LocalDate,
    
    // Time period this trend covers (DAILY, WEEKLY, MONTHLY)
    val period: String,
    
    // Type of content (NEWS, SOCIAL_MEDIA, etc.)
    val contentType: String,
    
    // Language of content
    val language: String,
    
    // Total content items analyzed in this period
    val totalAnalyzed: Int,
    
    // Count of content identified as misinformation
    val misinformationCount: Int,
    
    // Misinformation rate (0.0 - 1.0)
    val misinformationRate: Double,
    
    // Top misinformation categories
    val topCategories: Map<String, Int>,
    
    // Top misinformation techniques detected
    val topTechniques: Map<String, Int>,
    
    // Common keywords associated with misinformation
    val commonKeywords: List<String>,
    
    // Sentiment analysis of misinformation content
    val sentimentAnalysis: Map<String, Double>,
    
    // Geographic regions most affected
    val regions: Map<String, Int>,
    
    // Reliability score of this trend data (0.0 - 1.0)
    val reliabilityScore: Double,
    
    // Record metadata
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)