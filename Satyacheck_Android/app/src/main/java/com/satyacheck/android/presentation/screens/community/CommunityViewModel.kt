package com.satyacheck.android.presentation.screens.community

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satyacheck.android.domain.model.Comment
import com.satyacheck.android.domain.model.Community
import com.satyacheck.android.domain.model.CommunityPost
import com.satyacheck.android.domain.model.CommunityType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

data class CommunityState(
    val posts: List<CommunityPost> = emptyList(),
    val filteredPosts: List<CommunityPost> = emptyList(),
    val trendingPosts: List<CommunityPost> = emptyList(),
    val likedPostIds: Set<String> = emptySet(),
    val joinedCommunities: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isNewPostDialogOpen: Boolean = false,
    val newPostContent: String = "",
    val newPostLocation: String = "",
    val newPostAttachment: String? = null,
    val newPostTitle: String = "",
    val communities: List<Community> = emptyList(),
    val locationCommunities: List<Community> = emptyList(),
    val interestCommunities: List<Community> = emptyList(),
    val filteredCommunities: List<Community> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val currentCommunityId: String? = null,
    val commentDialogPostId: String? = null,
    val newComment: String = "",
    val reportDialogPostId: String? = null,
    val reportReason: String = "",
    val selectedTag: String = "All"
)

@HiltViewModel
class CommunityViewModel @Inject constructor() : ViewModel() {
    
    var state by mutableStateOf(CommunityState())
        private set
    
    // Community tags
    val communityTags = listOf(
        "All",
        "Official",
        "Local",
        "Health",
        "Tech",
        "Media",
        "Safety"
    )
    
    init {
        loadSampleData()
        loadCommunities()
    }
    
    private fun loadSampleData() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            
            // Simulate network delay
            delay(500)
            
            // Create sample posts
            val samplePosts = listOf(
                CommunityPost(
                    id = "1",
                    userId = "user123",
                    username = "Priya Sharma",
                    title = "Beware of WhatsApp Scam",
                    content = "I received a suspicious message claiming to be from my bank asking for OTP. Always verify with your bank directly and never share OTPs.",
                    createdAt = "2 hours ago",
                    communityId = "anti-phishing-squad",
                    verified = true,
                    fallback = "PS",
                    likes = 42,
                    comments = listOf(
                        Comment("1", "user456", "Rahul Gupta", "Thanks for the warning! My cousin almost fell for this last week.", "1 hour ago", fallback = "RG"),
                        Comment("2", "user789", "Amit Patel", "These scammers are getting more sophisticated. Always be careful!", "30 minutes ago", fallback = "AP")
                    )
                ),
                CommunityPost(
                    id = "2",
                    userId = "user456",
                    username = "Rahul Gupta",
                    title = "Fake News Alert",
                    content = "There's a viral message claiming schools will be closed next week due to heat wave. The education department has confirmed this is false. Always verify news from official sources.",
                    createdAt = "5 hours ago",
                    communityId = "delhi-truth-seekers",
                    fallback = "RG",
                    likes = 28,
                    comments = listOf(
                        Comment("3", "user123", "Priya Sharma", "I saw this on WhatsApp too. Thanks for clarifying!", "4 hours ago", fallback = "PS")
                    )
                ),
                CommunityPost(
                    id = "3",
                    userId = "user789",
                    username = "Amit Patel",
                    title = "Be careful of this SMS scam",
                    content = "I got a message claiming my electricity bill is due with a suspicious link. When I called the electricity office, they confirmed it was a scam. Be vigilant!",
                    createdAt = "1 day ago",
                    communityId = "mumbai-watchdogs",
                    fallback = "AP",
                    likes = 15,
                    location = "Mumbai, Maharashtra"
                ),
                CommunityPost(
                    id = "4",
                    userId = "user101",
                    username = "Neha Singh",
                    title = "Official COVID-19 Information",
                    content = "The Health Ministry has released new guidelines about booster doses. I've verified this information on the ministry website and it's accurate. Sharing the link for everyone.",
                    createdAt = "2 days ago",
                    communityId = "covid-fact-checking",
                    fallback = "NS",
                    likes = 67,
                    verified = true
                ),
                CommunityPost(
                    id = "5",
                    userId = "official-admin",
                    username = "SatyaCheck Team",
                    title = "Welcome to the Official Community",
                    content = "Welcome to the Official SatyaCheck Community! This is a space for all members to share tips, report misinformation, and help each other stay informed. Remember to follow our community guidelines and be respectful to all members.",
                    createdAt = "3 days ago",
                    communityId = "official-satyacheck",
                    fallback = "ST",
                    likes = 156,
                    verified = true
                ),
                CommunityPost(
                    id = "6",
                    userId = "tech-expert",
                    username = "Dr. Anil Kumar",
                    title = "How to spot AI-generated images",
                    content = "With the rise of AI image generators, it's becoming harder to spot fake images. Look for these telltale signs: unnatural hand positions, asymmetrical facial features, strange text, and inconsistent lighting. Zoom in on details to check for irregularities.",
                    createdAt = "1 week ago",
                    communityId = "deepfake-detectors",
                    fallback = "AK",
                    likes = 93,
                    verified = true,
                    comments = listOf(
                        Comment("4", "user345", "Meera Joshi", "This is so helpful! I nearly shared an AI-generated news photo yesterday until I noticed the strange hands.", "5 days ago", fallback = "MJ")
                    )
                )
            )
            
