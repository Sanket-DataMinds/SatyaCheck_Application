package com.satyacheck.android.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Modern Google-style AppHeader component with options for navigation, actions, and styling
 * 
 * @param title The title to display in the header
 * @param showBackButton Whether to show a back button
 * @param onBackPressed Callback when back button is pressed
 * @param actions Optional actions to display on the right side of the header
 * @param centerTitle Whether to center the title (Google Material 3 style)
 * @param elevated Whether to show elevation shadow
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    title: String,
    showBackButton: Boolean = false,
    onBackPressed: () -> Unit = {},
    showMenuButton: Boolean = false,
    onMenuClicked: () -> Unit = {},
    showNotificationBell: Boolean = false,
    unreadNotificationsCount: Int = 0,
    onNotificationBellClicked: () -> Unit = {},
    showProfilePicture: Boolean = true,
    profilePictureUrl: String? = null,
    onProfileClicked: () -> Unit = {},
    onSignOutClicked: () -> Unit = {},
    actions: @Composable () -> Unit = {},
    centerTitle: Boolean = true,
    elevated: Boolean = false,
    useAnimatedTitle: Boolean = false
) {
    val surfaceColor = if (elevated) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.background
    }
    
    val elevation = if (elevated) 3.dp else 0.dp
    
    Surface(
        color = surfaceColor,
        tonalElevation = elevation,
        shadowElevation = if (elevated) 2.dp else 0.dp
    ) {
        if (centerTitle) {
            // Center-aligned top app bar (Google Material 3 style)
            CenterAlignedTopAppBar(
                title = {
                    if (useAnimatedTitle) {
                        AnimatedSatyaCheckTitle()
                    } else {
                        Text(
                            text = title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(
                            onClick = onBackPressed,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .clip(RoundedCornerShape(50))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else if (showMenuButton) {
                        IconButton(
                            onClick = onMenuClicked,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .clip(RoundedCornerShape(50))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = { 
                    if (showNotificationBell) {
                        NotificationBell(
                            unreadCount = unreadNotificationsCount,
                            onBellClick = onNotificationBellClicked
                        )
                    }
                    actions() 
                    if (showProfilePicture) {
                        ProfilePictureWithDropdown(
                            profilePictureUrl = profilePictureUrl,
                            onProfileClicked = onProfileClicked,
                            onSignOutClicked = onSignOutClicked
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = surfaceColor,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        } else {
            // Regular top app bar with left-aligned title (Google Search style)
            TopAppBar(
                title = {
                    if (useAnimatedTitle) {
                        AnimatedSatyaCheckTitle()
                    } else {
                        Text(
                            text = title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(
                            onClick = onBackPressed,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .clip(RoundedCornerShape(50))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else if (showMenuButton) {
                        IconButton(
                            onClick = onMenuClicked,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .clip(RoundedCornerShape(50))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = { 
                    if (showNotificationBell) {
                        NotificationBell(
                            unreadCount = unreadNotificationsCount,
                            onBellClick = onNotificationBellClicked
                        )
                    }
                    actions() 
                    if (showProfilePicture) {
                        ProfilePictureWithDropdown(
                            profilePictureUrl = profilePictureUrl,
                            onProfileClicked = onProfileClicked,
                            onSignOutClicked = onSignOutClicked
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppHeaderPreview() {
    MaterialTheme {
        AppHeader(
            title = "SatyaCheck",
            showBackButton = false,
            actions = {
                IconButton(
                    onClick = {},
                    modifier = Modifier.clip(RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = {},
                    modifier = Modifier.clip(RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "Account",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            centerTitle = true,
            elevated = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppHeaderWithBackButtonPreview() {
    MaterialTheme {
        AppHeader(
            title = "Analysis",
            showBackButton = true,
            onBackPressed = {},
            actions = {
                IconButton(
                    onClick = {},
                    modifier = Modifier.clip(RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            elevated = false
        )
    }
}

@Composable
fun ProfilePictureWithDropdown(
    profilePictureUrl: String?,
    onProfileClicked: () -> Unit,
    onSignOutClicked: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .padding(4.dp)
                .size(40.dp)
                .clip(CircleShape)
        ) {
            if (profilePictureUrl != null) {
                AsyncImage(
                    model = profilePictureUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(8.dp)
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Profile") },
                onClick = {
                    expanded = false
                    onProfileClicked()
                }
            )
            DropdownMenuItem(
                text = { Text("Sign Out") },
                onClick = {
                    expanded = false
                    onSignOutClicked()
                }
            )
        }
    }
}
