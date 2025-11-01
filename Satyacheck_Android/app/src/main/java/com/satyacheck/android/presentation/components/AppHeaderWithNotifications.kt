package com.satyacheck.android.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.satyacheck.android.presentation.MainViewModel
import com.satyacheck.android.presentation.screens.auth.AuthViewModel

/**
 * Creates an AppHeader with a notification bell and profile picture in the top right corner
 */
@Composable
fun AppHeaderWithNotifications(
    title: String,
    navController: NavController,
    showBackButton: Boolean = false,
    onBackPressed: () -> Unit = {},
    showMenuButton: Boolean = false,
    onMenuClicked: () -> Unit = {},
    mainViewModel: MainViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    useAnimatedTitle: Boolean = false
) {
    val unreadAlertsCount = mainViewModel.unreadAlertsCount.collectAsState().value
    val isAuthenticated = authViewModel.isAuthenticated.collectAsState().value
    val isGuestMode = authViewModel.isGuestMode.collectAsState().value
    
    AppHeader(
        title = title,
        showBackButton = showBackButton,
        onBackPressed = onBackPressed,
        showMenuButton = showMenuButton,
        onMenuClicked = onMenuClicked,
        showNotificationBell = true,
        unreadNotificationsCount = unreadAlertsCount,
        onNotificationBellClicked = { navController.navigate("alerts") },
        showProfilePicture = true,
        profilePictureUrl = null, // Will be replaced with user's profile image URL when available
        onProfileClicked = { navController.navigate("profile") },
        onSignOutClicked = {
            // Sign out the user and navigate to login
            if (isAuthenticated) {
                authViewModel.signOut()
                navController.navigate("login") {
                    popUpTo("dashboard") { inclusive = true }
                }
            }
        },
        useAnimatedTitle = useAnimatedTitle
    )
}
