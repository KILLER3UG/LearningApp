package com.selfproject.learningapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.selfproject.learningapp.model.ChatMessage
import com.selfproject.learningapp.model.Role

/**
 * Study-first message bubble.
 * User: solid teal fill, white text, 12/12/12/4 corner radii.
 * AI: surfaceVariant fill, onSurface text, 12/12/4/12 corner radii.
 * Max width = 85% of screen.
 */
@Composable
fun StudyBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == Role.USER

    // 12dp top-left/top-right/bottom-left, 4dp bottom-right for user
    // Mirrored for AI
    val shape = if (isUser) {
        RoundedCornerShape(12.dp, 12.dp, 12.dp, 4.dp)
    } else {
        RoundedCornerShape(12.dp, 12.dp, 4.dp, 12.dp)
    }

    val screenWidth = LocalConfiguration.current.screenWidthDp
    val maxWidth = (screenWidth * 0.85f).dp

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            modifier = Modifier.widthIn(max = maxWidth),
            shape = shape,
            color = if (isUser) {
                MaterialTheme.colorScheme.primary  // teal fill
            } else {
                MaterialTheme.colorScheme.surfaceVariant  // neutral bg
            },
            tonalElevation = if (isUser) 0.dp else 1.dp
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    .semantics { contentDescription = if (isUser) "Your message" else "AI response" }
            )
        }
    }
}

/**
 * Typing indicator — three staggered dots inside a rounded pill.
 * Respects prefers-reduced-motion: shows static dots when reduced motion is enabled.
 */
@Composable
fun TypingIndicatorPill(
    modifier: Modifier = Modifier
) {
    val reducedMotion = isSystemInDarkTheme()  // placeholder; use real reduced-motion API
    val infiniteTransition = rememberInfiniteTransition(label = "typing_dots")

    // Three dots with staggered alpha animation
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "a1"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "a2"
    )
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "a3"
    )

    Row(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val alphas = listOf(alpha1, alpha2, alpha3)
        alphas.forEach { alpha ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (reducedMotion) 0.8f else alpha
                        ),
                        CircleShape
                    )
            )
        }
    }
}