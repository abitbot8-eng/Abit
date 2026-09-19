package com.nova.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class Accent(val name: String, val primary: Color, val secondary: Color)

val Accents = listOf(
    Accent("Cyan", Color(0xFF22D3EE), Color(0xFF6366F1)),
    Accent("Violet", Color(0xFFA78BFA), Color(0xFFEC4899)),
    Accent("Emerald", Color(0xFF34D399), Color(0xFF22D3EE)),
    Accent("Sunset", Color(0xFFFB923C), Color(0xFFF43F5E)),
)

val TouchGreen = Color(0xFF39FF14)
val LocalAnimations = compositionLocalOf { true }

private val NovaTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 32.sp, letterSpacing = (-0.6).sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 13.sp, letterSpacing = 0.4.sp),
)

@Composable
fun NovaTheme(settings: SettingsStore, content: @Composable () -> Unit) {
    val a = Accents[settings.accent.coerceIn(0, Accents.lastIndex)]
    val bg = if (settings.amoled) Color(0xFF000000) else Color(0xFF0B1020)
    val scheme = darkColorScheme(
        primary = a.primary,
        secondary = a.secondary,
        tertiary = TouchGreen,
        onPrimary = Color(0xFF04121A),
        background = bg,
        surface = Color(0xFF111827),
        onBackground = Color(0xFFE6EDF7),
        onSurface = Color(0xFFE6EDF7),
        surfaceVariant = Color(0xFF1B2437),
        onSurfaceVariant = Color(0xFF9AA7BD),
    )
    MaterialTheme(colorScheme = scheme, typography = NovaTypography, content = content)
}
