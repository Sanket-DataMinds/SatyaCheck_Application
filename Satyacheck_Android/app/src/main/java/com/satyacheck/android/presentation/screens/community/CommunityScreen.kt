package com.satyacheck.android.presentation.screens.community

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.satyacheck.android.domain.model.Comment
import com.satyacheck.android.domain.model.Community
import com.satyacheck.android.domain.model.CommunityPost
import com.satyacheck.android.domain.model.CommunityType
import com.satyacheck.android.presentation.components.AnimatedSearchHeader
import com.satyacheck.android.presentation.components.AppHeader
import com.satyacheck.android.presentation.components.MainLayout
import com.satyacheck.android.presentation.components.openAppDrawer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onNavigateToAnalyze: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToEducate: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCommunityDetails: (String) -> Unit,
    onNavigateToMap: () -> Unit = {},
    onNavigateToProfile: () -> Unit = { /* Navigate to profile */ },
    onNavigateToUserProfile: (String) -> Unit = { /* Navigate to user profile */ },
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Track which community has its details shown
    var showingCommunityDetailsId by remember { mutableStateOf<String?>(null) }
    
    // Handle dialogs
    val showNewPostDialog = state.isNewPostDialogOpen
    val showCommentDialog = state.commentDialogPostId != null
    val showReportDialog = state.reportDialogPostId != null
    
    MainLayout(
        currentRoute = "community",
        onNavigate = { route ->
            when (route) {
                "analyze" -> onNavigateToAnalyze()
                "dashboard" -> onNavigateToDashboard()
                "educate" -> onNavigateToEducate()
                "settings" -> onNavigateToSettings()
                "map" -> onNavigateToMap()
            }
        },
        snackbarHostState = snackbarHostState,
        drawerState = drawerState,
        topBar = {
            AnimatedSearchHeader(
                title = "Community",
                onQueryChange = viewModel::onSearchQueryChange,
                onSearch = { viewModel.onSearchQueryChange(it) },
                showMenuButton = true,
                onMenuClicked = { openAppDrawer(drawerState, scope) },
                showNotificationBell = true,
                unreadNotificationsCount = 0,
                onNotificationBellClicked = { onNavigateToProfile() },
                placeholderText = "Search communities and posts",
                hideSearchIcon = true
            )
        }
    ) {
        // Community content
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (showingCommunityDetailsId != null) {
            // Show community details screen
            CommunityDetailsScreen(
                communityId = showingCommunityDetailsId!!,
                onBackClick = { showingCommunityDetailsId = null },
                viewModel = viewModel,
                onNavigateToUserProfile = onNavigateToUserProfile
            )
        } else {
            // Main community screen
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Community tags/filters
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(viewModel.communityTags) { tag ->
                                val isSelected = state.selectedTag == tag
                                
                                OutlinedButton(
                                    onClick = { viewModel.onTagSelected(tag) },
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text(text = tag)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // If search is active, show search results
                    if (state.isSearchActive) {
                        // Search results - communities
                        if (state.filteredCommunities.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Communities",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                ) {
                                    items(state.filteredCommunities) { community ->
                                        CommunityItem(
                                            community = community,
                                            isJoined = state.joinedCommunities.contains(community.id),
                                            onJoinClick = { viewModel.joinCommunity(community.id) },
                                            onClick = { 
                                                showingCommunityDetailsId = community.id
                                                viewModel.setCurrentCommunity(community.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Search results - posts
                        item {
                            if (state.filteredPosts.isNotEmpty()) {
                                Text(
                                    text = "Posts",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                Text(
                                    text = "No results found for '${state.searchQuery}'",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp)
                                )
                            }
                        }
                        
                        items(state.filteredPosts) { post ->
                            val community = state.communities.find { it.id == post.communityId }
                            PostCard(
                                post = post,
                                isLiked = state.likedPostIds.contains(post.id),
                                onPostClick = { onNavigateToCommunityDetails(post.id) },
                                onLikeClick = { viewModel.toggleLike(post.id) },
                                onCommentClick = { viewModel.openComments(post.id) },
                                onReportClick = { viewModel.openReportDialog(post.id) },
                                communityName = community?.name,
                                isCommunityJoined = state.joinedCommunities.contains(post.communityId),
                                onJoinCommunityClick = { viewModel.joinCommunity(post.communityId) },
                                onUserProfileClick = onNavigateToUserProfile
                            )
                        }
                    } else {
                        // Regular community view
                        
                        // Official community
                        val officialCommunity = state.communities.find { it.official }
                        if (officialCommunity != null) {
                            item {
                                OfficialCommunityCard(
                                    community = officialCommunity,
                                    isJoined = state.joinedCommunities.contains(officialCommunity.id),
                                    onJoinClick = { viewModel.joinCommunity(officialCommunity.id) },
                                    onCardClick = { 
                                        showingCommunityDetailsId = officialCommunity.id
                                        viewModel.setCurrentCommunity(officialCommunity.id)
                                    }
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                        
                        // Discover Communities section
                        item {
                            Text(
                                text = "Discover Communities",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        // Communities by location
                        item {
                            CommunityGroupCard(
                                title = "By Location",
                                communities = state.locationCommunities,
                                joinedCommunities = state.joinedCommunities,
                                onJoinClick = { viewModel.joinCommunity(it) },
                                onCommunityClick = { communityId -> 
                                    showingCommunityDetailsId = communityId
                                    viewModel.setCurrentCommunity(communityId)
                                }
                            )
                        }
                        
                        // Communities by interest
                        item {
                            CommunityGroupCard(
                                title = "By Interest",
                                communities = state.interestCommunities,
                                joinedCommunities = state.joinedCommunities,
                                onJoinClick = { viewModel.joinCommunity(it) },
                                onCommunityClick = { communityId -> 
                                    showingCommunityDetailsId = communityId
                                    viewModel.setCurrentCommunity(communityId)
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        // Trending Posts section
                        item {
                            Text(
                                text = "Trending Posts",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                items(state.trendingPosts) { post ->
                                    TrendingPostCard(
                                        post = post,
                                        onClick = { onNavigateToCommunityDetails(post.id) },
                                        onUserProfileClick = onNavigateToUserProfile
                                    )
                                }
                            }
                            
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                        
                        // Community Feed heading
                        item {
                            Text(
                                text = "Community Feed",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        // Post cards
                        if (state.filteredPosts.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No posts found for selected filter",
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        items(state.filteredPosts) { post ->
                            PostCard(
                                post = post,
                                isLiked = state.likedPostIds.contains(post.id),
                                onPostClick = { onNavigateToCommunityDetails(post.id) },
                                onLikeClick = { viewModel.toggleLike(post.id) },
                                onCommentClick = { viewModel.openComments(post.id) },
                                onReportClick = { viewModel.openReportDialog(post.id) },
                                onUserProfileClick = onNavigateToUserProfile
                            )
                        }
                        
                        // Bottom spacing
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
                
                // Floating action button for new post
                FloatingActionButton(
                    onClick = { viewModel.onNewPostClick() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create new post",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
    
    // New Post Dialog
    if (showNewPostDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onNewPostDialogDismiss() },
            title = { Text("Create New Post") },
            text = {
                Column {
                    OutlinedTextField(
                        value = state.newPostTitle,
                        onValueChange = { viewModel.onNewPostTitleChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Title (optional)") },
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = state.newPostContent,
                        onValueChange = { viewModel.onNewPostContentChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        placeholder = { Text("What's on your mind?") }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Attach file button
                        OutlinedButton(
                            onClick = { /* Implement file picker */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Attach file"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Attach")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Location button
                        OutlinedButton(
                            onClick = { /* Implement location picker */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Add location"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Location")
                        }
                    }
                    
                    // Location field (if location button was clicked)
                    if (state.newPostLocation.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = state.newPostLocation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            
                            IconButton(
                                onClick = { viewModel.onNewPostLocationChange("") }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove location",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.createNewPost() },
                    enabled = state.newPostContent.isNotBlank() || state.newPostTitle.isNotBlank()
                ) {
                    Text("Post")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onNewPostDialogDismiss() }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Comments Dialog
    if (showCommentDialog) {
        val post = state.posts.find { it.id == state.commentDialogPostId }
        
        if (post != null) {
            AlertDialog(
                onDismissRequest = { viewModel.closeComments() },
                title = { Text("Comments") },
                text = {
                    Column {
                        // Existing comments
                        if (post.comments.isEmpty()) {
                            Text(
                                text = "No comments yet. Be the first to comment!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = 8.dp)
                            ) {
                                post.comments.forEach { comment ->
                                    CommentItem(
                                        comment = comment, 
                                        onUserProfileClick = onNavigateToUserProfile
                                    )
                                    
                                    Divider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // New comment input
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "YO",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Comment text field
                            OutlinedTextField(
                                value = state.newComment,
                                onValueChange = { viewModel.onNewCommentChange(it) },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Add a comment...") },
                                maxLines = 3
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Send button
                            IconButton(
                                onClick = { viewModel.addComment() },
                                enabled = state.newComment.isNotBlank()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send comment",
                                    tint = if (state.newComment.isNotBlank()) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.closeComments() }) {
                        Text("Close")
                    }
                },
                properties = androidx.compose.ui.window.DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            )
        }
    }
    
    // Report Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeReportDialog() },
            title = { Text("Report Post") },
            text = {
                Column {
                    Text(
                        text = "Please tell us why you're reporting this post:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = state.reportReason,
                        onValueChange = { viewModel.onReportReasonChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Reason for reporting...") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.submitReport() },
                    enabled = state.reportReason.isNotBlank()
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeReportDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CommunityDetailsScreen(
    communityId: String,
    onBackClick: () -> Unit,
    viewModel: CommunityViewModel,
    onNavigateToUserProfile: (String) -> Unit = {}
) {
    val state = viewModel.state
    val community = state.communities.find { it.id == communityId }
    
    if (community == null) {
        // Community not found
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Community not found",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }
    
    // Get posts for this community
    val communityPosts = state.posts.filter { it.communityId == communityId }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Community header
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Back button and community name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Back"
                        )
                    }
                    
                    Text(
                        text = community.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Button(
                        onClick = { viewModel.joinCommunity(communityId) }
                    ) {
                        Text(
                            text = if (state.joinedCommunities.contains(communityId)) "Joined" else "Join"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = community.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = community.members,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (community.location != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = community.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        // Community posts
        if (communityPosts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No posts in this community yet",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { viewModel.onNewPostClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create post"
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text("Create First Post")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(communityPosts) { post ->
                    PostCard(
                        post = post,
                        isLiked = state.likedPostIds.contains(post.id),
                        onPostClick = { /* View post details */ },
                        onLikeClick = { viewModel.toggleLike(post.id) },
                        onCommentClick = { viewModel.openComments(post.id) },
                        onReportClick = { viewModel.openReportDialog(post.id) },
                        onUserProfileClick = onNavigateToUserProfile
                    )
                }
                
                // Bottom spacing for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
            
            // Floating action button for new post in this community
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = { 
                        viewModel.setCurrentCommunity(communityId)
                        viewModel.onNewPostClick() 
                    },
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create new post",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
    
    // Show new post dialog when open
    if (state.isNewPostDialogOpen) {
        NewPostDialog(
            postContent = state.newPostContent,
            location = state.newPostLocation,
            onPostContentChange = viewModel::onNewPostContentChange,
            onLocationChange = viewModel::onNewPostLocationChange,
            onDismiss = viewModel::onNewPostDialogDismiss,
            onPost = viewModel::submitNewPostWithTag
        )
    }
    
    // Show comment dialog when open
    if (state.commentDialogPostId != null) {
        CommentDialog(
            post = state.posts.find { it.id == state.commentDialogPostId }!!,
            newComment = state.newComment,
            onCommentChange = viewModel::onNewCommentChange,
            onDismiss = viewModel::closeComments,
            onAddComment = viewModel::addComment,
            onNavigateToUserProfile = { userId -> onNavigateToUserProfile(userId) }
        )
    }
    
    // Show report dialog when open
    if (state.reportDialogPostId != null) {
        ReportDialog(
            reportReason = state.reportReason,
            onReportReasonChange = viewModel::onReportReasonChange,
            onDismiss = viewModel::closeReportDialog,
            onReport = viewModel::submitReport
        )
    }
}

@Composable
fun NewPostDialog(
    postContent: String,
    location: String,
    onPostContentChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onPost: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Post") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = postContent,
                    onValueChange = onPostContentChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("What's on your mind?") },
                    placeholder = { Text("Share information or ask a question...") },
                    minLines = 3,
                    maxLines = 5
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    OutlinedTextField(
                        value = location,
                        onValueChange = onLocationChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Location (optional)") },
                        placeholder = { Text("Add your location") },
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onPost,
                enabled = postContent.isNotBlank()
            ) {
                Text("Post")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CommentDialog(
    post: CommunityPost,
    newComment: String,
    onCommentChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onAddComment: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comments") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Post title
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Existing comments
                if (post.comments.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        post.comments.forEach { comment ->
                            CommentItem(
                                comment = comment,
                                onUserProfileClick = { userId -> 
                                    onDismiss()
                                    onNavigateToUserProfile(userId)
                                }
                            )
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                } else {
                    Text(
                        text = "No comments yet. Be the first to comment!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // New comment input
                OutlinedTextField(
                    value = newComment,
                    onValueChange = onCommentChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Add a comment...") },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (newComment.isNotBlank()) {
                                    onAddComment()
                                }
                            },
                            enabled = newComment.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send comment",
                                tint = if (newComment.isNotBlank()) MaterialTheme.colorScheme.primary 
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun CommentItem(
    comment: Comment,
    onUserProfileClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar with ripple effect for better UX
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = { onUserProfileClick(comment.userId) }),
                contentAlignment = Alignment.Center
            ) {
                if (comment.avatar?.isNotEmpty() == true) {
                    // If avatar image URL is available, show it
                    // AsyncImage could be used here in a real app
                    Text(
                        text = comment.username.first().toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    // Fallback to initials
                    Text(
                        text = comment.username.first().toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Username and timestamp with better styling
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comment.username,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onUserProfileClick(comment.userId) }
                    )
                    
                    // If user is verified, show a verification icon
                    if (comment.verified == true) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified User",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                
                Text(
                    text = comment.createdAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Comment content
        Text(
            text = comment.content,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 32.dp) // Align with username
        )
    }
}

@Composable
fun ReportDialog(
    reportReason: String,
    onReportReasonChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onReport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Content") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Please provide a reason for reporting this content:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = reportReason,
                    onValueChange = onReportReasonChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter reason for reporting...") },
                    minLines = 3,
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Your report will be reviewed by our team. Thank you for helping keep the community safe.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onReport,
                enabled = reportReason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityPostCard(
    post: CommunityPost,
    onPostClick: () -> Unit,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onReportClick: () -> Unit,
    isCommunityJoined: Boolean,
    onJoinCommunityClick: () -> Unit,
    onUserProfileClick: (String) -> Unit = {}
) {
    ElevatedCard(
        onClick = onPostClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Post header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // User avatar (clickable)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable(onClick = { onUserProfileClick(post.userId) }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.username.first().toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Username and timestamp
                    Column {
                        Text(
                            text = post.username,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onUserProfileClick(post.userId) }
                        )
                        
                        Text(
                            text = post.createdAt,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Join community button
                if (post.communityId.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onJoinCommunityClick,
                        modifier = Modifier.padding(start = 8.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isCommunityJoined) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isCommunityJoined) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            contentColor = if (isCommunityJoined) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(if (isCommunityJoined) "Joined" else "Join")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Post title
            if (post.title.isNotEmpty()) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Post content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Post actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Like button
                Row(
                    modifier = Modifier
                        .clickable { onLikeClick() }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Like",
                        tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "Like",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Comment button
                Row(
                    modifier = Modifier
                        .clickable { onCommentClick() }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comment",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "Comment (${post.comments.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Report button
                Row(
                    modifier = Modifier
                        .clickable { onReportClick() }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Report",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "Report",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
