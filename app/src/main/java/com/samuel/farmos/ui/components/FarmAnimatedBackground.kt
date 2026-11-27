package com.samuel.farmos.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Lightweight animated background with very slow-moving, cloud-like radial mist
 * blending subtle white, emerald green, and soft charcoal tones in both light and dark modes.
 */
@Composable
fun FarmAnimatedBackground(modifier: Modifier = Modifier) {
    val darkMode = isSystemInDarkTheme()
    val transition = rememberInfiniteTransition(label = "farm_clouds")

    val phaseA by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 28_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud_phase_a"
    )
    val phaseB by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 38_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud_phase_b"
    )
    val phaseC by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 46_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud_phase_c"
    )

    val baseGradient = if (darkMode) {
        listOf(Color(0xFF18221D), Color(0xFF213028), Color(0xFF161E1A))
    } else {
        listOf(Color(0xFFF8FCF6), Color(0xFFEEF6EE), Color(0xFFE5F0E6))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(baseGradient))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val greenCloud = if (darkMode) Color(0xFF2E8B57) else Color(0xFF34A853)
            val whiteCloud = if (darkMode) Color(0xFFEAF7EE) else Color(0xFFFFFFFF)
            val darkCloud = if (darkMode) Color(0xFF0A100D) else Color(0xFF1E2D24)

            // Cloud 1: Soft emerald green drift (top-left to center)
            val c1 = Offset(
                x = w * (0.25f + 0.18f * cos(phaseA)),
                y = h * (0.20f + 0.08f * sin(phaseA))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        greenCloud.copy(alpha = if (darkMode) 0.22f else 0.14f),
                        Color.Transparent
                    ),
                    center = c1,
                    radius = w * 0.72f
                ),
                radius = w * 0.72f,
                center = c1
            )

            // Cloud 2: Soft white mist drift (upper-right / middle)
            val c2 = Offset(
                x = w * (0.78f + 0.16f * sin(phaseB)),
                y = h * (0.42f + 0.10f * cos(phaseB))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        whiteCloud.copy(alpha = if (darkMode) 0.09f else 0.55f),
                        Color.Transparent
                    ),
                    center = c2,
                    radius = w * 0.68f
                ),
                radius = w * 0.68f,
                center = c2
            )

            // Cloud 3: Subtle dark/slate depth cloud (lower-left / center)
            val c3 = Offset(
                x = w * (0.35f + 0.20f * sin(phaseC)),
                y = h * (0.72f + 0.09f * cos(phaseC))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        darkCloud.copy(alpha = if (darkMode) 0.34f else 0.05f),
                        Color.Transparent
                    ),
                    center = c3,
                    radius = w * 0.65f
                ),
                radius = w * 0.65f,
                center = c3
            )

            // Cloud 4: Secondary soft green-white glow (bottom-right)
            val c4 = Offset(
                x = w * (0.72f - 0.15f * cos(phaseA * 0.8f)),
                y = h * (0.85f - 0.07f * sin(phaseB * 0.8f))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        greenCloud.copy(alpha = if (darkMode) 0.16f else 0.10f),
                        Color.Transparent
                    ),
                    center = c4,
                    radius = w * 0.58f
                ),
                radius = w * 0.58f,
                center = c4
            )
        }
    }
}
