package com.satyacheck.android.domain.model

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val language: String,
    val totalAnalyses: Int = 0,
    val misinformationDetected: Int = 0
)
