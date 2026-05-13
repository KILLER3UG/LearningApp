package com.selfproject.learningapp.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object StudyNotesAnimations {
    val DurationInstant  = 50
    val DurationFastest = 100
    val DurationFast    = 150
    val DurationNormal  = 250
    val DurationSlow    = 350
    val DurationSlower  = 500
    val DurationSlowest = 800

    val EasingDecelerate: Easing = CubicBezierEasing(0.4f, 0f, 1f, 1f)
    val EasingAccelerate: Easing = CubicBezierEasing(1f, 0f, 0.8f, 0f)
    val EasingStandard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EasingEmphasis: Easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
    val EasingLinear: Easing = LinearEasing

    val TypingDotDuration     = 600
    val TypingDotScaleMin     = 0.6f
    val TypingDotScaleMax     = 1.2f
    val TypingDotStaggerDelay = 150
    val TypingDotEasing       = FastOutSlowInEasing

    val ThinkExpandDuration = DurationSlow
    val ThinkShrinkDuration = DurationFast
    val ThinkExpandEasing   = EasingDecelerate
    val ThinkShrinkEasing   = EasingAccelerate

    val EntranceStaggerDelay = 50
    val EntranceFadeEasing   = EasingDecelerate
    val EntranceSlideDist    = 8.dp

    val FabIconDuration = DurationNormal
    val FabIconEasing  = EasingEmphasis

    val SheetSlideDuration = DurationSlow
    val SheetSlideEasing   = EasingStandard

    val CardPressDuration  = DurationFast
    val CardScalePressed   = 0.98f
    val CardElevationPress = 2.dp

    val ShimmerDuration = DurationSlowest
    val ShimmerEasing    = EasingLinear
}

// Reusable animation specs
fun ExpandVerticallySpec() = slideInVertically(
    animationSpec = tween(StudyNotesAnimations.ThinkExpandDuration, easing = StudyNotesAnimations.ThinkExpandEasing)
) + fadeIn(
    animationSpec = tween(StudyNotesAnimations.DurationNormal, easing = StudyNotesAnimations.EntranceFadeEasing)
)

fun ShrinkVerticallySpec() = slideOutVertically(
    animationSpec = tween(StudyNotesAnimations.ThinkShrinkDuration, easing = StudyNotesAnimations.ThinkShrinkEasing)
) + fadeOut(
    animationSpec = tween(StudyNotesAnimations.DurationFast, easing = StudyNotesAnimations.EasingAccelerate)
)
