package com.neobuk.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeoBukDarkTeal,
    secondary = NeoBukCyan,
    tertiary = Pink80,
    background = NeoBukDarkBackground,
    surface = NeoBukDarkSurface,
    onPrimary = Color.Black, // Dark Teal needs dark text for contrast
    onSecondary = Color.Black,
    onBackground = NeoBukDarkTextPrimary,
    onSurface = NeoBukDarkTextPrimary,
    onSurfaceVariant = NeoBukDarkTextSecondary,
    outline = Color(0xFF374151) // Gray 700 for borders
)

private val LightColorScheme = lightColorScheme(
    primary = NeoBukTeal,
    secondary = NeoBukCyan,
    tertiary = Pink40,
    
    background = NeoBukBackground,
    surface = NeoBukSurface,
    
    onPrimary = Color.White,
    onSecondary = NeoBukTextPrimary, // Cyan is light, so dark text is better
    onTertiary = Color.White,
    
    onBackground = NeoBukTextPrimary,
    onSurface = NeoBukTextPrimary,
    onSurfaceVariant = NeoBukTextSecondary,
    
    error = NeoBukError
)

@Composable
fun NeoBukTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // We default to false here to enforce the brand colors provided by the user
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
