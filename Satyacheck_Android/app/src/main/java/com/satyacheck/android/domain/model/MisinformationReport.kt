package com.satyacheck.android.domain.model

import java.util.UUID

/**
 * Data model for a misinformation report
 */
data class MisinformationReport(
    val id: String = UUID.randomUUID().toString(),
    val contentLink: String = "",
    val description: String = "",
    val category: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = "" // Would be populated with the actual user ID in a real app
)
