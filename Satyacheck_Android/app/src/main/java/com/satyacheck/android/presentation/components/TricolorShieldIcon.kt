package com.satyacheck.android.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.satyacheck.android.utils.AnimationSpecs

@Composable
fun TricolorShieldIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    pulsing: Boolean = false
) {
    val canvasModifier = if (pulsing) {
        // Define optimized infinite pulsing animation
        val infiniteTransition = rememberInfiniteTransition(label = "ShieldPulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1200,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "ShieldScale"
        )
        
        modifier.size(24.dp).scale(scale)
    } else {
        modifier.size(24.dp)
    }
    
    // Remember the path for better performance - avoid recreating on every composition
    val shieldPath = remember {
        Path().apply {
            // Shield path creation - moved to remember for performance
            val width = 24.dp.value * 3f // Approximation for path scaling
            val height = 24.dp.value * 3f
            
            moveTo(width / 2, 0f) // Top center
            lineTo(0f, height / 4) // Top left
            lineTo(0f, height * 3 / 4) // Bottom left
            quadraticBezierTo(
                width / 4, height,
                width / 2, height * 0.9f
            ) // Bottom curve left
            quadraticBezierTo(
                width * 3 / 4, height,
                width, height * 3 / 4
            ) // Bottom curve right
            lineTo(width, height / 4) // Top right
            close() // Back to top center
        }
    }
    
    Canvas(modifier = canvasModifier) {
        val width = size.width
        val height = size.height
        
        // Shield outline
        val shieldPath = Path().apply {
            moveTo(width / 2, 0f) // Top center
            lineTo(0f, height / 4) // Top left
            lineTo(0f, height * 3 / 4) // Bottom left
            quadraticBezierTo(
                width / 2, height,
                width, height * 3 / 4
            ) // Bottom curve
            lineTo(width, height / 4) // Bottom right to top right
            close() // Back to top center
        }
        
        // Draw the tricolor sections
        // Saffron (top third)
        drawRect(
            color = Color(0xFFFF9933),
            topLeft = Offset(0f, 0f),
            size = Size(width, height / 3)
        )
        
        // White (middle third)
        drawRect(
            color = Color.White,
            topLeft = Offset(0f, height / 3),
            size = Size(width, height / 3)
        )
        
        // Green (bottom third)
        drawRect(
            color = Color(0xFF138808),
            topLeft = Offset(0f, height * 2 / 3),
            size = Size(width, height / 3)
        )
        
        // Draw the shield outline
        drawPath(
            path = shieldPath,
            color = Color(0xFF444444), // Darker grey as requested
            style = Stroke(width = width / 12)
        )
        
        // Draw a navy blue checkmark in the middle
        val checkPath = Path().apply {
            moveTo(width * 0.3f, height * 0.5f) // Start at left
            lineTo(width * 0.45f, height * 0.65f) // Down to bottom
            lineTo(width * 0.7f, height * 0.35f) // Up to right
        }
        
        drawPath(
            path = checkPath,
            color = Color(0xFF000080), // Navy blue
            style = Stroke(width = width / 8)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TricolorShieldIconPreview() {
    TricolorShieldIcon(modifier = Modifier.size(48.dp))
}

@Preview(showBackground = true)
@Composable
fun TricolorShieldIconPulsingPreview() {
    TricolorShieldIcon(modifier = Modifier.size(48.dp), pulsing = true)
}
