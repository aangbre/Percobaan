package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SunnyOrangeDark,
    onPrimary = Color.White,
    primaryContainer = SunnyYellow,
    onPrimaryContainer = TextDark,
    secondary = GrassGreen,
    onSecondary = Color.White,
    secondaryContainer = GrassGreenLight,
    onSecondaryContainer = TextDark,
    tertiary = SkyBlue,
    onTertiary = Color.White,
    background = WarmBackground,
    onBackground = TextDark,
    surface = CardBackground,
    onSurface = TextDark,
    surfaceVariant = SurfaceSoftYellow,
    onSurfaceVariant = TextDark,
    outline = Color(0xFFFFD54F)
)

private val DarkColorScheme = lightColorScheme(
    // Keep kindergarten theme cheerful even in dark mode
    primary = SunnyOrange,
    onPrimary = Color.Black,
    primaryContainer = SunnyOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = GrassGreenLight,
    onSecondary = Color.Black,
    tertiary = SkyBlue,
    background = Color(0xFF231C18),
    onBackground = Color(0xFFFBF8F2),
    surface = Color(0xFF332924),
    onSurface = Color(0xFFFBF8F2)
)

@Composable
fun EduTKTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
