package com.satyacheck.android.domain.analyzer

import android.graphics.Bitmap
import android.util.Log
import com.satyacheck.android.domain.model.AnalysisResult
import com.satyacheck.android.domain.model.Verdict
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageAnalyzer @Inject constructor(
    private val visionTextExtractor: VisionTextExtractor,
    private val optimizedGeminiTextAnalyzer: OptimizedGeminiTextAnalyzer
) {
    
    companion object {
        private const val TAG = "ImageAnalyzer"
    }
    
    /**
     * Analyze image for misinformation by extracting text and analyzing it
     */
    suspend fun analyzeImage(bitmap: Bitmap): AnalysisResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting image analysis...")
            
            // Step 1: Extract text from image using ML Kit (offline)
            val extractedText = visionTextExtractor.extractTextFromImage(bitmap)
            
            if (extractedText.isBlank()) {
                Log.w(TAG, "No text extracted from image")
                return@withContext AnalysisResult(
                    verdict = Verdict.CREDIBLE,
                    explanation = "No text found in image to analyze for misinformation."
                )
            }
            
            Log.d(TAG, "Text extracted successfully, length: ${extractedText.length}")
            Log.d(TAG, "Extracted text preview: ${extractedText.take(100)}...")
            
            // Step 2: Analyze extracted text for misinformation using our optimized analyzer
            val textAnalysisResult = optimizedGeminiTextAnalyzer.analyzeText(extractedText)
            
            // Step 3: Enhanced result with image context
            val enhancedExplanation = "Image Analysis: ${textAnalysisResult.explanation}\n\nExtracted Text: \"${extractedText.take(200)}${if (extractedText.length > 200) "..." else ""}\""
            
            val finalResult = AnalysisResult(
                verdict = textAnalysisResult.verdict,
                explanation = enhancedExplanation
            )
            
            Log.d(TAG, "Image analysis completed with verdict: ${finalResult.verdict}")
            return@withContext finalResult
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during image analysis: ${e.message}", e)
            
            // Return fallback result
            return@withContext AnalysisResult(
                verdict = Verdict.UNKNOWN,
                explanation = "Unable to analyze image due to technical issues. Please try again or analyze the text manually."
            )
        }
    }
    
    /**
     * Quick image analysis without full text extraction (for preview)
     */
    suspend fun quickImageCheck(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        try {
            val extractedText = visionTextExtractor.extractTextFromImage(bitmap)
            
            return@withContext if (extractedText.isBlank()) {
                "No text detected in image"
            } else {
                "Text detected: ${extractedText.take(100)}${if (extractedText.length > 100) "..." else ""}"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in quick image check: ${e.message}")
            return@withContext "Unable to process image"
        }
    }
}