            // Set trending posts (posts with most likes)
            val trendingPosts = samplePosts.sortedByDescending { it.likes }.take(3)
            
            state = state.copy(
                posts = samplePosts,
                filteredPosts = samplePosts,
                trendingPosts = trendingPosts,
                isLoading = false
            )
        }
    }
    
    private fun loadCommunities() {
        viewModelScope.launch {
            // Official community
            val officialCommunity = Community(
                id = "official-satyacheck",
                name = "SatyaCheck Official",
                slug = "official-satyacheck",
                description = "The official SatyaCheck community for all members to share tips, report misinformation, and stay informed.",
                members = "12.5k members",
                type = CommunityType.OFFICIAL,
                official = true
            )
            
            // Location-based communities
            val locationCommunities = listOf(
                Community(
                    id = "mumbai-watchdogs",
                    name = "Mumbai Watchdogs",
                    slug = "mumbai-watchdogs",
                    description = "A community for Mumbaikars to share and verify local information.",
                    members = "5.2k members",
                    type = CommunityType.LOCATION,
                    location = "Mumbai, Maharashtra"
                ),
                Community(
                    id = "delhi-truth-seekers",
                    name = "Delhi Truth Seekers",
                    slug = "delhi-truth-seekers",
                    description = "Debunking false information circulating in Delhi.",
                    members = "3.8k members",
                    type = CommunityType.LOCATION,
                    location = "Delhi"
                ),
                Community(
                    id = "bangalore-fact-checkers",
                    name = "Bangalore Fact Checkers",
                    slug = "bangalore-fact-checkers",
                    description = "Tech city, tech-savvy fact checkers.",
                    members = "4.1k members",
                    type = CommunityType.LOCATION,
                    location = "Bangalore, Karnataka"
                ),
                Community(
                    id = "chennai-info-guardians",
                    name = "Chennai Info Guardians",
                    slug = "chennai-info-guardians",
                    description = "Protecting Chennai from misinformation.",
                    members = "2.7k members",
                    type = CommunityType.LOCATION,
                    location = "Chennai, Tamil Nadu"
                )
            )
            
            // Interest-based communities
            val interestCommunities = listOf(
                Community(
                    id = "anti-phishing-squad",
                    name = "Anti-Phishing Squad",
                    slug = "anti-phishing-squad",
                    description = "Helping people identify and avoid phishing attempts.",
                    members = "7.8k members",
                    type = CommunityType.INTEREST
                ),
                Community(
                    id = "covid-fact-checking",
                    name = "COVID-19 Fact Checkers",
                    slug = "covid-fact-checking",
                    description = "Verifying information related to COVID-19.",
                    members = "9.4k members",
                    type = CommunityType.INTEREST
                ),
                Community(
                    id = "deepfake-detectors",
                    name = "Deepfake Detectors",
                    slug = "deepfake-detectors",
                    description = "Spotting AI-generated fake images and videos.",
                    members = "6.3k members",
                    type = CommunityType.INTEREST
                ),
                Community(
                    id = "medical-myth-busters",
                    name = "Medical Myth Busters",
                    slug = "medical-myth-busters",
                    description = "Healthcare professionals busting medical misinformation.",
                    members = "5.9k members",
                    type = CommunityType.INTEREST
                )
            )
            
            val allCommunities = listOf(officialCommunity) + locationCommunities + interestCommunities
            
            state = state.copy(
                communities = allCommunities,
                locationCommunities = locationCommunities,
                interestCommunities = interestCommunities
            )
        }
    }
    
    fun onTagSelected(tag: String) {
        state = state.copy(selectedTag = tag)
        
        // Apply tag filtering
        applyTagFilter(tag)
        
        // Reapply search filter if active
        if (state.searchQuery.isNotEmpty()) {
            applySearchFilter()
        }
    }
    
    fun onSearchQueryChange(query: String) {
        state = state.copy(
            searchQuery = query,
            isSearchActive = query.isNotEmpty()
        )
        
        // Apply tag filter first
        applyTagFilter(state.selectedTag)
        
        // Then apply search filter if query is not empty
        if (query.isNotEmpty()) {
            applySearchFilter()
        }
        
        // Filter communities for search results
        val filteredCommunities = if (query.isNotEmpty()) {
            state.communities.filter { community ->
                community.name.contains(query, ignoreCase = true) ||
                community.description.contains(query, ignoreCase = true) ||
                community.location?.contains(query, ignoreCase = true) == true
            }
        } else {
            emptyList()
        }
        
        state = state.copy(filteredCommunities = filteredCommunities)
    }
    
    fun toggleLike(postId: String) {
        val likedPostIds = state.likedPostIds.toMutableSet()
        
        if (likedPostIds.contains(postId)) {
            // Unlike
            likedPostIds.remove(postId)
            
            // Update like count in post
            val updatedPosts = state.posts.map { post ->
                if (post.id == postId) {
                    post.copy(likes = (post.likes - 1).coerceAtLeast(0))
                } else {
                    post
                }
            }
            
            state = state.copy(
                posts = updatedPosts,
                likedPostIds = likedPostIds
            )
        } else {
            // Like
            likedPostIds.add(postId)
            
            // Update like count in post
            val updatedPosts = state.posts.map { post ->
                if (post.id == postId) {
                    post.copy(likes = post.likes + 1)
                } else {
                    post
                }
            }
            
            state = state.copy(
                posts = updatedPosts,
                likedPostIds = likedPostIds
            )
        }
        
        // Refresh filtered posts by reapplying current filters
        applyTagFilter(state.selectedTag)
        if (state.searchQuery.isNotEmpty()) {
            applySearchFilter()
        }
        
        // Update trending posts
        val updatedTrendingPosts = state.posts.sortedByDescending { it.likes }.take(3)
        state = state.copy(trendingPosts = updatedTrendingPosts)
    }
    
    // This function has been moved to the Community interaction section below
    
    fun onNewPostClick() {
        state = state.copy(isNewPostDialogOpen = true)
    }
    
    fun onNewPostContentChange(content: String) {
        state = state.copy(newPostContent = content)
    }
    
    fun onNewPostTitleChange(title: String) {
        state = state.copy(newPostTitle = title)
    }
    
    fun onNewPostLocationChange(location: String) {
        state = state.copy(newPostLocation = location)
    }
    
    fun onNewPostAttachmentChange(attachment: String?) {
        state = state.copy(newPostAttachment = attachment)
    }
    
    fun onNewPostDialogDismiss() {
        state = state.copy(
            isNewPostDialogOpen = false,
            newPostContent = "",
            newPostTitle = "",
            newPostLocation = "",
            newPostAttachment = null
        )
    }
    
    fun createNewPost() {
        submitNewPost()
    }
    
    fun submitNewPost() {
        if (state.newPostContent.isBlank() && state.newPostTitle.isBlank()) {
            return
        }
        
        val newPost = CommunityPost(
            id = UUID.randomUUID().toString(),
            userId = "current-user", // In a real app, this would be the logged-in user's ID
            username = "You", // In a real app, this would be the logged-in user's name
            title = state.newPostTitle,
            content = state.newPostContent,
            createdAt = "Just now",
            communityId = state.currentCommunityId ?: "official-satyacheck", // Default to official community
            fallback = "YO",
            likes = 0,
            verified = false,
            location = state.newPostLocation.ifBlank { null },
            attachedFile = state.newPostAttachment
        )
        
        // Add new post to the top of the list
        val updatedPosts = listOf(newPost) + state.posts
        
        state = state.copy(
            posts = updatedPosts,
            isNewPostDialogOpen = false,
            newPostContent = "",
            newPostTitle = "",
            newPostLocation = "",
            newPostAttachment = null
        )
        
        // Refresh filtered posts by reapplying current filters
        applyTagFilter(state.selectedTag)
        if (state.searchQuery.isNotEmpty()) {
            applySearchFilter()
        }
    }
    
    fun openComments(postId: String) {
        state = state.copy(commentDialogPostId = postId)
    }
    
    fun closeComments() {
        state = state.copy(
            commentDialogPostId = null,
            newComment = ""
        )
    }
    
    fun onNewCommentChange(comment: String) {
        state = state.copy(newComment = comment)
    }
    
    fun addComment() {
        val postId = state.commentDialogPostId ?: return
        
        if (state.newComment.isBlank()) {
            return
        }
        
        val newComment = Comment(
            id = UUID.randomUUID().toString(),
            userId = "current-user", // In a real app, this would be the logged-in user's ID
            username = "You", // In a real app, this would be the logged-in user's name
            content = state.newComment,
            createdAt = "Just now",
            fallback = "YO"
        )
        
        // Add comment to the post
        val updatedPosts = state.posts.map { post ->
            if (post.id == postId) {
                // Add comment to the top of the list
                post.copy(comments = listOf(newComment) + post.comments)
            } else {
                post
            }
        }
        
        state = state.copy(
            posts = updatedPosts,
            newComment = ""
        )
        
        // Refresh filtered posts by reapplying current filters
        applyTagFilter(state.selectedTag)
        if (state.searchQuery.isNotEmpty()) {
            applySearchFilter()
        }
    }
    
    fun openReportDialog(postId: String) {
        state = state.copy(reportDialogPostId = postId)
    }
    
    fun closeReportDialog() {
        state = state.copy(
            reportDialogPostId = null,
            reportReason = ""
        )
    }
    
    fun onReportReasonChange(reason: String) {
        state = state.copy(reportReason = reason)
    }
    
    fun submitReport() {
        // In a real app, this would send the report to a backend
        closeReportDialog()
        // You might want to show a snackbar or other feedback here
    }
    
    // This function has been moved to the Community interaction section below
    
    // Search functions - defined earlier in the file
    
    fun clearSearch() {
        state = state.copy(
            searchQuery = "",
            isSearchActive = false
        )
        updateFilteredContent()
    }
    
    private fun updateFilteredContent() {
        if (state.currentCommunityId != null) {
            // Filter posts within the current community only
            val communityPosts = state.posts.filter { it.communityId == state.currentCommunityId }
            
            val filteredPosts = if (state.searchQuery.isNotEmpty()) {
                val query = state.searchQuery.lowercase()
                communityPosts.filter { 
                    it.title.lowercase().contains(query) || 
                    it.content.lowercase().contains(query) || 
                    it.username.lowercase().contains(query)
                }
            } else {
                communityPosts
            }
            
            state = state.copy(filteredPosts = filteredPosts)
        } else {
            // Apply current tag filter and search filter
            applyTagFilter(state.selectedTag)
            if (state.searchQuery.isNotEmpty()) {
                applySearchFilter()
            }
        }
    }
    
    // Apply tag filter to posts
    private fun applyTagFilter(tag: String) {
        val basePosts = if (tag == "All") {
            state.posts
        } else {
            val filteredCommunityIds = when (tag) {
                "Official" -> listOf("official-satyacheck", "satyacheck-official", "fact-check-india")
                "Local" -> listOf("mumbai-watchdogs", "delhi-truth-seekers", "bangalore-fact-checkers", 
                                  "kolkata-myth-busters", "chennai-info-guardians")
                "Health" -> listOf("covid-fact-checking")
                "Tech" -> listOf("deepfake-detectors")
                "Media" -> listOf("media-literacy-group")
                "Safety" -> listOf("anti-phishing-squad")
                else -> emptyList()
            }
            
            state.posts.filter { post ->
                filteredCommunityIds.contains(post.communityId)
            }
        }
        
        state = state.copy(filteredPosts = basePosts)
    }
    
    // Apply search filter on top of current filtered posts
    private fun applySearchFilter() {
        val query = state.searchQuery.lowercase()
        if (query.isEmpty()) return
        
        val searchFiltered = state.filteredPosts.filter { post ->
            post.content.lowercase().contains(query) ||
            post.title.lowercase().contains(query) ||
            post.username.lowercase().contains(query) ||
            state.communities.find { it.id == post.communityId }?.name?.lowercase()?.contains(query) == true
        }
        
        state = state.copy(filteredPosts = searchFiltered)
    }
    
    // Post interaction
    // toggleLike is defined earlier in the file
    
    fun likePost(postId: String) {
        toggleLike(postId)
    }
    
    // Community interaction
    fun joinCommunity(communityId: String) {
        val currentJoinedCommunities = state.joinedCommunities.toMutableSet()
        if (currentJoinedCommunities.contains(communityId)) {
            currentJoinedCommunities.remove(communityId)
        } else {
            currentJoinedCommunities.add(communityId)
        }
        state = state.copy(joinedCommunities = currentJoinedCommunities)
    }
    
    fun setCurrentCommunity(communityId: String?) {
        state = state.copy(currentCommunityId = communityId)
        updateFilteredContent()
    }
    
    // New Post Dialog functions
    // These functions are defined earlier in the file, no need to duplicate
    
    // Submit new post (keeping this implementation as it's different from the earlier one)
    fun submitNewPostWithTag() {
        if (state.newPostContent.isBlank()) return
        
        // Determine a community ID based on the selected tag
        val communityId = when (state.selectedTag) {
            "Official" -> "official-satyacheck"
            "Local" -> "mumbai-watchdogs" // Default to Mumbai if no specific location
            "Health" -> "covid-fact-checking"
            "Tech" -> "deepfake-detectors"
            "Media" -> "media-literacy-group"
            "Safety" -> "anti-phishing-squad"
            else -> "" // Empty for "All" tag
        }
        
        val newPost = CommunityPost(
            id = UUID.randomUUID().toString(),
            userId = "current-user",  // In a real app, this would be the actual user ID
            username = "You",  // In a real app, this would be the actual username
            title = state.newPostContent.take(50).let { if (it.length >= 50) "$it..." else it },
            content = state.newPostContent,
            createdAt = "Just now",
            communityId = communityId,
            comments = emptyList()
        )
        
        val updatedPosts = state.posts.toMutableList().apply {
            add(0, newPost)  // Add at the top
        }
        
        state = state.copy(
            posts = updatedPosts,
            isNewPostDialogOpen = false,
            newPostContent = "",
            newPostLocation = ""
        )
        
        // Refresh filtered posts by reapplying current filters
        applyTagFilter(state.selectedTag)
        if (state.searchQuery.isNotEmpty()) {
            applySearchFilter()
        }
    }
    
    // Comment Dialog functions
    // openComments is defined earlier in the file
    // closeComments is defined earlier in the file
    // onNewCommentChange is defined earlier in the file
    
    // addComment implementation specific to this section
    private fun addCommentInSection() {
        val postId = state.commentDialogPostId ?: return
        if (state.newComment.isBlank()) return
        
        val newComment = Comment(
            id = UUID.randomUUID().toString(),
            userId = "current-user",  // In a real app, this would be the actual user ID
            username = "You",  // In a real app, this would be the actual username
            content = state.newComment,
            createdAt = "Just now"
        )
        
        val updatedPosts = state.posts.map { post ->
            if (post.id == postId) {
                val updatedComments = post.comments.toMutableList().apply {
                    add(newComment)
                }
                post.copy(comments = updatedComments)
            } else {
                post
            }
        }
        
        state = state.copy(
            posts = updatedPosts,
            newComment = ""
        )
        
        // Refresh filtered posts by reapplying current filters
        applyTagFilter(state.selectedTag)
        if (state.searchQuery.isNotEmpty()) {
            applySearchFilter()
        }
    }
    
    // Report Dialog functions
    // These functions are defined earlier in the file
    
    // onReportReasonChange is defined earlier in the file
    
    // submitReport is defined earlier in the file
    private fun submitReportInSection() {
        // In a real app, this would send the report to a backend server
        // For now, we'll just close the dialog
        state = state.copy(
            reportDialogPostId = null,
            reportReason = ""
        )
        
        // You could also show a snackbar or toast to confirm the report was submitted
    }
}