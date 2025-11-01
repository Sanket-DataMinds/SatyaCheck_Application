package com.satyacheck.android.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry

/**
 * Navigation animations for smooth transitions between screens
 */
object NavigationAnimations {
    
    private const val ANIMATION_DURATION = 300
    private const val FADE_DURATION = 200
    
    // Slide in from right (for forward navigation)
    val slideInFromRight = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    )
    
    // Slide out to left (when navigating forward)
    val slideOutToLeft = slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    )
    
    // Slide in from left (for back navigation)
    val slideInFromLeft = slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    )
    
    // Slide out to right (when navigating back)
    val slideOutToRight = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    )
    
    // Fade animations for subtle transitions
    val fadeIn = fadeIn(
        animationSpec = tween(
            durationMillis = FADE_DURATION,
            easing = FastOutSlowInEasing
        )
    )
    
    val fadeOut = fadeOut(
        animationSpec = tween(
            durationMillis = FADE_DURATION,
            easing = FastOutSlowInEasing
        )
    )
    
    // Combined animations for better UX
    val slideAndFadeIn = slideInFromRight + fadeIn
    val slideAndFadeOut = slideOutToLeft + fadeOut
    val slideBackAndFadeIn = slideInFromLeft + fadeIn  
    val slideBackAndFadeOut = slideOutToRight + fadeOut
}

/**
 * Extension functions to easily apply animations to composable destinations
 */
fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultEnterTransition() =
    NavigationAnimations.slideAndFadeIn

fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultExitTransition() =
    NavigationAnimations.slideAndFadeOut

fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultPopEnterTransition() =
    NavigationAnimations.slideBackAndFadeIn

fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultPopExitTransition() =
    NavigationAnimations.slideBackAndFadeOut