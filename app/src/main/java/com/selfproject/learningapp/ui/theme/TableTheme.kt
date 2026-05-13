package com.selfproject.learningapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
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
    val isDark = isSystemInDarkTheme()
    return if (isDark) {
        TableTheme(
            headerBackground = Color(0xFF212529),
            cellBorder = Color(0xFF343A40),
            textColor = Color.White
        )
    } else {
        TableTheme(
            headerBackground = Color(0xFFF8F9FA),
            cellBorder = Color(0xFFE9ECEF),
            textColor = Color(0xFF1C1C1E)
        )
    }
}