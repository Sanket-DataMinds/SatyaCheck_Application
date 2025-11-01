package com.satyacheck.backend.model.dto

import com.satyacheck.backend.model.enum.Verdict

data class AnalysisResult(
    val verdict: Verdict,
    val explanation: String
)