package com.satyacheck.android.domain.model

data class CommunityAlert(
    val id: String,
    val title: String,
    val description: String,
    val severity: String,
    val location: String?,
    val reportCount: Int,
    val createdAt: String
)

data class CommunityPost(
    val id: String,
    val userId: String,
    val username: String,
    val title: String,
    val content: String,
    val createdAt: String,
    val likes: Int = 0,
    val comments: List<Comment> = emptyList(),
    val communityId: String = "",
    val avatar: String = "",
    val fallback: String = "",
    val verified: Boolean = false,
    val attachedFile: String? = null,
    val location: String? = null
)

data class Comment(
    val id: String,
    val userId: String,
    val username: String,
    val content: String,
    val createdAt: String,
    val avatar: String = "",
    val fallback: String = "",
    val verified: Boolean = false
)

data class Community(
    val id: String,
    val name: String,
    val slug: String,
    val description: String = "",
    val members: String = "0 members",
    val type: CommunityType = CommunityType.INTEREST,
    val official: Boolean = false,
    val location: String? = null
)

enum class CommunityType {
    LOCATION, INTEREST, OFFICIAL
}
