package com.eggheadengineers.nimons360.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.eggheadengineers.nimons360.R

// Uber Move Text, display, bold, editorial, confident.
val UberMoveTextFamily = FontFamily(
    Font(R.font.uber_move_text_light,   weight = FontWeight.Light),
    Font(R.font.uber_move_text_regular, weight = FontWeight.Normal),
    Font(R.font.uber_move_text_medium,  weight = FontWeight.Medium),
    Font(R.font.uber_move_text_bold,    weight = FontWeight.Bold),
)

// Cal Sans, headline, distinctive geometric display face.
val CalSansFamily = FontFamily(
    Font(R.font.cal_sans_regular, weight = FontWeight.Normal),
    Font(R.font.cal_sans_regular, weight = FontWeight.Medium),
    Font(R.font.cal_sans_regular, weight = FontWeight.SemiBold),
    Font(R.font.cal_sans_regular, weight = FontWeight.Bold),
)

// Manrope, titles and actions, geometric, clear hierarchy.
val ManropeFamily = FontFamily(
    Font(R.font.manrope_regular,  weight = FontWeight.Normal),
    Font(R.font.manrope_medium,   weight = FontWeight.Medium),
    Font(R.font.manrope_semibold, weight = FontWeight.SemiBold),
    Font(R.font.manrope_bold,     weight = FontWeight.Bold),
)

// Geist, body and small labels, crisp, modern, functional.
val GeistFamily = FontFamily(
    Font(R.font.geist_regular,  weight = FontWeight.Normal),
    Font(R.font.geist_medium,   weight = FontWeight.Medium),
    Font(R.font.geist_semibold, weight = FontWeight.SemiBold),
    Font(R.font.geist_bold,     weight = FontWeight.Bold),
)

// Inter, kept for compatibility if needed.
val InterFamily = FontFamily(
    Font(R.font.inter_regular,  weight = FontWeight.Normal),
    Font(R.font.inter_medium,   weight = FontWeight.Medium),
    Font(R.font.inter_semibold, weight = FontWeight.SemiBold),
)

val AppTypography = Typography(
    // Display, hero text, login title, large feature headings.
    //    Uber Move: bold, editorial, sets the brand voice.
    displayLarge = TextStyle(
        fontFamily    = UberMoveTextFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 48.sp,
        lineHeight    = 52.sp,
        letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily    = UberMoveTextFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 40.sp,
        lineHeight    = 44.sp,
        letterSpacing = (-0.3).sp,
    ),
    displaySmall = TextStyle(
        fontFamily    = UberMoveTextFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 32.sp,
        lineHeight    = 36.sp,
        letterSpacing = (-0.2).sp,
    ),

    // Headline, section headers, screen titles, feature names.
    //    Cal Sans: distinctive geometric face, gives personality to mid-level headings.
    headlineLarge = TextStyle(
        fontFamily    = CalSansFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 32.sp,
        lineHeight    = 36.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily    = CalSansFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 26.sp,
        lineHeight    = 30.sp,
        letterSpacing = (-0.15).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily    = CalSansFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 22.sp,
        lineHeight    = 28.sp,
    ),

    // Title, card headers, top bar title, dialog titles.
    //    Manrope: geometric, structured, bridges headline and body.
    titleLarge = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 20.sp,
        lineHeight    = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 17.sp,
        lineHeight    = 24.sp,
    ),
    titleSmall = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 15.sp,
        lineHeight    = 20.sp,
    ),

    // Body, descriptions, supporting text, prose.
    //    Geist: crisp, modern, excellent readability at small sizes.
    bodyLarge = TextStyle(
        fontFamily    = GeistFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 17.sp,
        lineHeight    = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily    = GeistFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 15.sp,
        lineHeight    = 22.sp,
    ),
    bodySmall = TextStyle(
        fontFamily    = GeistFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
    ),

    // Label, buttons use Manrope (labelLarge), smaller labels use Geist.
    labelLarge = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 15.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.sp,
    ),
    labelMedium = TextStyle(
        fontFamily    = GeistFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.sp,
    ),
    labelSmall = TextStyle(
        fontFamily    = GeistFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.sp,
    ),
)

// Section eyebrow style, Cal Sans italic for card section headers.
val SectionEyebrowStyle = TextStyle(
    fontFamily = CalSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontStyle = FontStyle.Italic,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.sp,
)
