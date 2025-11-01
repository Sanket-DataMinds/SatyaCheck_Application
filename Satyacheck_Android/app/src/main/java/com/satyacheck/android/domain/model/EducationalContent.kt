package com.satyacheck.android.domain.model

data class EducationalContent(
    val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val category: String,
    val imageUrl: String?,
    val createdAt: String
)
