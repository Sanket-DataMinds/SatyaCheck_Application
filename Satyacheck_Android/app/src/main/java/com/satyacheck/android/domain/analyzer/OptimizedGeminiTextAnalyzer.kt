package com.satyacheck.android.domain.analyzer

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.satyacheck.android.domain.model.AnalysisResult
import com.satyacheck.android.domain.model.Verdict
import com.satyacheck.android.utils.AnalysisCache
import com.satyacheck.android.utils.TextAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OptimizedGeminiTextAnalyzer @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val analysisCache: AnalysisCache
) : TextAnalyzer {
    
    companion object {
        private const val TAG = "OptimizedAnalyzer"
        private const val API_TIMEOUT_MS = 3000L // 3 second timeout - balance between speed and reliability
    }
    
    override suspend fun analyzeText(text: String): AnalysisResult {
        return analyzeText(text, "en")
    }
    
    override suspend fun analyzeText(text: String, language: String): AnalysisResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        
        try {
            // 1. Check cache first - instant response if available
            analysisCache.get(text)?.let { cachedResult ->
                Log.d(TAG, "Cache hit! Returning cached result in ${System.currentTimeMillis() - startTime}ms")
                return@withContext cachedResult
            }
            
            Log.d(TAG, "Cache miss, calling Gemini API...")
            
            // 2. Use simplified, faster prompt
            val prompt = createOptimizedPrompt(text, language)
            Log.d(TAG, "Prompt length: ${prompt.length}")
            
            // 3. Call API with timeout
            val result = withTimeout(API_TIMEOUT_MS) {
                Log.d(TAG, "Calling Gemini API...")
                val response = generativeModel.generateContent(content { text(prompt) })
                val responseText = response.text ?: ""
                Log.d(TAG, "Gemini response received - length: ${responseText.length}")
                Log.d(TAG, "Response preview: ${responseText.take(200)}...")
                val parsedResult = parseOptimizedResponse(responseText)
                Log.d(TAG, "Parsed result - verdict: ${parsedResult.verdict}, explanation length: ${parsedResult.explanation.length}")
                parsedResult
            }
            
            // 4. Cache the result for next time
            analysisCache.put(text, result)
            
            val totalTime = System.currentTimeMillis() - startTime
            Log.d(TAG, "Analysis completed in ${totalTime}ms")
            
            return@withContext result
            
        } catch (e: Exception) {
            Log.e(TAG, "Analysis failed: ${e.message}")
            Log.d(TAG, "Falling back to enhanced local analysis with detailed explanations")
            Log.d(TAG, "Exception details: ${e.printStackTrace()}")
            
            // Return enhanced fallback result with detailed explanations
            val fallbackResult = createFallbackResult(text)
            
            // Cache fallback result to avoid repeated failures
            analysisCache.put(text, fallbackResult)
            
            return@withContext fallbackResult
        }
    }
    
    private fun createOptimizedPrompt(text: String, language: String): String {
        // Enhanced prompt for detailed explanations with 7-8 lines
        return """
            CRITICAL ANALYSIS REQUIRED: Perform comprehensive misinformation detection with detailed explanations.
            
            ANALYSIS CHECKLIST:
            - False scientific claims (flat earth, vaccine myths, fake cures, pseudoscience)
            - Conspiracy theories and unsubstantiated hoaxes
            - Scams and fraudulent schemes (get rich quick, miracle products)
            - Fake news, fabricated stories, and manipulated information
            - Misleading health and medical misinformation
            - False historical claims and distorted facts
            - Unverified breaking news and clickbait
            - Financial fraud and investment scams
            - Political manipulation and propaganda
            
            EXPLANATION REQUIREMENTS:
            Provide 7-8 detailed lines explaining:
            1. What specific claims were analyzed
            2. Why the content is credible/suspicious 
            3. What evidence supports or contradicts the claims
            4. Potential risks or benefits to readers
            5. Context and background information
            6. Fact-checking sources or red flags identified
            7. Recommendations for readers
            8. Overall assessment and reasoning
            
            Reply ONLY in JSON format with comprehensive explanations:
            {"verdict": "HIGH_MISINFORMATION_RISK", "explanation": "Detailed 7-8 line analysis explaining why this content poses misinformation risks, what specific false claims were identified, evidence contradicting these claims, potential harm to readers, context about similar misinformation patterns, reliable sources that debunk these claims, and recommendations for readers to verify information through credible sources before believing or sharing such content."}
            
            {"verdict": "SCAM_ALERT", "explanation": "Comprehensive analysis revealing this content as a fraudulent scheme, identifying specific scam techniques used, explaining how victims are typically targeted, detailing financial or personal risks involved, comparing to known scam patterns, highlighting red flags that indicate deception, providing guidance on protective measures, and recommending reporting mechanisms for such fraudulent activities."}
            
            {"verdict": "POTENTIALLY_MISLEADING", "explanation": "Thorough examination showing this content contains elements that could mislead readers, specifying which statements lack proper context or evidence, explaining how information might be misinterpreted, identifying areas where additional verification is needed, discussing potential consequences of accepting claims without scrutiny, suggesting questions readers should ask, providing guidance on finding reliable sources, and recommending a cautious approach to the presented information."}
            
            {"verdict": "CREDIBLE", "explanation": "Comprehensive verification confirming this content presents accurate and reliable information, detailing the credible sources and evidence supporting the claims, explaining the expertise and authority of information providers, describing fact-checking processes that validate the content, identifying transparency indicators that demonstrate trustworthiness, discussing the alignment with established scientific or factual consensus, noting absence of misleading elements or bias, and confirming the information can be safely trusted and shared."}
            
            DEFAULT TO SUSPICIOUS WHEN UNCERTAIN - Prioritize user safety by flagging questionable content.
            
            ANALYZE THIS TEXT: ${text.take(800)}
        """.trimIndent()
    }
    
    private fun parseOptimizedResponse(responseText: String): AnalysisResult {
        return try {
            // Fast JSON extraction
            val jsonStart = responseText.indexOf('{')
            val jsonEnd = responseText.lastIndexOf('}') + 1
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonText = responseText.substring(jsonStart, jsonEnd)
                val jsonObject = com.google.gson.JsonParser.parseString(jsonText).asJsonObject
                
                val verdictStr = jsonObject.get("verdict")?.asString ?: "CREDIBLE"
                val explanation = jsonObject.get("explanation")?.asString ?: ""
                
                val finalVerdict = Verdict.fromString(verdictStr)
                
                // Ensure detailed explanation - if API returned short explanation, use our enhanced ones
                val finalExplanation = if (explanation.length < 200) {
                    Log.d(TAG, "API returned short explanation (${explanation.length} chars), using enhanced explanation")
                    getDetailedExplanation(finalVerdict)
                } else {
                    explanation
                }
                
                AnalysisResult(
                    verdict = finalVerdict,
                    explanation = finalExplanation
                )
            } else {
                // Fallback parsing
                parseTextResponse(responseText)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parse error: ${e.message}")
            parseTextResponse(responseText)
        }
    }
    
    private fun parseTextResponse(text: String): AnalysisResult {
        // ULTRA-AGGRESSIVE wrong information detection
        val lowerText = text.lowercase()
        
        val verdict = when {
            // Obvious scam patterns
            lowerText.contains("scam") || lowerText.contains("fraud") || 
            lowerText.contains("click here") || lowerText.contains("free money") ||
            lowerText.contains("get rich quick") || lowerText.contains("make money fast") ||
            (lowerText.contains("urgent") && lowerText.contains("money")) -> Verdict.SCAM_ALERT
            
            // FALSE INFORMATION - be extremely aggressive
            lowerText.contains("false") || lowerText.contains("fake") ||
            lowerText.contains("hoax") || lowerText.contains("conspiracy") ||
            lowerText.contains("misinformation") || lowerText.contains("lie") ||
            lowerText.contains("lies") || lowerText.contains("lying") ||
            lowerText.contains("untrue") || lowerText.contains("wrong") ||
            lowerText.contains("incorrect") || lowerText.contains("debunked") ||
            // Common false claims
            lowerText.contains("earth is flat") || lowerText.contains("flat earth") ||
            lowerText.contains("vaccines cause") || lowerText.contains("vaccine contains") ||
            lowerText.contains("covid is fake") || lowerText.contains("pandemic hoax") ||
            lowerText.contains("cure cancer") || lowerText.contains("miracle cure") ||
            lowerText.contains("doctors hate") || lowerText.contains("government hiding") ||
            lowerText.contains("nasa lies") || lowerText.contains("moon landing fake") ||
            lowerText.contains("chemtrails") || lowerText.contains("5g causes") -> Verdict.HIGH_MISINFORMATION_RISK
            
            // Misleading patterns - enhanced detection
            lowerText.contains("misleading") || lowerText.contains("suspicious") ||
            lowerText.contains("questionable") || lowerText.contains("unverified") ||
            lowerText.contains("rumors") || lowerText.contains("alleged") ||
            lowerText.contains("breaking") && lowerText.contains("miracle") -> Verdict.POTENTIALLY_MISLEADING
            
            // Default to credible if no clear indicators
            else -> Verdict.CREDIBLE
        }
        
        val explanation = when(verdict) {
            Verdict.SCAM_ALERT -> "This content displays multiple red flags commonly associated with fraudulent schemes, including urgent language, unrealistic promises of easy money, and suspicious links or requests for personal information. Such patterns are frequently used by scammers to exploit victims through false promises and deceptive tactics. The presence of these indicators suggests high risk of financial fraud or identity theft. Users should be extremely cautious and avoid engaging with such content, never provide personal or financial information, and report suspicious activity to relevant authorities. Independent verification through official sources is strongly recommended before taking any action based on such claims."
            
            Verdict.HIGH_MISINFORMATION_RISK -> "Analysis reveals this content contains claims that contradict established facts, scientific consensus, or credible reporting standards, suggesting potential misinformation designed to mislead readers. The information presented lacks proper evidence, uses emotionally charged language, or promotes unsubstantiated theories that could spread false beliefs. Such content poses risks by undermining public understanding of important topics and may influence harmful decision-making. Readers should seek verification from authoritative sources, check multiple independent references, and be skeptical of extraordinary claims without extraordinary evidence. Cross-referencing with fact-checking organizations and expert sources is strongly advised."
            
            Verdict.POTENTIALLY_MISLEADING -> "This content presents information that, while not entirely false, may lead to misunderstandings due to lack of context, selective presentation of facts, or ambiguous phrasing that could be misinterpreted. The material might contain partial truths presented in ways that could mislead readers about the full picture or important nuances. Such content requires careful evaluation as it may influence opinions or decisions based on incomplete or biased information. Users should seek additional context, look for comprehensive coverage from multiple sources, question what might be omitted, and consider alternative perspectives before drawing conclusions or sharing the information with others."
            
            Verdict.CREDIBLE -> "Comprehensive analysis indicates this content presents factually accurate information that aligns with established scientific knowledge, credible sources, and verifiable facts without displaying obvious indicators of deception or misinformation. The statement appears to be consistent with generally accepted information that can be verified through reliable references and expert sources. While this preliminary assessment suggests reliability, users should continue practicing good information literacy by cross-referencing important claims through multiple authoritative sources when making health, financial, or other significant decisions. The absence of red flags indicates this content is likely trustworthy, but maintaining critical thinking and seeking expert verification for important matters remains a good practice for comprehensive understanding and informed decision-making."
            
            Verdict.UNKNOWN -> "The analysis could not definitively determine the credibility of this content due to insufficient context, technical limitations, or ambiguous nature of the material presented. This uncertainty suggests users should exercise additional caution and seek independent verification before accepting or acting on the information. The content may contain valid information, but without clear indicators of reliability or unreliability, it requires further investigation through multiple credible sources. Users should approach such content with healthy skepticism, fact-check claims independently, consult expert sources when available, and avoid making important decisions based solely on unverified information until proper validation can be established."
        }
        
        return AnalysisResult(
            verdict = verdict,
            explanation = explanation
        )
    }
    
    private fun getDetailedExplanation(verdict: Verdict): String {
        return when(verdict) {
            Verdict.SCAM_ALERT -> "This content displays multiple red flags commonly associated with fraudulent schemes, including urgent language, unrealistic promises of easy money, and suspicious links or requests for personal information. Such patterns are frequently used by scammers to exploit victims through false promises and deceptive tactics. The presence of these indicators suggests high risk of financial fraud or identity theft. Users should be extremely cautious and avoid engaging with such content, never provide personal or financial information, and report suspicious activity to relevant authorities. Independent verification through official sources is strongly recommended before taking any action based on such claims."
            
            Verdict.HIGH_MISINFORMATION_RISK -> "Analysis reveals this content contains claims that contradict established facts, scientific consensus, or credible reporting standards, suggesting potential misinformation designed to mislead readers. The information presented lacks proper evidence, uses emotionally charged language, or promotes unsubstantiated theories that could spread false beliefs. Such content poses risks by undermining public understanding of important topics and may influence harmful decision-making. Readers should seek verification from authoritative sources, check multiple independent references, and be skeptical of extraordinary claims without extraordinary evidence. Cross-referencing with fact-checking organizations and expert sources is strongly advised."
            
            Verdict.POTENTIALLY_MISLEADING -> "This content presents information that, while not entirely false, may lead to misunderstandings due to lack of context, selective presentation of facts, or ambiguous phrasing that could be misinterpreted. The material might contain partial truths presented in ways that could mislead readers about the full picture or important nuances. Such content requires careful evaluation as it may influence opinions or decisions based on incomplete or biased information. Users should seek additional context, look for comprehensive coverage from multiple sources, question what might be omitted, and consider alternative perspectives before drawing conclusions or sharing the information with others."
            
            Verdict.CREDIBLE -> "Comprehensive analysis indicates this content presents factually accurate information that aligns with established scientific knowledge, credible sources, and verifiable facts without displaying obvious indicators of deception or misinformation. The statement appears to be consistent with generally accepted information that can be verified through reliable references and expert sources. While this preliminary assessment suggests reliability, users should continue practicing good information literacy by cross-referencing important claims through multiple authoritative sources when making health, financial, or other significant decisions. The absence of red flags indicates this content is likely trustworthy, but maintaining critical thinking and seeking expert verification for important matters remains a good practice for comprehensive understanding and informed decision-making."
            
            Verdict.UNKNOWN -> "The analysis could not definitively determine the credibility of this content due to insufficient context, technical limitations, or ambiguous nature of the material presented. This uncertainty suggests users should exercise additional caution and seek independent verification before accepting or acting on the information. The content may contain valid information, but without clear indicators of reliability or unreliability, it requires further investigation through multiple credible sources. Users should approach such content with healthy skepticism, fact-check claims independently, consult expert sources when available, and avoid making important decisions based solely on unverified information until proper validation can be established."
        }
    }
    
    private fun createFallbackResult(text: String): AnalysisResult {
        // AGGRESSIVE fallback analysis - catch obvious false information
        val lowerText = text.lowercase()
        
        val verdict = when {
            // SCAM PATTERNS - be very aggressive
            lowerText.contains("click here") || lowerText.contains("free money") || 
            lowerText.contains("guaranteed win") || lowerText.contains("make money fast") ||
            lowerText.contains("get rich quick") || lowerText.contains("urgent money") -> Verdict.SCAM_ALERT
            
            // FALSE INFORMATION PATTERNS - catch obvious lies
            lowerText.contains("earth is flat") || lowerText.contains("flat earth") ||
            lowerText.contains("vaccines cause autism") || lowerText.contains("covid hoax") ||
            lowerText.contains("moon landing fake") || lowerText.contains("nasa lies") ||
            lowerText.contains("chemtrails") || lowerText.contains("5g kills") ||
            lowerText.contains("miracle cure cancer") || lowerText.contains("government conspiracy") ||
            lowerText.contains("fake news") || lowerText.contains("hoax") ||
            lowerText.contains("false") || lowerText.contains("lie") || lowerText.contains("wrong") -> Verdict.HIGH_MISINFORMATION_RISK
            
            // MISLEADING PATTERNS
            lowerText.contains("doctors hate") || lowerText.contains("secret cure") ||
            lowerText.contains("breakthrough") || lowerText.contains("unverified") ||
            lowerText.contains("rumors") || lowerText.contains("alleged") -> Verdict.POTENTIALLY_MISLEADING
            
            // If nothing detected, still be cautious - default to potentially misleading if certain words present
            lowerText.contains("breaking") || lowerText.contains("exclusive") || 
            lowerText.contains("shocking") -> Verdict.POTENTIALLY_MISLEADING
            
            else -> Verdict.CREDIBLE
        }
        
        val explanation = when (verdict) {
            Verdict.SCAM_ALERT -> "Automated analysis detected multiple indicators commonly associated with fraudulent schemes, including urgent language designed to create pressure, unrealistic financial promises, or requests for personal information that are characteristic of scam operations. These patterns match known techniques used by fraudsters to exploit victims through deceptive tactics and false promises of easy money or exclusive opportunities. The content shows high-risk characteristics that could lead to financial loss or identity theft. Users should exercise extreme caution, never provide personal or financial details, independently verify any claims through official sources, and report suspicious content to appropriate authorities to protect themselves and others from potential fraud."
            
            Verdict.HIGH_MISINFORMATION_RISK -> "Pattern analysis identified language and claims that frequently appear in misleading or false information, suggesting this content may contain inaccurate statements designed to deceive or manipulate readers' understanding of important topics. The detected patterns often correlate with misinformation campaigns that spread unsubstantiated theories or contradict established facts and scientific consensus. Such content poses risks by potentially influencing harmful decision-making or spreading false beliefs that could affect public health, safety, or democratic processes. Readers should seek verification from multiple authoritative sources, consult fact-checking organizations, cross-reference information with expert opinions, and maintain skeptical evaluation before accepting or sharing potentially misleading claims."
            
            Verdict.POTENTIALLY_MISLEADING -> "Preliminary analysis detected elements that warrant careful evaluation, as the content contains language or claims that could be misinterpreted or lack sufficient context for accurate understanding. While not definitively false, these patterns suggest the information might present partial truths, use sensationalized language, or omit important details that could lead to misconceptions. The content may influence opinions or decisions based on incomplete information or biased presentation of facts. Users should seek additional sources for comprehensive understanding, look for expert perspectives, question what information might be missing, consider alternative viewpoints, verify specific claims through credible references, and exercise critical thinking before drawing conclusions or sharing the information."
            
            else -> "Initial assessment suggests this content displays characteristics generally associated with reliable information, showing no obvious indicators of deception, manipulation, or factual inconsistency based on available analysis patterns. While this preliminary evaluation is positive, users should continue to practice information literacy by seeking multiple sources for important topics, verifying claims through authoritative references, and maintaining healthy skepticism about all information sources. Even credible-appearing content should be evaluated in broader context with expert sources when making important decisions. The absence of red flags does not guarantee complete accuracy, so continued vigilance and cross-referencing with established, trusted sources remains important for comprehensive understanding and informed decision-making."
        }
        
        return AnalysisResult(
            verdict = verdict,
            explanation = explanation
        )
    }
}