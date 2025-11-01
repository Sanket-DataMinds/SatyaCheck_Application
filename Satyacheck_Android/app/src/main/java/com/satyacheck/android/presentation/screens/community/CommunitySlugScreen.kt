package com.satyacheck.android.presentation.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.satyacheck.android.domain.model.Community
import com.satyacheck.android.domain.model.CommunityType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunitySlugScreen(
    slug: String,
    onBackPressed: () -> Unit,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val snackbarHostState = remember { SnackbarHostState() }
    // Use the LaunchedEffect pattern instead of var + by delegate
    val community = state.communities.find { it.id == slug }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = community?.name ?: "Community") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (community == null) {
                Text(
                    text = "Community not found",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Community header
                    item {
                        CommunityHeader(
                            name = community!!.name,
                            description = community!!.description,
                            members = community!!.members,
                            isOfficial = community!!.official,
                            isJoined = state.joinedCommunities.contains(slug),
                            onJoinClick = { viewModel.joinCommunity(slug) }
                        )
                    }
                    
                    // If official community, show announcements
                    if (community!!.official) {
                        item {
                            Text(
                                text = "Official Announcements",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        // Sample announcements
                        val announcements = listOf(
                            "We've launched a new feature to detect misinformation in videos. Try it now!",
                            "Join our webinar on 'Identifying Deep Fakes' this Friday at 6 PM.",
                            "Updated community guidelines are now in effect. Please review them."
                        )
                        
                        items(announcements) { announcement ->
                            AnnouncementCard(text = announcement)
                        }
                    } else {
                        // For regular communities, show welcome message and community posts
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Welcome to ${community!!.name}!",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "This is a community for people interested in fact-checking and fighting misinformation. Share your experiences and help others stay informed.",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Button(
                                        onClick = { /* Create post */ },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("Create Post")
                                    }
                                }
                            }
                        }
                        
                        // Show community posts
                        item {
                            Text(
                                text = "Recent Posts",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        // Filter posts for this community (in a real app)
                        // For now, just show all posts
                        items(state.posts) { post ->
                            PostCard(
                                post = post,
                                isLiked = state.likedPostIds.contains(post.id),
                                onPostClick = { /* Navigate to post details */ },
                                onLikeClick = { viewModel.likePost(post.id) },
                                onCommentClick = { /* Navigate to post details */ },
                                onReportClick = { viewModel.openReportDialog(post.id) }
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
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
fun CommunityHeader(
    name: String,
    description: String,
    members: String,
    isOfficial: Boolean,
    isJoined: Boolean,
    onJoinClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOfficial) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (isOfficial) {
                        Text(
                            text = "Official Community",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Text(
                        text = members,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                JoinButton(
                    isJoined = isJoined,
                    onClick = onJoinClick
                )
            }
            
            if (description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun JoinButton(
    isJoined: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = if (isJoined) {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) {
        Text(text = if (isJoined) "Joined" else "Join")
    }
}
