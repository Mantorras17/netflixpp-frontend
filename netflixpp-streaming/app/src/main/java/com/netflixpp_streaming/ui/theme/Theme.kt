package com.netflixpp_streaming.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ⬇️ CORES PERSONALIZADAS NETFLIX++
private val NetflixDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE50914), // Netflix Red
    secondary = Color(0xFF141414), // Netflix Dark
    tertiary = Color(0xFF2D2D2D), // Netflix Gray
    background = Color(0xFF000000), // Black
    surface = Color(0xFF141414), // Dark Surface
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val NetflixLightColorScheme = lightColorScheme(
    primary = Color(0xFFE50914), // Netflix Red
    secondary = Color(0xFFF5F5F5), // Light Gray
    tertiary = Color(0xFFE0E0E0), // Light Surface
    background = Color.White,
    surface = Color(0xFFF5F5F5),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun NetflixppstreamingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // ⬅️ Desabilitado para manter cores Netflix
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> NetflixDarkColorScheme
        else -> NetflixLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NetflixTypography, // ⬅️ Nome corrigido
        content = content
    )
}