package com.satyacheck.backend.model.dto

/**
 * Data class for content categorization result
 */
data class ContentCategory(
    val primaryCategory: String,
    val subCategories: List<String>,
    val confidence: Double,
    val tags: List<String>
)