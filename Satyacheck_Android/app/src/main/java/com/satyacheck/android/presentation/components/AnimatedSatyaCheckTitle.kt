package com.satyacheck.android.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

/**
 * A modern, minimalist animated title that displays "SatyaCheck" with a verification pulse animation
 * that aligns with the app's misinformation-checking motive.
 */
@Composable
fun AnimatedSatyaCheckTitle() {
    // Define colors that reflect truth verification: 
    // A trustworthy teal that represents accuracy and reliability
    val trustedTeal = Color(0xFF00ACC1) // Cyan 600
    
    // Define a subtle animation for the title
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse Animation")
    
    // Pulse animation for the verification indicator
    val pulseAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse"
    )
    
    // Secondary color animation
    val accentAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Accent"
    )
    
    // Create a row to hold the verification symbol and text
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        // Verification checkmark/pulse animation
        Box(
            modifier = Modifier
                .size(16.dp)
                .padding(end = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing circle behind the checkmark
            Canvas(modifier = Modifier.size(16.dp)) {
                // Draw outer pulsing circle
                drawCircle(
                    color = trustedTeal.copy(alpha = (0.2f * pulseAnimation).coerceIn(0f, 0.2f)),
                    radius = size.minDimension / 2f * (0.8f + (pulseAnimation * 0.2f))
                )
                
                // Draw inner solid circle
                drawCircle(
                    color = trustedTeal,
                    radius = size.minDimension / 4f
                )
            }
        }
        
        Spacer(modifier = Modifier.width(4.dp))
        
        // Build the text with semantic coloring
        val annotatedString = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            ) {
                append("Satya")
            }
            withStyle(
                style = SpanStyle(
                    color = trustedTeal.copy(alpha = accentAlpha),
                    fontWeight = FontWeight.Bold
                )
            ) {
                append("Check")
            }
        }
        
        // Display the animated text
        Text(
            text = annotatedString,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleLarge
        )
    }
}
