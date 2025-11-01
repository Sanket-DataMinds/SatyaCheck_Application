package com.satyacheck.android.components.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.satyacheck.android.presentation.theme.ThemeMode

/**
 * A UI component that allows the user to select a theme mode (light, dark, or system).
 */
@Composable
fun ThemeSelector(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "App Theme",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
            ) {
                ThemeOption(
                    icon = Icons.Default.LightMode,
                    title = "Light Theme",
                    selected = currentTheme == ThemeMode.LIGHT,
                    onClick = { onThemeSelected(ThemeMode.LIGHT) }
                )
                
                ThemeOption(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Theme",
                    selected = currentTheme == ThemeMode.DARK,
                    onClick = { onThemeSelected(ThemeMode.DARK) }
                )
                
                ThemeOption(
                    icon = Icons.Default.SettingsBrightness,
                    title = "Follow System",
                    selected = currentTheme == ThemeMode.SYSTEM,
                    onClick = { onThemeSelected(ThemeMode.SYSTEM) }
                )
            }
        }
    }
}

@Composable
private fun ThemeOption(
    icon: ImageVector,
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // null because the parent is already selectable
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
