package com.satyacheck.android.data.repository

import com.satyacheck.android.data.remote.api.EducationalService
import com.satyacheck.android.domain.model.EducationalContent
import com.satyacheck.android.domain.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EducationalRepository @Inject constructor(
    private val educationalService: EducationalService
) {
    
    suspend fun getEducationalContent(page: Int = 0, size: Int = 10, category: String? = null): Flow<Result<List<EducationalContent>>> = flow {
        emit(Result.Loading())
        try {
            val response = educationalService.getEducationalContent(page, size, category)
            
            if (response.status == "success" && response.data != null) {
                val content = response.data.map { dto ->
                    EducationalContent(
                        id = dto.id,
                        title = dto.title,
                        summary = dto.summary,
                        content = dto.content,
                        category = dto.category,
                        imageUrl = dto.imageUrl,
                        createdAt = dto.createdAt
                    )
                }
                emit(Result.Success(content))
            } else {
                emit(Result.Error(response.message ?: "Failed to get educational content"))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("An error occurred: ${e.message}"))
        }
    }
    
    suspend fun getEducationalContentById(contentId: String): Flow<Result<EducationalContent>> = flow {
        emit(Result.Loading())
        try {
            val response = educationalService.getEducationalContentById(contentId)
            
            if (response.status == "success" && response.data != null) {
                val dto = response.data
                val content = EducationalContent(
                    id = dto.id,
                    title = dto.title,
                    summary = dto.summary,
                    content = dto.content,
                    category = dto.category,
                    imageUrl = dto.imageUrl,
                    createdAt = dto.createdAt
                )
                emit(Result.Success(content))
            } else {
                emit(Result.Error(response.message ?: "Failed to get educational content"))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("An error occurred: ${e.message}"))
        }
    }
    
    suspend fun getEducationalCategories(): Flow<Result<List<String>>> = flow {
        emit(Result.Loading())
        try {
            val response = educationalService.getEducationalCategories()
            
            if (response.status == "success" && response.data != null) {
                emit(Result.Success(response.data))
            } else {
                emit(Result.Error(response.message ?: "Failed to get educational categories"))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("An error occurred: ${e.message}"))
        }
    }
}
