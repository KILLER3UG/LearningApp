package com.selfproject.learningapp.ui.theme

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Accessibility styles and constants.
 *
 * Accessibility styles and constants for touch targets, readable text, and motion.
 */
object AccessibilityStyles {

    /** Minimum touch target for interactive elements. */
    val MinTouchTarget: Dp = 44.dp

    /** Minimum font size for body text. */
    val MinBodyFontSize: Dp = 13.dp
}

/**
 * Returns true if the user has disabled Android animator duration scale.
 */
@Composable
fun prefersReducedMotion(): Boolean {
    val context = LocalContext.current
    return runCatching {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }.getOrDefault(false)
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
