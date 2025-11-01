package com.satyacheck.backend.controller

import com.satyacheck.backend.model.dto.AnalysisRequest
import com.satyacheck.backend.model.dto.AnalysisResult
import com.satyacheck.backend.model.dto.ApiResponse
import com.satyacheck.backend.service.AnalysisService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.logging.Logger

@RestController
@RequestMapping("/api/analyze")
class AnalysisController(private val analysisService: AnalysisService) {
    private val logger = Logger.getLogger(AnalysisController::class.java.name)

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    suspend fun analyzeContent(
        @RequestBody request: AnalysisRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        logger.info("Analysis requested by user: ${userDetails.username}")
        
        val result = analysisService.analyzeText(request)

        // Format the response to match what the Android app expects
        val response = mapOf(
            "verdict" to result.verdict.toString(),
            "explanation" to result.explanation
        )

        return ResponseEntity.ok(ApiResponse.success(response, "Content analyzed successfully"))
    }
}
