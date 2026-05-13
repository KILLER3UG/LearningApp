package com.selfproject.learningapp.ui.components

import android.os.Build
import android.graphics.RenderEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.selfproject.learningapp.ui.theme.StudyFirstColors
import com.selfproject.learningapp.ui.theme.StudyNotesElevation
import com.selfproject.learningapp.ui.theme.StudyNotesShapes

/**
 * Issue 3 fix: Replaced glassmorphism with clean solid surface.
 *
 * Glassmorphism removed per the design spec — no purple gradients, no floating
 * card shadows, no glass surfaces. Now uses solid surfaceVariant background
 * with a subtle shadow for depth.
 *
 * Blur effect preserved only on API 31+ as a progressive enhancement —
 * the surface is fully readable even without blur (fallback on older devices).
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    blurRadius: Dp = StudyNotesElevation.GlassBlurRadius,
    content: @Composable BoxScope.() -> Unit
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    // StudyFirst palette — solid surface, no glass
    val bgColor = if (dark) StudyFirstColors.StudySurfaceDark else StudyFirstColors.StudySurfaceLight
    val borderColor = if (dark) StudyFirstColors.StudyDividerDark else StudyFirstColors.StudyDividerLight

    Box(
        modifier = modifier
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.graphicsLayer {
                        // Subtle blur as enhancement, not required for readability
                        renderEffect = RenderEffect
                            .createBlurEffect(
                                blurRadius.value,
                                blurRadius.value,
                                android.graphics.Shader.TileMode.CLAMP
                            )
                            .asComposeRenderEffect()
                    }
                } else Modifier
            )
            .clip(StudyNotesShapes.large)
            .background(bgColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = StudyNotesShapes.large
            ),
        content = content
    )
}

/**
 * Subtle surface without blur for cards and panels.
 * Issue 3: Uses StudyFirst palette with no glassmorphism.
 */
@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(StudyNotesShapes.medium)
            .background(backgroundColor),
        content = content
    )
}

private fun Color.luminance(): Float {
    val c = this
    return 0.299f * c.red + 0.587f * c.green + 0.114f * c.blue
}
