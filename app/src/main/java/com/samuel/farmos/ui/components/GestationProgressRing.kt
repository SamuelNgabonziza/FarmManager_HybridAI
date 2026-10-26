package com.samuel.farmos.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samuel.farmos.data.BreedingRecord
import com.samuel.farmos.ui.screens.FarmUi
import java.util.concurrent.TimeUnit

@Composable
fun GestationProgressRing(
    progress: Float,
    daysLeft: Long,
    size: Dp = 54.dp,
    strokeWidth: Dp = 5.dp,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900),
        label = "gestationProgress"
    )

    val activeColor = if (daysLeft <= 7) FarmUi.coral else FarmUi.purple
    val trackColor = if (isSystemInDarkTheme()) Color(0xFF382552) else Color(0xFFF0E5FF)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            // Background track
            drawCircle(
                color = trackColor,
                radius = (size.toPx() - strokeWidth.toPx()) / 2f,
                style = stroke
            )
            // Progress arc
            drawArc(
                color = activeColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = stroke
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val label = if (daysLeft <= 0) "Due" else "${daysLeft}d"
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = if (size > 50.dp) 11.sp else 9.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = FarmUi.ink
            )
        }
    }
}

@Composable
fun GestationSummaryBadge(
    record: BreedingRecord,
    animalTag: String,
    modifier: Modifier = Modifier
) {
    val dueDate = record.expectedDueDateMillis ?: return
    val now = System.currentTimeMillis()
    val totalGestationMillis = (dueDate - record.eventDateMillis).coerceAtLeast(1L)
    val elapsedMillis = (now - record.eventDateMillis).coerceAtLeast(0L)
    val progress = (elapsedMillis.toFloat() / totalGestationMillis).coerceIn(0f, 1f)
    val daysRemaining = TimeUnit.MILLISECONDS.toDays(dueDate - now)

    val badgeBg = if (isSystemInDarkTheme()) Color(0xFF30243C) else Color(0xFFF7F0FF)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = badgeBg,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (daysRemaining <= 7) FarmUi.coral.copy(alpha = 0.5f) else FarmUi.purple.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GestationProgressRing(
                progress = progress,
                daysLeft = daysRemaining,
                size = 38.dp,
                strokeWidth = 3.5.dp
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = animalTag.ifBlank { "Animal" },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = FarmUi.ink
                )
                Text(
                    text = if (daysRemaining <= 0) "Birth due now!" else "$daysRemaining days to birth",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (daysRemaining <= 7) FarmUi.coral else FarmUi.purple,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
