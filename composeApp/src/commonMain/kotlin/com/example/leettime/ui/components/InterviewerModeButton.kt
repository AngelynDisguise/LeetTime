package com.example.leettime.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun InterviewerModeButton(
    isEnabled: Boolean,
    isAiSpeaking: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animated glow effect when AI is speaking
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val backgroundColor = when {
        isEnabled && isAiSpeaking -> MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha)
        isEnabled -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when {
        isEnabled -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .then(
                if (isEnabled && isAiSpeaking) {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(8.dp),
                        ambientColor = MaterialTheme.colorScheme.primary,
                        spotColor = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Modifier
                }
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Using Star icon as a placeholder for Gemini logo
            // You can replace this with an actual Gemini logo image
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Gemini",
                modifier = Modifier.size(20.dp)
            )
            Text("Interviewer Mode")
        }
    }
}
