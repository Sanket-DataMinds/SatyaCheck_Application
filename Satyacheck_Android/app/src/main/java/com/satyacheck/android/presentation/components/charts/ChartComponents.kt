package com.satyacheck.android.presentation.components.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

/**
 * A bar chart component for displaying weekly data
 */
@Composable
fun BarChart(
    data: Map<String, Int>,
    title: String,
    modifier: Modifier = Modifier
) {
    var animated by remember { mutableStateOf(false) }
    val maxValue = data.values.maxOrNull() ?: 1
    
    LaunchedEffect(key1 = data) {
        animated = true
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { (label, value) ->
                    val animatedHeight by animateFloatAsState(
                        targetValue = if (animated) (value.toFloat() / maxValue) else 0f,
                        animationSpec = tween(durationMillis = 1000, delayMillis = 100),
                        label = "Bar height animation"
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .height(120.dp)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height((100.dp * animatedHeight))
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f + (0.3f * animatedHeight)))
                            )
                        }
                        
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(36.dp)
                        )
                        
                        Text(
                            text = "$value",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * A pie chart component for displaying categorical data
 */
@Composable
fun PieChart(
    data: Map<String, Int>,
    title: String,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        Color(0xFF5C6BC0), // Indigo 400
        Color(0xFF26A69A), // Teal 400
        Color(0xFFEF5350), // Red 400
        Color(0xFFFFA726), // Orange 400
        Color(0xFF66BB6A), // Green 400
        Color(0xFF42A5F5), // Blue 400
        Color(0xFFAB47BC), // Purple 400
        Color(0xFF8D6E63)  // Brown 400
    )
) {
    var animated by remember { mutableStateOf(false) }
    val total = data.values.sum().toFloat().coerceAtLeast(1f)
    
    // Calculate sweep angles for each category
    val sweepAngles = data.values.map { it / total * 360f }
    
    LaunchedEffect(key1 = data) {
        animated = true
    }
    
    val animatedFraction by animateFloatAsState(
        targetValue = if (animated) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "Pie chart animation"
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                // Get the surface color outside of the Canvas scope
                val surfaceColor = MaterialTheme.colorScheme.surface
                
                Canvas(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(8.dp)
                ) {
                    var startAngle = -90f
                    
                    // Draw the pie chart segments
                    data.values.forEachIndexed { index, _ ->
                        val sweepAngle = sweepAngles[index] * animatedFraction
                        drawArc(
                            color = colors[index % colors.size],
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            size = Size(size.width, size.height)
                        )
                        startAngle += sweepAngle
                    }
                    
                    // Draw a white circle in the middle for a donut chart effect
                    drawCircle(
                        color = surfaceColor,
                        radius = size.minDimension * 0.25f
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Legend
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                data.entries.toList().forEachIndexed { index, (category, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = colors[index % colors.size],
                                    shape = CircleShape
                                )
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = "$value (${(value / total * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * A stat card component for displaying a single statistic with label
 */
@Composable
fun StatCard(
    value: String,
    label: String,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * A progress ring component for displaying percentage-based statistics
 */
@Composable
fun ProgressRing(
    percentage: Float,
    label: String,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    var animated by remember { mutableStateOf(false) }
    
    LaunchedEffect(key1 = percentage) {
        animated = true
    }
    
    val animatedPercentage by animateFloatAsState(
        targetValue = if (animated) percentage else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "Progress ring animation"
    )
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(80.dp)) {
                    // Background circle
                    drawCircle(
                        color = color.copy(alpha = 0.2f),
                        radius = size.minDimension / 2f,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                    
                    // Progress arc
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedPercentage,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                
                Text(
                    text = "${(animatedPercentage * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
