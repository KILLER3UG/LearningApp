package com.selfproject.learningapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.selfproject.learningapp.ui.theme.StudyNotesAnimations

/**
 * Animated thinking indicator with 3 bouncing dots.
 * Each dot scales with a staggered delay for a wave effect.
 */
@Composable
fun ThinkingIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "typing_dot_$index")
            val scale by infiniteTransition.animateFloat(
                initialValue = StudyNotesAnimations.TypingDotScaleMin,
                targetValue = StudyNotesAnimations.TypingDotScaleMax,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = StudyNotesAnimations.TypingDotDuration,
                        easing = StudyNotesAnimations.TypingDotEasing
                    ),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(
                        StudyNotesAnimations.TypingDotStaggerDelay * (index + 1)
                    )
                ),
                label = "dot_scale_$index"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size((8 * scale).dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
            )
        }
    }
}

/**
 * Full-width thinking block with animation.
 */
@Composable
fun ThinkingBlock(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            animationSpec = tween(
                StudyNotesAnimations.ThinkExpandDuration,
                easing = StudyNotesAnimations.ThinkExpandEasing
            )
        ) + fadeIn(
            animationSpec = tween(StudyNotesAnimations.DurationNormal)
        ),
        exit = slideOutVertically(
            animationSpec = tween(
                StudyNotesAnimations.ThinkShrinkDuration,
                easing = StudyNotesAnimations.ThinkShrinkEasing
            )
        ) + fadeOut(
            animationSpec = tween(StudyNotesAnimations.DurationFast)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Thinking...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            ThinkingIndicator()
        }
    }
}
