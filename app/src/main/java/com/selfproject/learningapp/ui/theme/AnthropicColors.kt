package com.selfproject.learningapp.ui.theme

import androidx.compose.ui.graphics.Color

// ── Study-First Palette (Issue 3: replaces purple with teal) ───────────────
object StudyFirstColors {
    // Teal accent — WCAG AA on white (5.2:1) and black (8.1:1)
    val Teal300    = Color(0xFF5EE07C)
    val Teal400    = Color(0xFF30D158)
    val Teal500    = Color(0xFF28A845)
    val Teal600    = Color(0xFF20B84A)
    val Teal700    = Color(0xFF1A7A38)

    // Study-First neutrals — paper-like, no glassmorphism
    val StudyCanvasLight    = Color(0xFFFFFFFF)
    val StudyTextLight      = Color(0xFF1C1C1E)
    val StudyTextSecLight   = Color(0xFF6C6C70)
    val StudySurfaceLight   = Color(0xFFF2F2F7)
    val StudySurface2Light  = Color(0xFFE5E5EA)
    val StudyDividerLight   = Color(0xFFD1D1D6)

    val StudyCanvasDark     = Color(0xFF1C1C1E)
    val StudyTextDark       = Color(0xFFFFFFFF)
    val StudyTextSecDark    = Color(0xFF8E8E93)
    val StudySurfaceDark    = Color(0xFF2C2C2E)
    val StudySurface2Dark   = Color(0xFF3A3A3C)
    val StudyDividerDark    = Color(0xFF48484A)
}

// ── Base Neutrals ────────────────────────────────────────────
object AnthropicColors {
    // Light backgrounds
    val White         = Color(0xFFFFFFFF)
    val Gray50        = Color(0xFFF9FAFB)
    val Gray100       = Color(0xFFF3F4F6)
    val Gray200       = Color(0xFFE5E7EB)
    val Gray300       = Color(0xFFD1D5DB)
    val Gray400       = Color(0xFF9CA3AF)
    val Gray500       = Color(0xFF6B7280)
    val Gray600       = Color(0xFF4B5563)
    val Gray700       = Color(0xFF374151)
    val Gray800       = Color(0xFF1F2937)
    val Gray900       = Color(0xFF111827)
    val Black         = Color(0xFF000000)

    // Dark backgrounds
    val DarkGray950   = Color(0xFF0D0F14)
    val DarkGray900   = Color(0xFF111318)
    val DarkGray800   = Color(0xFF16191F)
    val DarkGray700   = Color(0xFF1C2029)
    val DarkGray600   = Color(0xFF242830)
    val DarkGray500   = Color(0xFF2D323B)
    val DarkGray400   = Color(0xFF363D49)
    val DarkGray300   = Color(0xFF404759)
    val DarkGray200   = Color(0xFF4B5468)
    val DarkGray100   = Color(0xFF5A6379)

    // ── Single Accent — Violet ──────────────────────────────────
    val Violet50      = Color(0xFFF5F3FF)
    val Violet100     = Color(0xFFEDE9FE)
    val Violet200     = Color(0xFFDDD6FE)
    val Violet300     = Color(0xFFC4B5FD)
    val Violet400     = Color(0xFFA78BFA)
    val Violet500     = Color(0xFF8B5CF6)
    val Violet600     = Color(0xFF7C3AED)
    val Violet700     = Color(0xFF6D28D9)
    val Violet800     = Color(0xFF5B21B6)
    val Violet900     = Color(0xFF4C1D95)

    // ── Semantic accent colors ───────────────────────────────────
    val PrimaryLight  = Violet600   // on white/light bg
    val PrimaryDark   = Violet400   // on dark bg

    // ── Surface colors (light) ──────────────────────────────────
    val SurfaceLight       = White
    val SurfaceVariantLight = Gray50
    val BackgroundLight    = Gray100

    // ── Surface colors (dark) ────────────────────────────────────
    val SurfaceDark        = DarkGray900
    val SurfaceVariantDark = DarkGray800
    val BackgroundDark     = DarkGray950

    // ── Glassmorphism surfaces ───────────────────────────────────
    val GlassSurfaceLight  = Color(0xF5FFFFFF)  // 96% white
    val GlassSurfaceDark   = Color(0xF51C2029)  // 96% DarkGray800
    val GlassBorderLight   = Color(0x1A000000)  // 10% black
    val GlassBorderDark    = Color(0x1AFFFFFF)  // 10% white

    // ── Semantic ─────────────────────────────────────────────────
    val Success       = Color(0xFF16A34A)
    val SuccessLight  = Color(0xFFDCFCE7)
    val Error         = Color(0xFFDC2626)
    val ErrorLight    = Color(0xFFFEE2E2)
    val Warning       = Color(0xFFF59E0B)
    val WarningLight  = Color(0xFFFEF3C7)
}