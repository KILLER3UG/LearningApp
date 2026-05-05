package com.selfproject.learningapp.model

import androidx.compose.ui.graphics.Color

/**
 * Highlight range for highlighting text in the document viewer.
 */
data class HighlightRange(
    val start: Int,
    val end: Int,
    val color: Color,
)
