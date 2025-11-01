package com.satyacheck.android.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * An expandable FAB that shows input options when expanded
 */
@Composable
fun InputFab(
    onTextInput: () -> Unit,
    onCameraInput: () -> Unit,
    onImageInput: () -> Unit,
    onAudioInput: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier.padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Expanded FAB options
        if (expanded) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Text input option
                FabOption(
                    icon = Icons.Default.TextFormat,
                    label = "Text",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = {
                        expanded = false
                        onTextInput()
                    }
                )
                
                // Camera input option
                FabOption(
                    icon = Icons.Default.PhotoCamera,
                    label = "Camera",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = {
                        expanded = false
                        onCameraInput()
                    }
                )
                
                // Image input option
                FabOption(
                    icon = Icons.Default.Image,
                    label = "Gallery",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = {
                        expanded = false
                        onImageInput()
                    }
                )
                
                // Audio input option
                FabOption(
                    icon = Icons.Default.KeyboardVoice,
                    label = "Voice",
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    onClick = {
                        expanded = false
                        onAudioInput()
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Main FAB (close button when expanded)
                FloatingActionButton(
                    onClick = { expanded = false },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Close options",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else {
            // Main FAB (add button when collapsed)
            FloatingActionButton(
                onClick = { expanded = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Show input options",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun FabOption(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(end = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Surface(
            color = containerColor.copy(alpha = 0.9f),
            contentColor = contentColor,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        
        Surface(
            shape = CircleShape,
            color = containerColor,
            contentColor = contentColor,
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
