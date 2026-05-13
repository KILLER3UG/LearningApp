package com.selfproject.learningapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Issue 3: Teal primary (replaces violet), clean paper-like surfaces, no glassmorphism
private val LightColorScheme = ColorScheme(
    primary            = StudyFirstColors.Teal400,    // #30D158
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFB7F4C3),
    onPrimaryContainer = Color(0xFF002200),
    secondary          = StudyFirstColors.StudySurfaceLight,
    onSecondary        = StudyFirstColors.StudyTextLight,
    secondaryContainer = StudyFirstColors.StudySurface2Light,
    onSecondaryContainer = StudyFirstColors.StudyTextSecLight,
    tertiary           = StudyFirstColors.Teal600,
    onTertiary         = Color.White,
    tertiaryContainer  = Color(0xFF90F59C),
    onTertiaryContainer = Color(0xFF003311),
    error              = Color(0xFFFF3B30),
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
    surfaceTint        = StudyFirstColors.Teal400
)

private val DarkColorScheme = ColorScheme(
    primary            = StudyFirstColors.Teal400,    // #30D158
    onPrimary          = Color.Black,
    primaryContainer   = Color(0xFF003311),
    onPrimaryContainer = Color(0xFF90F59C),
    secondary          = StudyFirstColors.StudySurfaceDark,
    onSecondary        = StudyFirstColors.StudyTextDark,
    secondaryContainer = StudyFirstColors.StudySurface2Dark,
    onSecondaryContainer = StudyFirstColors.StudyTextSecDark,
    tertiary           = StudyFirstColors.Teal300,
    onTertiary         = Color.Black,
    tertiaryContainer  = Color(0xFF1A7A38),
    onTertiaryContainer = Color(0xFF5EE07C),
    error              = Color(0xFFFF6961),
    onError            = Color.Black,
    errorContainer      = Color(0xFF93000A),
    onErrorContainer    = Color(0xFFFFDAD6),
    background         = StudyFirstColors.StudyCanvasDark,  // #1C1C1E
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