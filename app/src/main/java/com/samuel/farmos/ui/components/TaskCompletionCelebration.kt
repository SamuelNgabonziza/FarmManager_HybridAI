package com.samuel.farmos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.samuel.farmos.ui.screens.FarmUi
import kotlin.random.Random

private data class ConfettiParticle(
    val initialX: Float,
    val initialY: Float,
    val targetX: Float,
    val targetY: Float,
    val size: Float,
    val color: Color,
    val rotationSpeed: Float
)

@Composable
fun TaskCompletionCelebration(
    modifier: Modifier = Modifier,
    message: String = "All farm chores done for today! 🎉",
    subtitle: String = "Great job keeping your farm running smoothly"
) {
    val scaleAnim = remember { Animatable(0.4f) }
    val progress = remember { Animatable(0f) }

    val particles = remember {
        val colors = listOf(
            Color(0xFF80E3A7), // Green
            Color(0xFF52D3C0), // Teal
            Color(0xFFFFA94D), // Amber
            Color(0xFFC7A8FF), // Purple
            Color(0xFFFF8DA1), // Pink
            Color(0xFF64B5F6)  // Sky
        )
        List(40) {
            ConfettiParticle(
                initialX = 0.5f + (Random.nextFloat() - 0.5f) * 0.2f,
                initialY = 0.4f,
                targetX = Random.nextFloat(),
                targetY = Random.nextFloat() * 1.1f,
                size = Random.nextFloat() * 8f + 5f,
                color = colors[Random.nextInt(colors.size)],
                rotationSpeed = (Random.nextFloat() - 0.5f) * 360f
            )
        }
    }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1800, easing = LinearEasing)
        )
    }

    val cardBg = if (isSystemInDarkTheme()) Color(0xFF1E3526) else Color(0xFFE8F8EE)
    val cardBorder = if (isSystemInDarkTheme()) Color(0xFF3B6647) else Color(0xFFBCE8CC)

    Box(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scaleAnim.value),
            shape = FarmUi.cardShape,
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.5.dp, cardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = FarmUi.green,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FarmUi.green
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = FarmUi.textMuted
                    )
                }
            }
        }

        // Confetti Canvas Overlay
        if (progress.value < 1f) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
            ) {
                val w = size.width
                val h = size.height
                val currentP = progress.value
                val alpha = (1f - currentP).coerceIn(0f, 1f)

                particles.forEach { p ->
                    val curX = (p.initialX + (p.targetX - p.initialX) * currentP) * w
                    val curY = (p.initialY + (p.targetY - p.initialY) * currentP) * h

                    drawCircle(
                        color = p.color.copy(alpha = alpha),
                        radius = p.size * (1f - currentP * 0.3f),
                        center = Offset(curX, curY)
                    )
                }
            }
        }
    }
}
