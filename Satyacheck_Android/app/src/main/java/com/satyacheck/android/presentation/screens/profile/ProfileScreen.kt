package com.satyacheck.android.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.satyacheck.android.presentation.components.AppHeader
import com.satyacheck.android.presentation.screens.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    onEditProfile: () -> Unit,
    onSettingsClicked: () -> Unit,
    onSignOut: () -> Unit = {},
    userId: String? = null // If null, show current user's profile, otherwise show specified user
) {
    val userProfileState by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    LaunchedEffect(key1 = userId) {
        viewModel.getUserProfile(userId)
    }
    
    // Determine if this is the current user's profile or someone else's
    val isCurrentUserProfile = userId == null
    
    Scaffold(
        topBar = {
            AppHeader(
                title = if (isCurrentUserProfile) "My Profile" else "User Profile",
                showBackButton = true,
                onBackPressed = onBackPressed,
                actions = {
                    // Only show edit and settings buttons for current user
                    if (isCurrentUserProfile) {
                        IconButton(onClick = onEditProfile) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile"
                            )
                        }
                        IconButton(onClick = onSettingsClicked) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    }
                },
                showProfilePicture = false
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Always attempt to show profile content first if available
            if (userProfileState != null) {
                ProfileContent(
                    userProfile = userProfileState!!,
                    onEditProfile = onEditProfile,
                    onSignOut = {
                        viewModel.signOut()
                        authViewModel.signOut()
                        onSignOut()
                    },
                    isCurrentUser = userId == null
                )
            } else if (isLoading) {
                // Only show loading if we don't have profile data
                LoadingProfileUI()
            } else {
                // Show empty state only if not loading and no profile
                EmptyProfileUI()
            }
            
            // If there's an error but we have profile data, we can show both
            if (error != null && !isLoading && userProfileState != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val displayText = if (error?.contains("Unable to resolve host") == true) {
                            "Offline mode: using locally saved data"
                        } else {
                            error ?: "Offline mode: using locally saved data"
                        }
                        
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingProfileUI() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Loading profile information...")
    }
}

@Composable
fun ErrorProfileUI(error: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Error loading profile: $error",
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun EmptyProfileUI() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("No profile information available")
    }
}

@Composable
fun ProfileContent(
    userProfile: com.satyacheck.android.domain.model.UserProfile,
    onEditProfile: () -> Unit,
    onSignOut: () -> Unit = {},
    isCurrentUser: Boolean = true
) {
    // Profile picture and header section
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = userProfile.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = userProfile.email,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display verification status
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Verified User",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.size(4.dp))
            
            Text(
                text = "Verified User",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Only show edit button for current user
            if (isCurrentUser) {
                Spacer(modifier = Modifier.width(16.dp))
                
                androidx.compose.material3.TextButton(
                    onClick = onEditProfile,
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text("Edit")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // User stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(
                title = "Analyses",
                value = userProfile.totalAnalyses.toString(),
                icon = Icons.Default.Shield
            )
            
            StatCard(
                title = "Detected",
                value = userProfile.misinformationDetected.toString(),
                icon = Icons.Default.Shield
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // User level
        LevelProgressCard(level = calculateUserLevel(userProfile.totalAnalyses))
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Activity Summary
        Text(
            text = "Activity Summary",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Start
        )
        
        ActivitySummaryCard(userProfile = userProfile)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Achievements section
        Text(
            text = "Achievements",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Start
        )
        
        AchievementsSection(totalAnalyses = userProfile.totalAnalyses)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Badges section
        Text(
            text = "Badges",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Start
        )
        
        BadgesSection(totalAnalyses = userProfile.totalAnalyses, detectedCount = userProfile.misinformationDetected)
        
        // Sign Out Button (only for current user)
        if (isCurrentUser) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier
            .size(width = 120.dp, height = 100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LevelProgressCard(level: Int) {
    val progress = (level % 5) / 5f
    val nextLevel = level + 1
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Level",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Level",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.size(4.dp))
                    
                    Text(
                        text = level.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Progress to Level $nextLevel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AchievementsSection(totalAnalyses: Int) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        AchievementItem(
            title = "Fact Checker",
            description = "Complete 10 analyses",
            isUnlocked = totalAnalyses >= 10,
            progress = if (totalAnalyses < 10) totalAnalyses / 10f else 1f
        )
        
        AchievementItem(
            title = "Truth Seeker",
            description = "Complete 50 analyses",
            isUnlocked = totalAnalyses >= 50,
            progress = if (totalAnalyses < 50) totalAnalyses / 50f else 1f
        )
        
        AchievementItem(
            title = "Misinformation Fighter",
            description = "Complete 100 analyses",
            isUnlocked = totalAnalyses >= 100,
            progress = if (totalAnalyses < 100) totalAnalyses / 100f else 1f
        )
    }
}

@Composable
fun AchievementItem(
    title: String,
    description: String,
    isUnlocked: Boolean,
    progress: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (isUnlocked) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.size(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUnlocked) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (isUnlocked) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
fun BadgesSection(totalAnalyses: Int, detectedCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Badge(
            title = "Rookie",
            isUnlocked = true,
            icon = Icons.Default.Shield
        )
        
        Badge(
            title = "Expert",
            isUnlocked = totalAnalyses >= 50,
            icon = Icons.Default.Shield
        )
        
        Badge(
            title = "Master",
            isUnlocked = totalAnalyses >= 100,
            icon = Icons.Default.Shield
        )
        
        Badge(
            title = "Champion",
            isUnlocked = detectedCount >= 20,
            icon = Icons.Default.Shield
        )
    }
}

@Composable
fun Badge(
    title: String,
    isUnlocked: Boolean,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    if (isUnlocked) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
                .border(
                    width = 2.dp,
                    color = if (isUnlocked) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isUnlocked) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(36.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = if (isUnlocked) 
                MaterialTheme.colorScheme.onSurface 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

// Helper function to calculate user level based on total analyses
private fun calculateUserLevel(totalAnalyses: Int): Int {
    return when {
        totalAnalyses < 10 -> 1
        totalAnalyses < 25 -> 2
        totalAnalyses < 50 -> 3
        totalAnalyses < 100 -> 4
        totalAnalyses < 200 -> 5
        else -> 6
    }
}
