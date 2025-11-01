package com.satyacheck.android.presentation.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.satyacheck.android.domain.model.Verdict

@Composable
fun CredibilityBreakdown(
    credibilityData: Map<Verdict, Int>,
    title: String,
    modifier: Modifier = Modifier
) {
    val total = credibilityData.values.sum()
    
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
            
            credibilityData.forEach { (verdict, count) ->
                val percentage = if (total > 0) (count.toFloat() / total) * 100 else 0f
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Verdict indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(getVerdictColor(verdict))
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Verdict label
                    Text(
                        text = verdict.toString().replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Count and percentage
                    Text(
                        text = "$count (${percentage.toInt()}%)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Progress bar
                if (total > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(percentage / 100)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(getVerdictColor(verdict))
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun getVerdictColor(verdict: Verdict): Color {
    return when (verdict) {
        Verdict.CREDIBLE -> Color(0xFF43A047) // Green 600
        Verdict.POTENTIALLY_MISLEADING -> Color(0xFFFFA726) // Orange 400
        Verdict.HIGH_MISINFORMATION_RISK -> Color(0xFFE53935) // Red 600
        Verdict.SCAM_ALERT -> Color(0xFFE53935) // Red 600
        Verdict.UNKNOWN -> Color(0xFF42A5F5) // Blue 400
        else -> MaterialTheme.colorScheme.primary
    }
}
