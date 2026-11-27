package com.samuel.farmos.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samuel.farmos.ui.theme.FarmOsTheme
import kotlin.math.roundToInt

/**
 * Draggable floating Gemini-style launcher button for FarmOS Hybrid AI.
 * Clamped safely inside status-bar and navigation-bar bounds.
 */
@Composable
fun FloatingHybridAiOrb(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val orbSize = 62.dp
    val edgeMargin = 16.dp
    val density = LocalDensity.current

    val infiniteTransition = rememberInfiniteTransition(label = "hybrid_ai_orb_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_pulse"
    )
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_alpha"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(edgeMargin)
    ) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }
        val orbSizePx = with(density) { orbSize.toPx() }

        val maxX = (maxWidthPx - orbSizePx).coerceAtLeast(0f)
        val maxY = (maxHeightPx - orbSizePx).coerceAtLeast(0f)

        // Default position: bottom-right above navigation bar
        var offsetX by remember(maxX) { mutableFloatStateOf(maxX) }
        var offsetY by remember(maxY) { mutableFloatStateOf((maxY - with(density) { 12.dp.toPx() }).coerceAtLeast(0f)) }

        val clampedX = offsetX.coerceIn(0f, maxX)
        val clampedY = offsetY.coerceIn(0f, maxY)

        val containerColor = if (isDark) Color(0xFF1A3A2B) else Color(0xFF0E6B47)
        val borderColor = if (isDark) Color(0xFF70DC9E).copy(alpha = 0.55f) else Color(0xFFA7F3D0)

        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            color = containerColor,
            shadowElevation = 8.dp,
            tonalElevation = 6.dp,
            border = BorderStroke(1.2.dp, borderColor),
            modifier = Modifier
                .offset { IntOffset(clampedX.roundToInt(), clampedY.roundToInt()) }
                .size(orbSize)
                .pointerInput(maxX, maxY) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x).coerceIn(0f, maxX)
                        offsetY = (offsetY + dragAmount.y).coerceIn(0f, maxY)
                    }
                }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Canvas(modifier = Modifier.size(26.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = size.minDimension / 2f
                    val inner = r * 0.28f

                    val starPath = Path().apply {
                        moveTo(cx, cy - r)
                        quadraticTo(cx + inner * 0.4f, cy - inner * 0.4f, cx + r, cy)
                        quadraticTo(cx + inner * 0.4f, cy + inner * 0.4f, cx, cy + r)
                        quadraticTo(cx - inner * 0.4f, cy + inner * 0.4f, cx - r, cy)
                        quadraticTo(cx - inner * 0.4f, cy - inner * 0.4f, cx, cy - r)
                        close()
                    }

                    scale(scale = pulse, pivot = Offset(cx, cy)) {
                        drawPath(
                            path = starPath,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF9AF5C0).copy(alpha = shimmerAlpha),
                                    Color(0xFFFFFFFF).copy(alpha = shimmerAlpha),
                                    Color(0xFFFDE68A).copy(alpha = shimmerAlpha)
                                )
                            )
                        )
                    }
                }
                Text(
                    text = "AI",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Preview(name = "Orb Light", showBackground = true)
@Composable
private fun FloatingHybridAiOrbLightPreview() {
    FarmOsTheme(darkTheme = false) {
        FloatingHybridAiOrb(onClick = {})
    }
}

@Preview(name = "Orb Dark", showBackground = true)
@Composable
private fun FloatingHybridAiOrbDarkPreview() {
    FarmOsTheme(darkTheme = true) {
        FloatingHybridAiOrb(onClick = {})
    }
}
