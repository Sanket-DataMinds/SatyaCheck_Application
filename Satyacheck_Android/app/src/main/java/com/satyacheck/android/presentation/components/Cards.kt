package com.satyacheck.android.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A Google-style card component with proper elevation, rounded corners, and padding
 * 
 * @param modifier Modifier to be applied to the card
 * @param onClick Optional click handler for the card
 * @param containerColor Background color of the card
 * @param contentColor Content color of the card
 * @param elevation Elevation of the card in dp
 * @param content Content to be displayed inside the card
 */
@Composable
fun GoogleCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Int = 2,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick() } else it },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation.dp
        )
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
        ) {
            content()
        }
    }
}

/**
 * A section card that groups related content with a subtle background and rounded corners
 */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

/**
 * A feature card that highlights important actions or information with more prominent styling
 */
@Composable
fun FeatureCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .padding(20.dp)
                .heightIn(min = 100.dp)
                .clickable { onClick() }
        ) {
            content()
        }
    }
}
