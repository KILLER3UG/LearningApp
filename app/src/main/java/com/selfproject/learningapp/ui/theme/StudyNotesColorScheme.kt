package com.selfproject.learningapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = ColorScheme(
    primary            = StudyFirstColors.Teal500,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFE3F3EC),
    onPrimaryContainer = Color(0xFF083D2E),
    secondary          = Color(0xFF5E6A65),
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFFE8ECE8),
    onSecondaryContainer = StudyFirstColors.StudyTextSecLight,
    tertiary           = StudyFirstColors.Clay600,
    onTertiary         = Color.White,
    tertiaryContainer  = Color(0xFFF4E2D8),
    onTertiaryContainer = Color(0xFF442312),
    error              = Color(0xFFB3261E),
    onError            = Color.White,
    errorContainer     = Color(0xFFFFDAD6),
    onErrorContainer   = Color(0xFF410002),
    background         = StudyFirstColors.StudyCanvasLight,
    onBackground       = StudyFirstColors.StudyTextLight,
    surface            = StudyFirstColors.StudyCanvasLight,
    onSurface          = StudyFirstColors.StudyTextLight,
    surfaceVariant     = StudyFirstColors.StudySurfaceLight,
    onSurfaceVariant   = StudyFirstColors.StudyTextSecLight,
    outline            = StudyFirstColors.StudyDividerLight,
    outlineVariant     = StudyFirstColors.StudySurface2Light,
    scrim              = Color.Black,
    inverseSurface     = StudyFirstColors.StudySurfaceDark,
    inverseOnSurface   = StudyFirstColors.StudyTextDark,
    inversePrimary     = StudyFirstColors.Teal300,
    surfaceTint        = StudyFirstColors.Teal500
)

private val DarkColorScheme = ColorScheme(
    primary            = StudyFirstColors.Teal300,
    onPrimary          = Color(0xFF06251A),
    primaryContainer   = Color(0xFF123D30),
    onPrimaryContainer = Color(0xFFD5F7E9),
    secondary          = Color(0xFFA7ADA8),
    onSecondary        = Color(0xFF171A18),
    secondaryContainer = Color(0xFF2A302B),
    onSecondaryContainer = StudyFirstColors.StudyTextSecDark,
    tertiary           = StudyFirstColors.Clay400,
    onTertiary         = Color(0xFF2A1207),
    tertiaryContainer  = Color(0xFF51301E),
    onTertiaryContainer = Color(0xFFFFE2CF),
    error              = Color(0xFFFF6961),
    onError            = Color.Black,
    errorContainer      = Color(0xFF93000A),
    onErrorContainer    = Color(0xFFFFDAD6),
    background         = StudyFirstColors.StudyCanvasDark,
    onBackground       = StudyFirstColors.StudyTextDark,
    surface            = StudyFirstColors.StudyCanvasDark,
    onSurface          = StudyFirstColors.StudyTextDark,
    surfaceVariant     = StudyFirstColors.StudySurfaceDark,
    onSurfaceVariant   = StudyFirstColors.StudyTextSecDark,
    outline            = StudyFirstColors.StudyDividerDark,
    outlineVariant     = StudyFirstColors.StudySurface2Dark,
    scrim              = Color.Black,
    inverseSurface     = StudyFirstColors.StudyCanvasLight,
    inverseOnSurface   = StudyFirstColors.StudyTextLight,
    inversePrimary     = StudyFirstColors.Teal600,
    surfaceTint        = StudyFirstColors.Teal400
)

@Composable
fun StudyNotesColorScheme(darkTheme: Boolean): ColorScheme =
    if (darkTheme) DarkColorScheme else LightColorScheme
