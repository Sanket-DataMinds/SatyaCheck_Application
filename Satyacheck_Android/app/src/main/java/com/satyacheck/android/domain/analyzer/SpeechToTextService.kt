package com.satyacheck.android.domain.analyzer

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.satyacheck.android.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class SpeechToTextService @Inject constructor(
    private val context: Context
) {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false
    private val client = OkHttpClient()
    
    private val apiKey = BuildConfig.SPEECH_TO_TEXT_API_KEY

    /**
     * Start recording audio
     */
    suspend fun startRecording(): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (isRecording) {
                return@withContext Result.failure(Exception("Already recording"))
            }

            // Create audio file
            audioFile = File(context.cacheDir, "audio_recording_${System.currentTimeMillis()}.3gp")
            
            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile?.absolutePath)
                
                prepare()
                start()
            }
            
            isRecording = true
            Result.success("Recording started successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Stop recording and convert to text
     */
    suspend fun stopRecordingAndConvertToText(): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!isRecording) {
                return@withContext Result.failure(Exception("Not currently recording"))
            }

            // Stop recording
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false

            // Convert audio to text
            audioFile?.let { file ->
                if (file.exists()) {
                    val result = convertAudioToText(file)
                    // Clean up audio file
                    file.delete()
                    return@withContext result
                } else {
                    return@withContext Result.failure(Exception("Audio file not found"))
                }
            } ?: return@withContext Result.failure(Exception("No audio file to process"))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Convert audio file to text using Google Speech-to-Text API
     */
    private suspend fun convertAudioToText(audioFile: File): Result<String> = 
        suspendCancellableCoroutine { continuation ->
            try {
                // Convert audio file to base64
                val audioBytes = audioFile.readBytes()
                val base64Audio = android.util.Base64.encodeToString(audioBytes, android.util.Base64.NO_WRAP)
                
                // Create the JSON request body for Speech-to-Text API
                val jsonRequest = JSONObject().apply {
                    put("config", JSONObject().apply {
                        put("encoding", "AMR")
                        put("sampleRateHertz", 8000)
                        put("languageCode", "en-US")
                        put("enableAutomaticPunctuation", true)
                    })
                    put("audio", JSONObject().apply {
                        put("content", base64Audio)
                    })
                }

                android.util.Log.d("SpeechToText", "Audio file size: ${audioBytes.size} bytes")
                android.util.Log.d("SpeechToText", "Base64 length: ${base64Audio.length}")
                android.util.Log.d("SpeechToText", "Request URL: https://speech.googleapis.com/v1/speech:recognize?key=${apiKey.take(10)}...")

                val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("https://speech.googleapis.com/v1/speech:recognize?key=$apiKey")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        android.util.Log.e("SpeechToText", "API call failed", e)
                        continuation.resume(Result.failure(Exception("Network error: ${e.message}")))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                val errorBody = response.body?.string()
                                android.util.Log.e("SpeechToText", "API error: ${response.code} - $errorBody")
                                continuation.resume(
                                    Result.failure(Exception("API error (${response.code}): ${response.message}. $errorBody"))
                                )
                                return
                            }

                            val responseBody = response.body?.string()
                            if (responseBody != null) {
                                try {
                                    val jsonResponse = JSONObject(responseBody)
                                    val results = jsonResponse.optJSONArray("results")
                                    
                                    if (results != null && results.length() > 0) {
                                        val firstResult = results.getJSONObject(0)
                                        val alternatives = firstResult.getJSONArray("alternatives")
                                        
                                        if (alternatives.length() > 0) {
                                            val transcript = alternatives.getJSONObject(0)
                                                .getString("transcript")
                                            continuation.resume(Result.success(transcript))
                                        } else {
                                            continuation.resume(
                                                Result.failure(Exception("No speech detected in audio"))
                                            )
                                        }
                                    } else {
                                        continuation.resume(
                                            Result.failure(Exception("No speech detected in audio"))
                                        )
                                    }
                                } catch (e: Exception) {
                                    continuation.resume(Result.failure(e))
                                }
                            } else {
                                continuation.resume(
                                    Result.failure(Exception("Empty response from API"))
                                )
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }

    /**
     * Check if currently recording
     */
    fun isCurrentlyRecording(): Boolean = isRecording

    /**
     * Cancel recording without converting to text
     */
    suspend fun cancelRecording(): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (isRecording) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                isRecording = false
                
                // Clean up audio file
                audioFile?.delete()
                audioFile = null
                
                Result.success("Recording cancelled")
            } else {
                Result.failure(Exception("Not currently recording"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}