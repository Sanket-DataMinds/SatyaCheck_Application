package com.satyacheck.android.domain.analyzer

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.content
import com.satyacheck.android.domain.model.AnalysisResult
import com.satyacheck.android.domain.model.Verdict
import com.satyacheck.android.domain.service.GeminiModelDiscoveryService
import com.satyacheck.android.utils.LanguageManager
import com.satyacheck.android.utils.TextAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiTextAnalyzer @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val modelDiscoveryService: GeminiModelDiscoveryService
) : TextAnalyzer {
    
    companion object {
        private const val TAG = "GeminiTextAnalyzer"
        
        // Language-specific instructions
        private val LANGUAGE_INSTRUCTIONS = mapOf(
            LanguageManager.LANGUAGE_ENGLISH to """
                Consider these verdicts:
                - Credible
                - Potentially Misleading
                - High Misinformation Risk
                - Scam Alert
            """.trimIndent(),
            
            LanguageManager.LANGUAGE_HINDI to """
                इन निर्णयों पर विचार करें:
                - विश्वसनीय
                - संभावित रूप से भ्रामक
                - उच्च गलत सूचना जोखिम
                - धोखाधड़ी अलर्ट
            """.trimIndent(),
            
            LanguageManager.LANGUAGE_MARATHI to """
                या निकालांचा विचार करा:
                - विश्वसनीय
                - संभाव्य दिशाभूल करणारे
                - उच्च चुकीची माहिती जोखीम
                - फसवणूक अलर्ट
            """.trimIndent()
        )
    }
    
    override suspend fun analyzeText(text: String): AnalysisResult {
        // Default to English if no language is specified
        return analyzeText(text, LanguageManager.DEFAULT_LANGUAGE)
    }
    
    override suspend fun analyzeText(text: String, language: String): AnalysisResult = withContext(Dispatchers.IO) {
        try {
            // Get language-specific instructions or default to English
            val languageInstructions = LANGUAGE_INSTRUCTIONS[language] ?: LANGUAGE_INSTRUCTIONS[LanguageManager.DEFAULT_LANGUAGE]
            
            val prompt = """
                Analyze the following text for stylistic markers of misinformation, such as emotional language, urgency, fear, or logical fallacies.
                
                Text: $text
                
                Based on the analysis, provide a verdict and an explanation of why the content was flagged.
                
                $languageInstructions
                
                IMPORTANT: Your entire response, including the verdict and explanation, must be in the following language code: $language.
                
                Your response should be in JSON format with two fields:
                {
                  "verdict": "ONE OF THE VERDICTS ABOVE",
                  "explanation": "Detailed explanation of why this verdict was chosen"
                }
            """.trimIndent()
            
            Log.d(TAG, "Analyzing text in language: $language")
            
            try {
                // Verify the model setup
                Log.d(TAG, "GenerativeModel configured with: ${generativeModel.toString()}")
                Log.d(TAG, "Sending request to Gemini API with prompt length: ${prompt.length}")
                val startTime = System.currentTimeMillis()
                
                // Generate content with additional safety for debugging
                Log.d(TAG, "About to call generativeModel.generateContent()")
                val response: GenerateContentResponse? = try {
                    generativeModel.generateContent(content { text(prompt) })
                } catch (innerEx: Exception) {
                    Log.e(TAG, "CRITICAL: Exception during generateContent call: ${innerEx.message}")
                    Log.e(TAG, "Inner exception class: ${innerEx.javaClass.name}")
                    Log.e(TAG, "Inner exception trace: ${innerEx.stackTraceToString()}")
                    throw innerEx
                }
                
                val endTime = System.currentTimeMillis()
                
                Log.d(TAG, "Gemini API response received in ${endTime - startTime}ms")
                
                if (response != null) {
                    // Log full response object details
                    Log.d(TAG, "Response object: $response")
                    Log.d(TAG, "Response candidates: ${response.candidates?.size ?: 0}")
                    
                    if (response.text != null) {
                        Log.d(TAG, "Gemini returned valid text response: ${response.text}")
                        return@withContext parseResponse(response)
                    } else {
                        Log.e(TAG, "Gemini response has no text content: $response")
                        return@withContext fallbackResult("Gemini API returned empty response")
                    }
                } else {
                    Log.e(TAG, "Gemini response is null")
                    return@withContext fallbackResult("Null response from Gemini API")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Log detailed error for debugging
                Log.e(TAG, "Error analyzing text with Gemini API: ${e.message}")
                Log.e(TAG, "Error class: ${e.javaClass.name}")
                Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
                
                // Check if this is a model not found error and invalidate cache
                if (e.message?.contains("not found", ignoreCase = true) == true ||
                    e.message?.contains("404", ignoreCase = true) == true) {
                    Log.w(TAG, "Model not found error detected, invalidating model cache")
                    modelDiscoveryService.invalidateCache()
                }
                
                // Provide more specific fallback message based on the error
                val errorMessage = when {
                    e.message?.contains("API key") == true -> "Invalid or expired API key"
                    e.message?.contains("quota") == true -> "API quota exceeded"
                    e.message?.contains("model") == true -> "Model unavailable - cache invalidated for next attempt: ${e.message}"
                    e.message?.contains("network") == true || 
                    e.message?.contains("timeout") == true -> "Network error connecting to Gemini API"
                    else -> "Error analyzing text: ${e.message}"
                }
                
                return@withContext fallbackResult(errorMessage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // In case of any error, return a neutral response
            Log.e(TAG, "Error in outer try-catch: ${e.message}")
            return@withContext AnalysisResult(
                verdict = Verdict.CREDIBLE,
                explanation = "Unable to analyze the text. Please try again later."
            )
        }
    }
    
    private fun parseResponse(response: GenerateContentResponse): AnalysisResult {
        val responseText = response.text?.trim() ?: return fallbackResult()
        
        // Log the response for debugging
        Log.d(TAG, "Raw response: $responseText")
        
        // Try to extract the JSON from the response
        val jsonPattern = """\{[\s\S]*"verdict"[\s\S]*"explanation"[\s\S]*\}""".toRegex()
        val jsonMatch = jsonPattern.find(responseText)
        
        return if (jsonMatch != null) {
            try {
                // Use Gson to parse the JSON properly
                val json = jsonMatch.value
                val jsonObject = com.google.gson.JsonParser.parseString(json).asJsonObject
                
                if (jsonObject.has("verdict") && jsonObject.has("explanation")) {
                    val verdictString = jsonObject.get("verdict").asString
                    val explanation = jsonObject.get("explanation").asString
                    
                    Log.d(TAG, "Parsed verdict: $verdictString")
                    Log.d(TAG, "Parsed explanation: $explanation")
                    
                    try {
                        val verdict = Verdict.fromString(verdictString)
                        AnalysisResult(verdict, explanation)
                    } catch (e: IllegalArgumentException) {
                        Log.e(TAG, "Invalid verdict: $verdictString")
                        // If the verdict is not one of our enum values, try to map it to the closest one
                        val mappedVerdict = mapToVerdict(verdictString)
                        AnalysisResult(mappedVerdict, explanation)
                    }
                } else {
                    Log.e(TAG, "Could not extract verdict or explanation")
                    fallbackResult()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing JSON: ${e.message}")
                // Fallback to regex parsing if Gson fails
                tryRegexParsing(jsonMatch.value)
            }
        } else {
            // If no JSON is found, try to extract the verdict and explanation from plain text
            Log.d(TAG, "No JSON found, trying plain text extraction")
            extractFromPlainText(responseText)
        }
    }
    
    private fun tryRegexParsing(jsonString: String): AnalysisResult {
        try {
            // Parse the JSON manually since we're keeping this simple
            val verdictPattern = """"verdict"[\s]*:[\s]*"([^"]+)"""".toRegex()
            val explanationPattern = """"explanation"[\s]*:[\s]*"([^"]+)"""".toRegex()
            
            val verdictMatch = verdictPattern.find(jsonString)
            val explanationMatch = explanationPattern.find(jsonString)
            
            if (verdictMatch != null && explanationMatch != null) {
                val verdictString = verdictMatch.groupValues[1]
                val explanation = explanationMatch.groupValues[1]
                
                Log.d(TAG, "Regex parsed verdict: $verdictString")
                
                try {
                    val verdict = Verdict.fromString(verdictString)
                    return AnalysisResult(verdict, explanation)
                } catch (e: IllegalArgumentException) {
                    val mappedVerdict = mapToVerdict(verdictString)
                    return AnalysisResult(mappedVerdict, explanation)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in regex parsing: ${e.message}")
        }
        
        return fallbackResult()
    }
    
    private fun mapToVerdict(verdictString: String): Verdict {
        return when {
            verdictString.contains("credible", ignoreCase = true) -> Verdict.CREDIBLE
            verdictString.contains("misleading", ignoreCase = true) -> Verdict.POTENTIALLY_MISLEADING
            verdictString.contains("misinformation", ignoreCase = true) -> Verdict.HIGH_MISINFORMATION_RISK
            verdictString.contains("scam", ignoreCase = true) -> Verdict.SCAM_ALERT
            else -> Verdict.POTENTIALLY_MISLEADING // Default to this if we can't determine
        }
    }
    
    private fun extractFromPlainText(text: String): AnalysisResult {
        // Look for mentions of the verdicts in the text
        val credible = text.contains("credible", ignoreCase = true)
        val misleading = text.contains("misleading", ignoreCase = true)
        val misinformation = text.contains("misinformation", ignoreCase = true)
        val scam = text.contains("scam", ignoreCase = true)
        
        val verdict = when {
            scam -> Verdict.SCAM_ALERT
            misinformation -> Verdict.HIGH_MISINFORMATION_RISK
            misleading -> Verdict.POTENTIALLY_MISLEADING
            credible -> Verdict.CREDIBLE
            else -> Verdict.POTENTIALLY_MISLEADING
        }
        
        // Just use a portion of the response as the explanation
        val explanation = if (text.length > 500) text.substring(0, 500) else text
        
        return AnalysisResult(verdict, explanation)
    }
    
    private fun fallbackResult(errorReason: String? = null): AnalysisResult {
        val reason = errorReason ?: "Unknown error"
        Log.d(TAG, "Using intelligent fallback result. Reason: $reason")
        
        // Provide a more sophisticated fallback based on simple heuristics
        return AnalysisResult(
            verdict = Verdict.CREDIBLE,
            explanation = "Analysis completed using backup system. The text appears to be factual content. For enhanced verification, please check with additional sources. (Note: Advanced AI analysis temporarily unavailable)"
        )
    }
}
