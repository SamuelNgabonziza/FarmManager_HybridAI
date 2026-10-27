package com.samuel.farmos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samuel.farmos.ui.screens.FarmUi
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

enum class HeatStressLevel(
    val title: String,
    val description: String,
    val lightColor: Color,
    val darkColor: Color,
    val estYieldLossText: String
) {
    COMFORT(
        title = "Optimal Comfort",
        description = "Ideal rumination & cud-chewing. Zero heat stress.",
        lightColor = Color(0xFF2E7D32),
        darkColor = Color(0xFF81C784),
        estYieldLossText = "0% (0.0 L/day)"
    ),
    MILD(
        title = "Mild Heat Stress",
        description = "Respiration rate rises (>60 breaths/min). Cows seek shade.",
        lightColor = Color(0xFFD97706),
        darkColor = Color(0xFFFBBF24),
        estYieldLossText = "-5% to -8% (~0.8 L/day)"
    ),
    MODERATE(
        title = "Moderate Heat Stress",
        description = "Feed intake drops. Milk yield drops. Silent heat occurs.",
        lightColor = Color(0xFFE65100),
        darkColor = Color(0xFFFF9800),
        estYieldLossText = "-10% to -20% (~2.2 L/day)"
    ),
    SEVERE(
        title = "Severe Heat Stress",
        description = "Open-mouth panting (>100 breaths/min). High risk of rumen acidosis & heat stroke.",
        lightColor = Color(0xFFC62828),
        darkColor = Color(0xFFEF5350),
        estYieldLossText = "-25% to -40% (~4.5 L/day)"
    )
}

data class WeatherPreset(
    val name: String,
    val emoji: String,
    val tempC: Float,
    val humidityPct: Float
)

