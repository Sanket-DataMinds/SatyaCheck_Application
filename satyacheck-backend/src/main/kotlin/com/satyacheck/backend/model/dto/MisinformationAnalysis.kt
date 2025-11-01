package com.satyacheck.backend.model.dto

/**
 * DTO representing misinformation analysis results
 */
data class MisinformationAnalysis(
    val isLikelyMisinformation: Boolean,
    val confidenceScore: Double,
    val riskLevel: String? = null,
    val patterns: List<String>? = null,
    val techniques: List<String>? = null,
    val explanation: String? = null
)