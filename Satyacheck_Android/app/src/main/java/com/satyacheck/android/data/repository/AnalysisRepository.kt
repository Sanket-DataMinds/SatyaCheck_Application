package com.satyacheck.android.data.repository

import android.content.Context
import android.net.Uri
import com.satyacheck.android.data.remote.api.AnalysisService
import com.satyacheck.android.data.remote.dto.AnalysisRequestDto
import com.satyacheck.android.domain.model.AnalysisResult
import com.satyacheck.android.domain.model.Result
import com.satyacheck.android.domain.model.Verdict
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalysisRepository @Inject constructor(
    private val analysisService: AnalysisService,
    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) {
    
    suspend fun analyzeText(text: String, language: String? = null): Flow<Result<AnalysisResult>> = flow {
        emit(Result.Loading())
        try {
            // Use provided language or get from preferences
            val userLanguage = language ?: run {
                var lang = "en" // Default to English
                userPreferencesRepository.getUserLanguage().collect { lang = it }
                lang
            }
            
            val request = AnalysisRequestDto(
                content = text,
                contentType = "TEXT",
                language = userLanguage
            )
            
            val response = analysisService.analyzeText(request)
            
            if (response.status == "success" && response.data != null) {
                val data = response.data
                emit(Result.Success(
                    AnalysisResult(
                        verdict = Verdict.fromString(data.verdict),
                        explanation = data.explanation
                    )
                ))
            } else {
                emit(Result.Error(response.message ?: "Analysis failed"))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("An error occurred: ${e.message}"))
        }
    }
    
    suspend fun analyzeImage(uri: Uri, language: String? = null): Flow<Result<AnalysisResult>> = flow {
        emit(Result.Loading())
        try {
            // Use provided language or get from preferences
            val userLanguage = language ?: run {
                var lang = "en" // Default to English
                userPreferencesRepository.getUserLanguage().collect { lang = it }
                lang
            }
            
            // Convert Uri to File
            val file = uriToFile(uri) ?: throw IOException("Failed to read image file")
            
            // Create multipart request
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
            
            val response = analysisService.analyzeImage(imagePart, userLanguage)
            
            if (response.status == "success" && response.data != null) {
                val data = response.data
                emit(Result.Success(
                    AnalysisResult(
                        verdict = Verdict.fromString(data.verdict),
                        explanation = data.explanation
                    )
                ))
            } else {
                emit(Result.Error(response.message ?: "Image analysis failed"))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("An error occurred: ${e.message}"))
        }
    }
    
    suspend fun analyzeAudio(uri: Uri, language: String? = null): Flow<Result<AnalysisResult>> = flow {
        emit(Result.Loading())
        try {
            // Use provided language or get from preferences
            val userLanguage = language ?: run {
                var lang = "en" // Default to English
                userPreferencesRepository.getUserLanguage().collect { lang = it }
                lang
            }
            
            // Convert Uri to File
            val file = uriToFile(uri) ?: throw IOException("Failed to read audio file")
            
            // Create multipart request
            val requestFile = file.asRequestBody("audio/*".toMediaTypeOrNull())
            val audioPart = MultipartBody.Part.createFormData("audio", file.name, requestFile)
            
            val response = analysisService.analyzeAudio(audioPart, userLanguage)
            
            if (response.status == "success" && response.data != null) {
                val data = response.data
                emit(Result.Success(
                    AnalysisResult(
                        verdict = Verdict.fromString(data.verdict),
                        explanation = data.explanation
                    )
                ))
            } else {
                emit(Result.Error(response.message ?: "Audio analysis failed"))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("An error occurred: ${e.message}"))
        }
    }
    
    suspend fun getAnalysisHistory(page: Int = 0, size: Int = 10): Flow<Result<List<AnalysisResult>>> = flow {
        emit(Result.Loading())
        try {
            val response = analysisService.getAnalysisHistory(page, size)
            
            if (response.status == "success" && response.data != null) {
                val analyses = response.data.map { dto ->
                    AnalysisResult(
                        verdict = Verdict.fromString(dto.verdict),
                        explanation = dto.explanation
                    )
                }
                emit(Result.Success(analyses))
            } else {
                emit(Result.Error(response.message ?: "Failed to get analysis history"))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("An error occurred: ${e.message}"))
        }
    }
    
    /**
     * Utility function to convert a Uri to a File
     */
    private fun uriToFile(uri: Uri): File? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val outputFile = File(context.cacheDir, "temp_file_${System.currentTimeMillis()}")
            
            FileOutputStream(outputFile).use { outputStream ->
                val buffer = ByteArray(4 * 1024) // 4k buffer
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                outputStream.flush()
            }
            
            inputStream.close()
            return outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
