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
    onPrimary = Color(0xFF00390A),
    primaryContainer = FarmGreenContainerDark,
    onPrimaryContainer = FarmGreenContainer,
    secondary = FarmTealDark,
    onSecondary = Color(0xFF003730),
    secondaryContainer = FarmTealContainerDark,
    onSecondaryContainer = FarmTealContainer,
    tertiary = FarmAmberDark,
    onTertiary = Color(0xFF452B00),
    tertiaryContainer = FarmAmberContainerDark,
    onTertiaryContainer = FarmAmberContainer,
    background = FarmDarkBackground,
    onBackground = FarmDarkOnBackground,
    surface = FarmDarkSurface,
    onSurface = FarmDarkOnSurface,
    surfaceVariant = FarmDarkSurfaceVariant,
    onSurfaceVariant = FarmDarkOnSurfaceVariant
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
    onSurfaceVariant = FarmLightOnSurfaceVariant
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
