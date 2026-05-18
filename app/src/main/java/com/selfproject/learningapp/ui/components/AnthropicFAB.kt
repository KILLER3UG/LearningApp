package com.selfproject.learningapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.selfproject.learningapp.ui.theme.StudyNotesAnimations

/**
 * Animated FAB that morphs between Send and Stop icons.
 * Used in the AI chat for starting/stopping streaming responses.
 */
@Composable
fun AnthropicFAB(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                val halfDuration = StudyNotesAnimations.DurationNormal / 2
                (fadeIn(
                    animationSpec = tween(halfDuration)
                ) + scaleIn(
                    initialScale = 0.7f,
                    animationSpec = tween(halfDuration)
                )) togetherWith (fadeOut(
                    animationSpec = tween(StudyNotesAnimations.DurationFast / 2)
                ) + scaleOut(
                    targetScale = 0.7f,
                    animationSpec = tween(StudyNotesAnimations.DurationFast / 2)
                ))
            },
            label = "fab_icon_swap"
        ) { loading ->
            Icon(
                imageVector = if (loading) Icons.Outlined.Stop else Icons.AutoMirrored.Filled.Send,
                contentDescription = if (loading) "Stop" else "Send",
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        scaleX = if (loading) 0.9f else 1f
                        scaleY = if (loading) 0.9f else 1f
                    }
            )
        }
    }
}
