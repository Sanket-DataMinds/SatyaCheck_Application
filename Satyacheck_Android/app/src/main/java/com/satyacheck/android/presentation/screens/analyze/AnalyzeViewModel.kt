package com.satyacheck.android.presentation.screens.analyze

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory

import android.util.Log
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satyacheck.android.domain.model.AnalysisResult
import com.satyacheck.android.domain.analyzer.SpeechToTextService
import com.satyacheck.android.domain.model.Verdict
import com.satyacheck.android.domain.repository.AnalysisRepository
import com.satyacheck.android.utils.AccessibilityHelper
import com.satyacheck.android.utils.Result
import com.satyacheck.android.utils.Result.Success
import com.satyacheck.android.utils.Result.Error
import com.satyacheck.android.utils.Result.Loading
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AnalyzeState(
    val inputText: String = "",
    val selectedImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val isRecording: Boolean = false,
    val showCameraDialog: Boolean = false,
    val accessibilityServiceEnabled: Boolean = false,
    val analysisCompleted: Boolean = false,
    val selectedLanguage: String = "English",
    val error: String? = null,
    val analysisResult: AnalysisResult? = null
)

@HiltViewModel
class AnalyzeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analysisRepository: AnalysisRepository,
    private val apiDebugger: com.satyacheck.android.utils.ApiDebugger,
    private val textAnalyzer: com.satyacheck.android.utils.TextAnalyzer,
    private val imageAnalyzer: com.satyacheck.android.domain.analyzer.ImageAnalyzer,
    private val speechToTextService: SpeechToTextService
) : ViewModel() {
    
    private val _state = MutableStateFlow(
        AnalyzeState(
            inputText = "",
            selectedImageUri = null,
            isLoading = false,
            isRecording = false,
            showCameraDialog = false,
            accessibilityServiceEnabled = false,
            analysisCompleted = false,
            selectedLanguage = "English",
            error = null,
            analysisResult = null
        )
    )
    val state: StateFlow<AnalyzeState> = _state.asStateFlow()
    
    val snackbarHostState = SnackbarHostState()
    
    /**
     * Debug function to test backend API connection
     */
    fun testApiConnection() {
        viewModelScope.launch {
            try {
                // Don't set isLoading = true for API test to avoid triggering analysis UI
                _state.update { currentState: AnalyzeState -> 
                    currentState.copy(error = null) 
                }
                
                val result = apiDebugger.testBackendConnection()
                
                // Show result via snackbar only, don't update main state
                snackbarHostState.showSnackbar(
                    if (result.isSuccess) "✅ API Connection Successful!" else "❌ API Connection Failed: ${result.message}"
                )
                
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("❌ Error testing API: ${e.message}")
                snackbarHostState.showSnackbar("Error: ${e.message}")
            }
        }
    }
    
    fun updateInputText(text: String) {
        _state.update { currentState: AnalyzeState -> 
            currentState.copy(inputText = text) 
        }
    }
    
    fun showCameraDialog() {
        _state.update { currentState: AnalyzeState -> 
            currentState.copy(showCameraDialog = true) 
        }
    }
    
    fun hideCameraDialog() {
        _state.update { currentState: AnalyzeState -> 
            currentState.copy(showCameraDialog = false) 
        }
    }
    
    fun updateSelectedImageUri(uri: Uri?) {
        _state.update { currentState: AnalyzeState -> 
            currentState.copy(selectedImageUri = uri) 
        }
    }
    
    /**
     * Convert URI to Bitmap for image analysis
     */
    private suspend fun uriToBitmap(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            return@withContext BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e("AnalyzeViewModel", "Error converting URI to bitmap: ${e.message}")
            return@withContext null
        }
    }
    
    /**
     * Unified analyze function that handles both text and image analysis
     */
    fun analyzeContent() {
        viewModelScope.launch {
            try {
                _state.update { currentState: AnalyzeState -> 
                    currentState.copy(isLoading = true) 
                }
                
                val currentState = _state.value
                val textToAnalyze = currentState.inputText
                val selectedImageUri = currentState.selectedImageUri
                
                // Determine analysis type and perform accordingly
                val result = when {
                    // Priority 1: If image is selected, analyze image
                    selectedImageUri != null -> {
                        analyzeSelectedImage(selectedImageUri)
                    }
                    // Priority 2: If text is provided, analyze text
                    textToAnalyze.isNotBlank() -> {
                        analyzeProvidedText(textToAnalyze)
                    }
                    // No content provided
                    else -> {
                        showError("Please enter text or select an image to analyze")
                        return@launch
                    }
                }
                
                // Save analysis result to preferences
                val prefs = context.getSharedPreferences("satya_check_prefs", Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putString("verdict", result.verdict.name)
                    putString("explanation", result.explanation)
                    putString("original_content", textToAnalyze.ifBlank { "Image Analysis" })
                    putString("content_type", if (selectedImageUri != null) "IMAGE" else "TEXT")
                    apply()
                }
                
                _state.update { currentState -> 
                    currentState.copy(
                        isLoading = false,
                        analysisCompleted = true,
                        analysisResult = result,
                        error = null
                    )
                }
                
            } catch (e: Exception) {
                _state.update { currentState -> 
                    currentState.copy(
                        isLoading = false,
                        analysisResult = AnalysisResult(
                            explanation = "Error occurred: ${e.message}",
                            verdict = Verdict.UNKNOWN
                        )
                    )
                }
                showError("Analysis failed: ${e.message}")
            }
        }
    }
    
    /**
     * Analyze image from URI
     */
    private suspend fun analyzeSelectedImage(imageUri: Uri): AnalysisResult {
        val bitmap = uriToBitmap(imageUri)
        return if (bitmap != null) {
            imageAnalyzer.analyzeImage(bitmap)
        } else {
            AnalysisResult(
                verdict = Verdict.UNKNOWN,
                explanation = "Unable to load selected image. Please try selecting a different image."
            )
        }
    }
    
    /**
     * Analyze provided text
     */
    private suspend fun analyzeProvidedText(text: String): AnalysisResult {
        return try {
            // Use the TextAnalyzer directly for text analysis
            textAnalyzer.analyzeText(text)
        } catch (e: Exception) {
            Log.e("AnalyzeViewModel", "Text analysis error: ${e.message}")
            AnalysisResult(
                explanation = "Text analysis error: ${e.message}",
                verdict = Verdict.UNKNOWN
            )
        }
    }
    
    /**
     * Analyze image for misinformation using Vision API + Gemini AI
     */
    fun analyzeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                _state.update { currentState -> 
                    currentState.copy(isLoading = true) 
                }
                
                // Use ImageAnalyzer for combined Vision API + Text Analysis
                val result = imageAnalyzer.analyzeImage(bitmap)
                
                // Save analysis result to preferences
                val prefs = context.getSharedPreferences("satya_check_prefs", Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putString("verdict", result.verdict.name)
                    putString("explanation", result.explanation)
                    putString("original_content", "Image Analysis")
                    putString("content_type", "IMAGE")
                    apply()
                }
                
                _state.update { currentState -> 
                    currentState.copy(
                        isLoading = false,
                        analysisCompleted = true,
                        analysisResult = result,
                        error = null
                    )
                }
                
            } catch (e: Exception) {
                _state.update { currentState -> 
                    currentState.copy(
                        isLoading = false,
                        analysisResult = AnalysisResult(
                            explanation = "Error analyzing image: ${e.message}",
                            verdict = Verdict.UNKNOWN
                        )
                    )
                }
                showError("Image analysis failed: ${e.message}")
            }
        }
    }
    
    fun showError(message: String) {
        viewModelScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }
    
    fun resetAnalysis() {
        _state.update { currentState: AnalyzeState -> 
            currentState.copy(
                inputText = "",
                selectedImageUri = null,
                analysisResult = null
            )
        }
    }
    
    fun toggleAccessibilityService(enabled: Boolean) {
        // Get current state of the accessibility service
        val isCurrentlyEnabled = AccessibilityHelper.isAccessibilityServiceEnabled(context)
        
        if (enabled && !isCurrentlyEnabled) {
            // User wants to enable but service is not enabled
            // Open accessibility settings
            AccessibilityHelper.openAccessibilitySettings(context)
            
            // Show a message
            showError("Please enable the SatyaCheck Misinformation Analyzer in Accessibility Settings")
            
            // Update UI state to reflect actual system state
            _state.update { it.copy(accessibilityServiceEnabled = isCurrentlyEnabled) }
        } else if (!enabled && isCurrentlyEnabled) {
            // User wants to disable but service is enabled
            // Open accessibility settings to let them disable it
            AccessibilityHelper.openAccessibilitySettings(context)
            
            // Show a message
            showError("Please disable the SatyaCheck Misinformation Analyzer in Accessibility Settings")
            
            // Update UI state to reflect actual system state
            _state.update { it.copy(accessibilityServiceEnabled = isCurrentlyEnabled) }
        } else {
            // Update state to match current system state
            _state.update { it.copy(accessibilityServiceEnabled = isCurrentlyEnabled) }
        }
    }
    
    fun saveBitmapAndAnalyze(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                // Create a file to save the bitmap
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val imageFileName = "IMG_${timeStamp}.jpg"
                val imageFile = File(context.getExternalFilesDir(null), imageFileName)
                
                // Save the bitmap to the file
                try {
                    FileOutputStream(imageFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    
                    // Create a URI for the saved file
                    val imageUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        imageFile
                    )
                    
                    // Update the UI with the selected image
                    _state.update { it.copy(selectedImageUri = imageUri) }
                    
                    // Hide the camera dialog
                    hideCameraDialog()
                    
                    // Show success message
                    showError("Image captured successfully")
                    
                    // Start analysis
                    analyzeContent()
                } catch (e: IOException) {
                    showError("Failed to save image: ${e.message}")
                }
            } catch (e: Exception) {
                showError("Error processing image: ${e.message}")
            }
        }
    }
    
    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    // Initialize accessibility state when ViewModel is created
    init {
        // Set initial state based on current system settings
        val isAccessibilityEnabled = AccessibilityHelper.isAccessibilityServiceEnabled(context)
        _state.update { it.copy(accessibilityServiceEnabled = isAccessibilityEnabled) }
    }
    
    fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED) {
            if (state.value.isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        } else {
            showError("Microphone permission required. Please grant it in Settings.")
        }
    }
    
    private fun startRecording() {
        viewModelScope.launch {
            try {
                val result = speechToTextService.startRecording()
                result.onSuccess { message ->
                    _state.update { it.copy(isRecording = true) }
                    showError("🎤 Recording started... Tap again to stop and analyze")
                    
                    // Auto-stop recording after 30 seconds
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(30000) // 30 seconds
                        if (state.value.isRecording) {
                            stopRecording()
                        }
                    }
                }.onFailure { error ->
                    showError("Failed to start recording: ${error.message}")
                }
            } catch (e: Exception) {
                showError("Error starting recording: ${e.message}")
            }
        }
    }
    
    private fun stopRecording() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, isRecording = false) }
                showError("🔄 Converting speech to text...")
                
                val result = speechToTextService.stopRecordingAndConvertToText()
                result.onSuccess { transcribedText ->
                    if (transcribedText.isNotBlank()) {
                        // Update input text with transcribed content
                        _state.update { 
                            it.copy(
                                inputText = transcribedText,
                                isLoading = false
                            ) 
                        }
                        showError("✅ Speech converted to text! Tap 'Analyze' to check for misinformation.")
                        
                        // Optionally auto-analyze the transcribed text
                        // analyzeContent()
                    } else {
                        _state.update { it.copy(isLoading = false) }
                        showError("⚠️ No speech detected in recording. Please try again.")
                    }
                }.onFailure { error ->
                    _state.update { it.copy(isLoading = false) }
                    showError("❌ Speech conversion failed: ${error.message}")
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, isRecording = false) }
                showError("Error processing recording: ${e.message}")
            }
        }
    }
    
    override fun onCleared() {
        if (state.value.isRecording) {
            viewModelScope.launch {
                try {
                    speechToTextService.cancelRecording()
                    _state.update { it.copy(isRecording = false) }
                } catch (e: Exception) {
                    // Ignore errors during cleanup
                }
            }
        }
        super.onCleared()
    }
}