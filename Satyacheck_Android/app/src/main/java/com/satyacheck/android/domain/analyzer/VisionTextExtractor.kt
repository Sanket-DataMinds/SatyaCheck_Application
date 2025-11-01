package com.satyacheck.android.domain.analyzer

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class VisionTextExtractor @Inject constructor() {
    
    companion object {
        private const val TAG = "VisionTextExtractor"
    }
    
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    /**
     * Extract text from image using Google ML Kit Text Recognition
     */
    suspend fun extractTextFromImage(bitmap: Bitmap, apiKey: String = ""): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting text extraction from image using ML Kit")
            
            // Create InputImage from bitmap
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            
            // Use suspendCancellableCoroutine to convert callback-based API to coroutines
            val result = suspendCancellableCoroutine<String> { continuation ->
                textRecognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val extractedText = visionText.text
                        Log.d(TAG, "Text extraction successful, length: ${extractedText.length}")
                        if (extractedText.isNotBlank()) {
                            Log.d(TAG, "Extracted text preview: ${extractedText.take(100)}...")
                        }
                        continuation.resume(extractedText)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Text extraction failed: ${exception.message}", exception)
                        continuation.resume("")
                    }
            }
            
            return@withContext result
            
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting text from image: ${e.message}", e)
            return@withContext ""
        }
    }
    
    /**
     * Quick text detection check (for preview purposes)
     */
    suspend fun quickTextCheck(bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        try {
            val text = extractTextFromImage(bitmap)
            return@withContext text.isNotBlank()
        } catch (e: Exception) {
            Log.e(TAG, "Error in quick text check: ${e.message}")
            return@withContext false
        }
    }
}