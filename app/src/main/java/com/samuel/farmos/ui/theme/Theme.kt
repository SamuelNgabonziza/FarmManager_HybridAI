package com.samuel.farmos.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = FarmGreenDark,
    onPrimary = Color(0xFF143820),
    primaryContainer = FarmGreenContainerDark,
    onPrimaryContainer = FarmGreenDark,
    secondary = FarmTealDark,
    onSecondary = Color(0xFF003730),
    secondaryContainer = FarmTealContainerDark,
    onSecondaryContainer = FarmTealDark,
    tertiary = FarmAmberDark,
    onTertiary = Color(0xFF332308),
    tertiaryContainer = FarmAmberContainerDark,
    onTertiaryContainer = FarmAmberDark,
    background = FarmDarkBackground,
    onBackground = FarmDarkOnBackground,
    surface = FarmDarkSurface,
    onSurface = FarmDarkOnSurface,
    surfaceVariant = FarmDarkSurfaceVariant,
    onSurfaceVariant = FarmDarkOnSurfaceVariant,
    outline = Color(0xFF4D6654),
    outlineVariant = Color(0xFF384B3D),
    error = FarmCoralDark,
    onError = Color(0xFF520014),
    errorContainer = Color(0xFF5C3036),
    onErrorContainer = Color(0xFFFFDADF)
)

private val LightColorScheme = lightColorScheme(
    primary = FarmGreen,
    onPrimary = Color.White,
    primaryContainer = FarmGreenContainer,
    onPrimaryContainer = Color(0xFF002103),
    secondary = FarmTeal,
    onSecondary = Color.White,
    secondaryContainer = FarmTealContainer,
    onSecondaryContainer = Color(0xFF00201B),
    tertiary = FarmAmber,
    onTertiary = Color.White,
    tertiaryContainer = FarmAmberContainer,
    onTertiaryContainer = Color(0xFF261900),
    background = FarmLightBackground,
    onBackground = FarmLightOnBackground,
    surface = FarmLightSurface,
    onSurface = FarmLightOnSurface,
    surfaceVariant = FarmLightSurfaceVariant,
    onSurfaceVariant = FarmLightOnSurfaceVariant,
    outline = Color(0xFF86968A),
    outlineVariant = Color(0xFFD8E6DA),
    error = FarmCoral,
    onError = Color.White,
    errorContainer = Color(0xFFFCE1E4),
    onErrorContainer = Color(0xFF4C1821)
)

@Composable
fun FarmOsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
