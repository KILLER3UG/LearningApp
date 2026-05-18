package com.selfproject.learningapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Table rendering theme — shared across MarkdownViewer and MarkdownRenderer.
 *
 * Issue 2: Provides theme-aware colors for table rendering.
 * Header gets a distinct neutral background.
 * No zebra striping — clean hairline borders only.
 */
data class TableTheme(
    val headerBackground: Color,
    val cellBorder: Color,
    val textColor: Color,
    val headerTextWeight: FontWeight = FontWeight.SemiBold,
    val cellPaddingHorizontal: Dp = 16.dp,
    val cellPaddingVertical: Dp = 12.dp
)

@Composable
fun tableTheme(): TableTheme {
    val colors = MaterialTheme.colorScheme
    return TableTheme(
        headerBackground = colors.surfaceVariant.copy(alpha = 0.72f),
        cellBorder = colors.outlineVariant,
        textColor = colors.onSurface
    )
}
