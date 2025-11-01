package com.satyacheck.backend.service

import com.satyacheck.backend.model.dto.AnalysisRequest
import com.satyacheck.backend.model.dto.AnalysisResult

interface AnalysisService {
    suspend fun analyzeText(request: AnalysisRequest): AnalysisResult
}