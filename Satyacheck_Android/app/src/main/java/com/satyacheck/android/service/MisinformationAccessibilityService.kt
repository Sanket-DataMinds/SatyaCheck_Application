package com.satyacheck.android.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.satyacheck.android.utils.TextAnalyzer
import com.satyacheck.android.domain.model.Verdict
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Accessibility service to analyze screen content for misinformation.
 * This service can extract text from the current screen and analyze it.
 */
@AndroidEntryPoint
class MisinformationAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var textAnalyzer: TextAnalyzer

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var lastAnalyzedText = ""
    private var isAnalyzing = false

    companion object {
        const val TAG = "MisinfoAccessService"
        const val ACTION_ANALYZE_CURRENT_SCREEN = "com.satyacheck.android.ACTION_ANALYZE_CURRENT_SCREEN"
        const val ACTION_ANALYSIS_RESULT = "com.satyacheck.android.ACTION_ANALYSIS_RESULT"
        const val EXTRA_CONTENT = "EXTRA_CONTENT"
        const val EXTRA_VERDICT = "EXTRA_VERDICT"
        const val EXTRA_EXPLANATION = "EXTRA_EXPLANATION"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Accessibility service created")
        
        // Register to receive broadcast intents requesting screen analysis
        LocalBroadcastManager.getInstance(this).registerReceiver(
            object : android.content.BroadcastReceiver() {
                override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
                    if (intent?.action == ACTION_ANALYZE_CURRENT_SCREEN) {
                        Log.d(TAG, "Received request to analyze current screen")
                        analyzeCurrentScreen()
                    }
                }
            },
            android.content.IntentFilter(ACTION_ANALYZE_CURRENT_SCREEN)
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // We don't process events directly, only on-demand via the analyzeCurrentScreen method
        // This helps preserve battery and avoids constant analysis
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "Accessibility service destroyed")
    }

    /**
     * Analyzes the current screen content for misinformation.
     * This method extracts text from the screen and sends it to the analyzer.
     */
    fun analyzeCurrentScreen() {
        if (isAnalyzing) {
            Log.d(TAG, "Already analyzing content, skipping")
            return
        }

        val rootNode = rootInActiveWindow ?: return
        try {
            isAnalyzing = true
            
            // Extract text from the screen
            val screenText = extractTextFromNode(rootNode)
            
            // Skip if the screen text is empty or the same as the last analyzed text
            if (screenText.isBlank() || screenText == lastAnalyzedText) {
                isAnalyzing = false
                return
            }
            
            lastAnalyzedText = screenText
            
            Log.d(TAG, "Analyzing screen content: ${screenText.take(100)}...")
            
            // Analyze the extracted text in a coroutine
            serviceScope.launch {
                try {
                    val result = textAnalyzer.analyzeText(screenText)
                    
                    // Send the result via broadcast
                    val intent = Intent(ACTION_ANALYSIS_RESULT).apply {
                        putExtra(EXTRA_CONTENT, screenText)
                        putExtra(EXTRA_VERDICT, result.verdict.name)
                        putExtra(EXTRA_EXPLANATION, result.explanation)
                    }
                    LocalBroadcastManager.getInstance(this@MisinformationAccessibilityService)
                        .sendBroadcast(intent)
                    
                    Log.d(TAG, "Analysis completed: ${result.verdict}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error analyzing screen content", e)
                    
                    // Send error result
                    val intent = Intent(ACTION_ANALYSIS_RESULT).apply {
                        putExtra(EXTRA_CONTENT, screenText)
                        putExtra(EXTRA_VERDICT, Verdict.UNKNOWN.name)
                        putExtra(EXTRA_EXPLANATION, "Error analyzing content: ${e.message}")
                    }
                    LocalBroadcastManager.getInstance(this@MisinformationAccessibilityService)
                        .sendBroadcast(intent)
                } finally {
                    isAnalyzing = false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting text from screen", e)
            isAnalyzing = false
        } finally {
            rootNode.recycle()
        }
    }

    /**
     * Recursively extracts text from an accessibility node and its children.
     */
    private fun extractTextFromNode(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""
        
        val sb = StringBuilder()
        
        // Get the text from this node
        val nodeText = node.text?.toString() ?: ""
        if (nodeText.isNotBlank()) {
            sb.append(nodeText).append(" ")
        }
        
        // Get content description if available
        val contentDesc = node.contentDescription?.toString() ?: ""
        if (contentDesc.isNotBlank() && contentDesc != nodeText) {
            sb.append(contentDesc).append(" ")
        }
        
        // Recursively process child nodes
        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i)
            if (childNode != null) {
                sb.append(extractTextFromNode(childNode))
                childNode.recycle()
            }
        }
        
        return sb.toString().trim()
    }
}
