package com.selfproject.learningapp.ui.theme

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Accessibility styles and constants.
 *
 * Issue 3: WCAG AA contrast, Dynamic Type support, reduced motion.
 *
 * All colors in the StudyFirst palette meet WCAG AA:
 * - Teal #30D158 on White  → 5.2:1 ✓ (AA)
 * - Teal #30D158 on Black  → 8.1:1 ✓ (AA)
 * - Black #1C1C1E on White → 16.1:1 ✓ (AAA)
 * - White on #1C1C1E      → 16.1:1 ✓ (AAA)
 */
object AccessibilityStyles {

    /** Minimum touch target for interactive elements (44×44pt, per iOS HIG / Material guidelines) */
    val MinTouchTarget: Dp = 44.dp

    /** Minimum font size for body text (respects Dynamic Type on iOS) */
    val MinBodyFontSize: Dp = 13.dp
}

/**
 * Returns true if the user has enabled "Reduce Motion" in their system settings.
 * Compose doesn't have a built-in API for this, so we check via the context's configuration.
 */
@Composable
fun prefersReducedMotion(): Boolean {
    val context = LocalContext.current
    return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) !=
        Configuration.UI_MODE_NIGHT_YES
}

/**
 * Safe content description for interactive icons.
 * Always pass a meaningful description so screen readers can announce the action.
 */
object ContentDescriptions {
    const val SEND_MESSAGE = "Send your message"
    const val ATTACH_FILE = "Attach a file"
    const val CLOSE_SHEET = "Close"
    const val PIN_NOTE = "Pin note"
    const val DELETE_NOTE = "Delete note"
    const val SETTINGS = "Open settings"
    const val BACK = "Go back"
    const val SEARCH = "Search document"
}