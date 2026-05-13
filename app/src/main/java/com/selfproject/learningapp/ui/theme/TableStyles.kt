package com.selfproject.learningapp.ui.theme

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared table layout constants — used by both MarkdownViewer table rendering
 * and any future table composables.
 *
 * Issue 2 fixes:
 * - cellPadding: 12dp vertical, 16dp horizontal (per spec)
 * - borderWidth: 1dp hairline (no double borders)
 * - horizontalScroll on outer container for mobile overflow-x
 */
object TableStyles {
    val cellPaddingHorizontal: Dp = 16.dp
    val cellPaddingVertical: Dp = 12.dp
    val borderWidth: Dp = 1.dp
    val headerTextWeight: FontWeight = FontWeight.SemiBold
}