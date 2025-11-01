package com.satyacheck.android.utils

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.animation.core.AnimationSpec

/**
 * Performance-optimized animation specifications for consistent, smooth animations
 * across the app. These follow Material Design motion principles.
 */
object AnimationSpecs {
    
    // Standard durations following Material Design
    const val DURATION_SHORT = 150
    const val DURATION_MEDIUM = 300
    const val DURATION_LONG = 500
    
    // Standard easing curves
    val fastOutSlowIn = FastOutSlowInEasing
    
    // Optimized spring animations for different use cases
    val fastSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val smoothSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    
    val gentleSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    // Standard tween animations
    val shortTween = tween<Float>(
        durationMillis = DURATION_SHORT,
        easing = fastOutSlowIn
    )
    
    val mediumTween = tween<Float>(
        durationMillis = DURATION_MEDIUM,
        easing = fastOutSlowIn
    )
    
    val longTween = tween<Float>(
        durationMillis = DURATION_LONG,
        easing = fastOutSlowIn
    )
}

/**
 * Memory-efficient composable modifier that remembers animation specs
 */
@Composable
fun rememberOptimizedAnimationSpec(
    type: AnimationType = AnimationType.MEDIUM_TWEEN
): AnimationSpec<Float> {
    return remember(type) {
        when (type) {
            AnimationType.FAST_SPRING -> AnimationSpecs.fastSpring
            AnimationType.SMOOTH_SPRING -> AnimationSpecs.smoothSpring
            AnimationType.GENTLE_SPRING -> AnimationSpecs.gentleSpring
            AnimationType.SHORT_TWEEN -> AnimationSpecs.shortTween
            AnimationType.MEDIUM_TWEEN -> AnimationSpecs.mediumTween
            AnimationType.LONG_TWEEN -> AnimationSpecs.longTween
        }
    }
}

enum class AnimationType {
    FAST_SPRING,
    SMOOTH_SPRING,
    GENTLE_SPRING,
    SHORT_TWEEN,
    MEDIUM_TWEEN,
    LONG_TWEEN
}