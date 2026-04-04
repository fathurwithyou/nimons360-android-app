package com.eggheadengineers.nimons360.ui.theme

import androidx.compose.ui.graphics.Color

// Primary: black and bold, confident, Uber-grade.
val Primary            = Color(0xFF000000)
val PrimaryDark        = Color(0xFF000000)
val PrimaryLight       = Color(0xFFEAE5DE)   // warm stone for icon containers & active pills
val OnPrimary          = Color(0xFFFFFFFF)
val OnPrimaryContainer = Color(0xFF111111)
val InversePrimary     = Color(0xFF1F1F1F)

// Accent: warm red and strong, intentional, never decorative.
val Accent             = Color(0xFFD14040)
val AccentLight        = Color(0xFFFBECEC)
val OnAccent           = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFFBECEC)
val OnAccentContainer  = Color(0xFF6A1B1B)
val SecondaryFixedDim  = Color(0xFFF5D0D0)

// Tertiary: deep blue and info states, member map markers.
val Tertiary           = Color(0xFF276EF1)
val TertiaryContainer  = Color(0xFFEAF1FF)
val OnTertiary         = Color(0xFFFFFFFF)
val OnTertiaryContainer = Color(0xFF173E87)

// Error: same warm red family, slightly sharper.
val ErrorColor         = Color(0xFFC0362C)
val OnError            = Color(0xFFFFFFFF)
val ErrorContainer     = Color(0xFFFDE8E6)
val OnErrorContainer   = Color(0xFF6A1B14)

// Background & surface: warm off-white, not clinical.
val Background         = Color(0xFFF0ECE6)
val OnBackground       = Color(0xFF111111)

val Surface            = Color(0xFFFFFFFF)
val OnSurface          = Color(0xFF111111)
val OnSurfaceVariant   = Color(0xFF4E4E4E)

val SurfaceContainerLowest  = Color(0xFFFFFFFF)
val SurfaceContainerLow     = Color(0xFFF4F0EA)
val SurfaceContainer        = Color(0xFFE8E3DC)
val SurfaceContainerHigh    = Color(0xFFE1DBD3)
val SurfaceContainerHighest = Color(0xFFDAD4CB)
val SurfaceVariant          = Color(0xFFEEE8E0)

val InverseSurface     = Color(0xFF111111)
val InverseOnSurface   = Color(0xFFFFFFFF)

val Outline            = Color(0xFF6C6C6C)
val OutlineVariant     = Color(0xFFD7D1C8)

val Scrim              = Color(0xFF000000)

// Semantic aliases
val SurfaceSecondary   = SurfaceContainerLow
val SurfaceTertiary    = SurfaceContainer
val TextPrimary        = OnSurface
val TextSecondary      = OnSurfaceVariant
val TextTertiary       = Outline
val Border             = OutlineVariant
val Divider            = SurfaceContainerLow

val Success            = Color(0xFF2AA76B)
val Warning            = Color(0xFFD08B00)
val Info               = Tertiary
