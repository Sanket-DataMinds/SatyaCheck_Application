package com.satyacheck.backend.model.dto

data class AnalysisRequest(
    val content: String,
    val contentType: String = "TEXT",
    val language: String = "en"
)