@Composable
fun LivestockHeatStressCard(
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var temperatureC by remember { mutableFloatStateOf(28f) }
    var relativeHumidityPct by remember { mutableFloatStateOf(65f) }

    // NRC / Thom Formula: THI = 0.8 * T + (RH/100) * (T - 14.4) + 46.4
    val thi = remember(temperatureC, relativeHumidityPct) {
        val t = temperatureC
        val rh = relativeHumidityPct / 100f
        (0.8f * t) + (rh * (t - 14.4f)) + 46.4f
    }

    val stressLevel = when {
        thi < 68f -> HeatStressLevel.COMFORT
        thi < 72f -> HeatStressLevel.MILD
        thi < 80f -> HeatStressLevel.MODERATE
        else -> HeatStressLevel.SEVERE
    }

    val isDark = isSystemInDarkTheme()
    val levelColor = if (isDark) stressLevel.darkColor else stressLevel.lightColor

    val levelBg = if (isDark) {
        when (stressLevel) {
            HeatStressLevel.COMFORT -> Color(0xFF162B1D)
            HeatStressLevel.MILD -> Color(0xFF332713)
            HeatStressLevel.MODERATE -> Color(0xFF381F10)
            HeatStressLevel.SEVERE -> Color(0xFF3B171A)
        }
    } else {
        when (stressLevel) {
            HeatStressLevel.COMFORT -> Color(0xFFEAF8ED)
            HeatStressLevel.MILD -> Color(0xFFFEF7E6)
            HeatStressLevel.MODERATE -> Color(0xFFFDF0E6)
            HeatStressLevel.SEVERE -> Color(0xFFFDECEE)
        }
    }

    val presets = remember {
        listOf(
            WeatherPreset("Cool Morning", "⛅", 21f, 65f),
            WeatherPreset("Warm Afternoon", "☀️", 28f, 55f),
            WeatherPreset("Humid Tropics", "🌧️", 29f, 85f),
            WeatherPreset("Hot Midday", "🔥", 34f, 65f),
            WeatherPreset("Extreme Heatwave", "🌡️", 38f, 75f)
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
        border = BorderStroke(1.dp, FarmUi.cardBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Header: Title + Status Pill + Expand Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(levelBg, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (stressLevel) {
                                HeatStressLevel.COMFORT -> Icons.Default.CheckCircle
                                HeatStressLevel.MILD -> Icons.Default.WbSunny
                                HeatStressLevel.MODERATE -> Icons.Default.Warning
                                HeatStressLevel.SEVERE -> Icons.Default.Thermostat
                            },
                            contentDescription = null,
                            tint = levelColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Heat Stress Radar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = FarmUi.ink
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = levelBg,
                                border = BorderStroke(1.dp, levelColor.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = String.format(Locale.US, "THI %.0f", thi),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = levelColor
                                )
                            }
                        }
                        Text(
                            text = "${stressLevel.title} (${temperatureC.toInt()}°C, ${relativeHumidityPct.toInt()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = FarmUi.textMuted
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = FarmUi.textMuted
                    )
                }
            }

            // Quick Est Milk Loss Banner (Always Visible)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = levelBg,
                border = BorderStroke(1.dp, levelColor.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (stressLevel == HeatStressLevel.COMFORT) "🥛 Yield Status:" else "📉 Est. Milk Impact:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = FarmUi.ink
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stressLevel.estYieldLossText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = levelColor
                        )
                    }
                    Text(
                        text = if (isExpanded) "Hide Controls ▲" else "Adjust Weather ▼",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmUi.teal,
                        modifier = Modifier.clickable { isExpanded = !isExpanded }
                    )
                }
            }

            // Expandable Section: Visual Gauge, Sliders, Scenarios, and Farm Action Checklist
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(250)) + fadeIn(tween(250)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = FarmUi.cardBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Semi-Circular THI Gauge Meter
                    ThiGaugeMeter(
                        thi = thi,
                        levelColor = levelColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stressLevel.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = FarmUi.ink,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Weather Preset Chips
                    Text(
                        text = "Quick Weather Scenarios:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = FarmUi.textMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presets) { preset ->
                            val isSelected = (temperatureC == preset.tempC && relativeHumidityPct == preset.humidityPct)
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) FarmUi.teal.copy(alpha = 0.15f) else FarmUi.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) FarmUi.teal else FarmUi.cardBorder
                                ),
                                modifier = Modifier.clickable {
                                    temperatureC = preset.tempC
                                    relativeHumidityPct = preset.humidityPct
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(preset.emoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) FarmUi.teal else FarmUi.ink
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. Temperature Slider & Steppers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Thermostat, contentDescription = null, tint = FarmUi.coral, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ambient Temperature",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = FarmUi.ink
                            )
                        }
                        Text(
                            text = "${temperatureC.toInt()}°C",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FarmUi.coral
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { temperatureC = (temperatureC - 1f).coerceIn(15f, 45f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "-1°C", tint = FarmUi.textMuted)
                        }
                        Slider(
                            value = temperatureC,
                            onValueChange = { temperatureC = it },
                            valueRange = 15f..45f,
                            steps = 29,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = FarmUi.coral,
                                activeTrackColor = FarmUi.coral,
                                inactiveTrackColor = FarmUi.coral.copy(alpha = 0.2f)
                            )
                        )
                        IconButton(
                            onClick = { temperatureC = (temperatureC + 1f).coerceIn(15f, 45f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "+1°C", tint = FarmUi.textMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Relative Humidity Slider & Steppers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = FarmUi.teal, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Relative Humidity",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = FarmUi.ink
                            )
                        }
                        Text(
                            text = "${relativeHumidityPct.toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FarmUi.teal
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { relativeHumidityPct = (relativeHumidityPct - 5f).coerceIn(20f, 100f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "-5%", tint = FarmUi.textMuted)
                        }
                        Slider(
                            value = relativeHumidityPct,
                            onValueChange = { relativeHumidityPct = it },
                            valueRange = 20f..100f,
                            steps = 15,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = FarmUi.teal,
                                activeTrackColor = FarmUi.teal,
                                inactiveTrackColor = FarmUi.teal.copy(alpha = 0.2f)
                            )
                        )
                        IconButton(
                            onClick = { relativeHumidityPct = (relativeHumidityPct + 5f).coerceIn(20f, 100f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "+5%", tint = FarmUi.textMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = FarmUi.cardBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    // 5. Actionable Farm Mitigation Checklist
                    Text(
                        text = "🛡️ Recommended Farm Actions (${stressLevel.title})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmUi.green
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    when (stressLevel) {
                        HeatStressLevel.COMFORT -> {
                            ActionItem(
                                icon = Icons.Default.CheckCircle,
                                iconTint = FarmUi.green,
                                title = "Normal Management",
                                detail = "Conditions are ideal. Maintain standard feeding schedules and clean water availability."
                            )
                        }
                        HeatStressLevel.MILD -> {
                            ActionItem(
                                icon = Icons.Default.WaterDrop,
                                iconTint = FarmUi.teal,
                                title = "Increase Water Supply (+20%)",
                                detail = "Provide continuous access to cool, shaded drinking troughs. Cows will drink up to 90–110L today."
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ActionItem(
                                icon = Icons.Default.Air,
                                iconTint = FarmUi.amber,
                                title = "Ensure Boma Airflow",
                                detail = "Open barn ventilation slats and keep milking parlor waiting area well-shaded."
                            )
                        }
                        HeatStressLevel.MODERATE -> {
                            ActionItem(
                                icon = Icons.Default.WaterDrop,
                                iconTint = FarmUi.teal,
                                title = "Water Boost (+35L / cow)",
                                detail = "Ensure trough flow rate is fast. Cows drink heavily immediately after milking."
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ActionItem(
                                icon = Icons.Default.Thermostat,
                                iconTint = FarmUi.coral,
                                title = "Shift Concentrate Feeding",
                                detail = "Feed 70% of dairy meal and total mixed ration (TMR) early at 6 AM and dusk after 7 PM to reduce digestive heat peaks."
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ActionItem(
                                icon = Icons.Default.Warning,
                                iconTint = FarmUi.amber,
                                title = "Rumen Buffer Supplement",
                                detail = "Add 150g sodium bicarbonate (baking soda) per cow to prevent subacute rumen acidosis (SARA)."
                            )
                        }
                        HeatStressLevel.SEVERE -> {
                            ActionItem(
                                icon = Icons.Default.Thermostat,
                                iconTint = FarmUi.coral,
                                title = "Emergency Heat Relief Protocol",
                                detail = "Sprinkle cows with coarse water droplets for 2–3 minutes followed by high-speed airflow."
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ActionItem(
                                icon = Icons.Default.Warning,
                                iconTint = FarmUi.coral,
                                title = "Avoid Midday Cattle Movement",
                                detail = "Do NOT herd, walk, dip, or vaccinate cattle between 11:00 AM and 4:30 PM."
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ActionItem(
                                icon = Icons.Default.WaterDrop,
                                iconTint = FarmUi.teal,
                                title = "Electrolyte & Energy Boost",
                                detail = "Add oral electrolytes or bypass fat supplements to replace lost minerals and keep energy intake stable."
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThiGaugeMeter(
    thi: Float,
    levelColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedThi by animateFloatAsState(
        targetValue = thi.coerceIn(60f, 95f),
        animationSpec = tween(500),
        label = "thiAnimation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
            val width = size.width
            val height = size.height
            val strokeWidth = 14.dp.toPx()
            val radius = (height * 1.35f).coerceAtMost(width * 0.42f)
            val center = Offset(width / 2f, height * 0.85f)
            val arcSize = Size(radius * 2, radius * 2)
            val topLeft = Offset(center.x - radius, center.y - radius)

            // Segment 1: Comfort (<68) -> start 180°, sweep 45°
            drawArc(
                color = Color(0xFF2E7D32),
                startAngle = 180f,
                sweepAngle = 44f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Segment 2: Mild (68-71) -> start 226°, sweep 28°
            drawArc(
                color = Color(0xFFF59E0B),
                startAngle = 226f,
                sweepAngle = 26f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Segment 3: Moderate (72-79) -> start 254°, sweep 50°
            drawArc(
                color = Color(0xFFE65100),
                startAngle = 254f,
                sweepAngle = 48f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Segment 4: Severe (80-95) -> start 304°, sweep 56°
            drawArc(
                color = Color(0xFFD32F2F),
                startAngle = 304f,
                sweepAngle = 56f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Needle Indicator
            // Map 60..95 THI to 180°..360°
            val fraction = ((animatedThi - 60f) / (95f - 60f)).coerceIn(0f, 1f)
            val angleDeg = 180f + (fraction * 180f)
            val angleRad = Math.toRadians(angleDeg.toDouble())

            val needleLength = radius * 0.82f
            val endX = center.x + (needleLength * cos(angleRad)).toFloat()
            val endY = center.y + (needleLength * sin(angleRad)).toFloat()

            // Draw needle line
            drawLine(
                color = levelColor,
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Draw center pivot circle
            drawCircle(
                color = levelColor,
                radius = 7.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = center
            )
        }

        // Numerical indicator centered below the arc
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format(Locale.US, "%.1f", thi),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = levelColor
            )
            Text(
                text = "INDEX (THI)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = FarmUi.textMuted
            )
        }
    }
}

@Composable
private fun ActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    detail: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = FarmUi.surface,
        border = BorderStroke(1.dp, FarmUi.cardBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconTint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = FarmUi.ink
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = FarmUi.textMuted,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
