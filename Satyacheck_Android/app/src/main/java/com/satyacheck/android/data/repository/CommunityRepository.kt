package com.satyacheck.android.data.repository

import com.satyacheck.android.data.remote.api.CommunityService
import com.satyacheck.android.domain.model.CommunityAlert
import com.satyacheck.android.domain.model.CommunityPost
import com.satyacheck.android.domain.model.Comment
import com.satyacheck.android.domain.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepository @Inject constructor(
    private val communityService: CommunityService
) {
    
    suspend fun getCommunityAlerts(page: Int = 0, size: Int = 10, location: String? = null): Flow<Result<List<CommunityAlert>>> = flow {
        emit(Result.Loading())
        try {
            val response = communityService.getCommunityAlerts(page, size, location)
            
            if (response.status == "success" && response.data != null) {
                val alerts = response.data.map { dto ->
                    CommunityAlert(
                        id = dto.id,
                        title = dto.title,
                        description = dto.description,
                        severity = dto.severity,
                        location = dto.location,
                        reportCount = dto.reportCount,
                        createdAt = dto.createdAt
                    )
                }
                emit(Result.Success(alerts))
            } else {
                emit(Result.Error(response.message ?: "Failed to get community alerts"))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("An error occurred: ${e.message}"))
        }
    }
    
    suspend fun getCommunityPosts(page: Int = 0, size: Int = 10): Flow<Result<List<CommunityPost>>> = flow {
        emit(Result.Loading())
        try {
            val response = communityService.getCommunityPosts(page, size)
            
            if (response.status == "success" && response.data != null) {
                val posts = response.data.map { dto ->
                    CommunityPost(
                        id = dto.id,
                        userId = dto.userId,
                        username = dto.username,
                        title = dto.title,
                        content = dto.content,
                        createdAt = dto.createdAt,
                        comments = dto.comments.map { commentDto ->
                            Comment(
                                id = commentDto.id,
                                userId = commentDto.userId,
                                username = commentDto.username,
                                content = commentDto.content,
                                createdAt = commentDto.createdAt
                            )
                        }
                    )
                }
                emit(Result.Success(posts))
            } else {
                emit(Result.Error(response.message ?: "Failed to get community posts"))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("An error occurred: ${e.message}"))
        }
    }
    
    suspend fun getCommunityPost(postId: String): Flow<Result<CommunityPost>> = flow {
        emit(Result.Loading())
        try {
            val response = communityService.getCommunityPost(postId)
            
            if (response.status == "success" && response.data != null) {
                val dto = response.data
                val post = CommunityPost(
                    id = dto.id,
                    userId = dto.userId,
                    username = dto.username,
                    title = dto.title,
                    content = dto.content,
                    createdAt = dto.createdAt,
                    comments = dto.comments.map { commentDto ->
                        Comment(
                            id = commentDto.id,
                            userId = commentDto.userId,
                            username = commentDto.username,
                            content = commentDto.content,
                            createdAt = commentDto.createdAt
                        )
                    }
                )
                emit(Result.Success(post))
            } else {
                emit(Result.Error(response.message ?: "Failed to get community post"))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("An error occurred: ${e.message}"))
        }
    }
    
    suspend fun createCommunityPost(title: String, content: String, analysisId: String? = null): Flow<Result<CommunityPost>> = flow {
        emit(Result.Loading())
        try {
            val postData = mutableMapOf(
                "title" to title,
                "content" to content
            )
            
            analysisId?.let { postData["analysisId"] = it }
            
            val response = communityService.createCommunityPost(postData)
            
            if (response.status == "success" && response.data != null) {
                val dto = response.data
                val post = CommunityPost(
                    id = dto.id,
                    userId = dto.userId,
                    username = dto.username,
                    title = dto.title,
                    content = dto.content,
                    createdAt = dto.createdAt,
                    comments = emptyList() // New post, no comments yet
                )
                emit(Result.Success(post))
            } else {
                emit(Result.Error(response.message ?: "Failed to create community post"))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("An error occurred: ${e.message}"))
        }
    }
    
    suspend fun addComment(postId: String, content: String): Flow<Result<Comment>> = flow {
        emit(Result.Loading())
        try {
            val commentData = mapOf(
                "content" to content
            )
            
            val response = communityService.addComment(postId, commentData)
            
            if (response.status == "success" && response.data != null) {
                val dto = response.data
                val comment = Comment(
                    id = dto.id,
                    userId = dto.userId,
                    username = dto.username,
                    content = dto.content,
                    createdAt = dto.createdAt
                )
                emit(Result.Success(comment))
            } else {
                emit(Result.Error(response.message ?: "Failed to add comment"))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("An error occurred: ${e.message}"))
        }
    }
    
    suspend fun reportMisinformation(
        title: String,
        description: String,
        contentType: String,
        contentUrl: String? = null
    ): Flow<Result<Boolean>> = flow {
        emit(Result.Loading())
        try {
            val reportData = mutableMapOf(
                "title" to title,
                "description" to description,
                "contentType" to contentType
            )
            
            contentUrl?.let { reportData["contentUrl"] = it }
            
            val response = communityService.reportMisinformation(reportData)
            
            if (response.status == "success") {
                emit(Result.Success(true))
            } else {
                emit(Result.Error(response.message ?: "Failed to report misinformation"))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("An error occurred: ${e.message}"))
        }
    }
}
