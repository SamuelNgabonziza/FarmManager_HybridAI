package com.samuel.farmos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samuel.farmos.ui.screens.FarmUi
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FarmProfitSimulatorView(
    initialCowCount: Int = 3,
    initialDailyYield: Double = 12.0,
    initialMilkPrice: Double = 1400.0,
    modifier: Modifier = Modifier
) {
    // Simulator Inputs
    var cowCount by remember { mutableIntStateOf(initialCowCount.coerceAtLeast(1)) }
    var dailyYieldPerCow by remember { mutableFloatStateOf(initialDailyYield.toFloat().coerceIn(4f, 35f)) }
    var milkPricePerLitre by remember { mutableFloatStateOf(initialMilkPrice.toFloat().coerceIn(800f, 3000f)) }
    var dailyFeedCostPerCow by remember { mutableFloatStateOf(3500f) }
    var monthlyOverheads by remember { mutableFloatStateOf(150000f) } // Labor, power, vet

    // Calculations
    val daysInMonth = 30.5f
    val totalDailyMilk = cowCount * dailyYieldPerCow
    val monthlyMilkLitres = totalDailyMilk * daysInMonth

    val monthlyGrossRevenue = monthlyMilkLitres * milkPricePerLitre
    val monthlyFeedCost = cowCount * dailyFeedCostPerCow * daysInMonth
    val totalMonthlyExpenses = monthlyFeedCost + monthlyOverheads
    val monthlyNetProfit = monthlyGrossRevenue - totalMonthlyExpenses
    val annualNetProfit = monthlyNetProfit * 12

    val costToProduceOneLitre = if (monthlyMilkLitres > 0) totalMonthlyExpenses / monthlyMilkLitres else 0.0
    val feedToRevenueRatio = if (monthlyGrossRevenue > 0) (monthlyFeedCost / monthlyGrossRevenue) * 100f else 0f
    val profitMarginPct = if (monthlyGrossRevenue > 0) (monthlyNetProfit / monthlyGrossRevenue) * 100f else 0f

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

    val summaryBg = if (monthlyNetProfit >= 0) {
        if (isSystemInDarkTheme()) Color(0xFF1E3526) else Color(0xFFE8F8EE)
    } else {
        if (isSystemInDarkTheme()) Color(0xFF3B2023) else Color(0xFFFFECEF)
    }

    val summaryBorder = if (monthlyNetProfit >= 0) FarmUi.green else FarmUi.coral

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Preset Scenarios Quick Chips
        Text(
            "Quick \"What-If\" Stress Tests",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = FarmUi.green
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                PresetChip(label = "🚀 +2 Cows") {
                    cowCount = (cowCount + 2).coerceAtMost(50)
                }
            }
            item {
                PresetChip(label = "🥛 Yield Boost (+3L)") {
                    dailyYieldPerCow = (dailyYieldPerCow + 3f).coerceAtMost(35f)
                }
            }
            item {
                PresetChip(label = "🌾 Feed Shock (+25%)") {
                    dailyFeedCostPerCow *= 1.25f
                }
            }
            item {
                PresetChip(label = "📉 Price Drop (-200 UGX)") {
                    milkPricePerLitre = (milkPricePerLitre - 200f).coerceAtLeast(800f)
                }
            }
            item {
                PresetChip(label = "🔄 Reset") {
                    cowCount = initialCowCount.coerceAtLeast(1)
                    dailyYieldPerCow = initialDailyYield.toFloat().coerceIn(4f, 35f)
                    milkPricePerLitre = initialMilkPrice.toFloat().coerceIn(800f, 3000f)
                    dailyFeedCostPerCow = 3500f
                    monthlyOverheads = 150000f
                }
            }
        }

        // Live Simulated Performance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = FarmUi.cardShape,
            colors = CardDefaults.cardColors(containerColor = summaryBg),
            border = BorderStroke(1.5.dp, summaryBorder.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = summaryBorder.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = summaryBorder,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Simulated Monthly Net Profit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = summaryBorder
                        )
                    }
                    Text(
                        text = "${String.format("%.0f", profitMarginPct)}% margin",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = summaryBorder
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "${if (monthlyNetProfit >= 0) "+" else ""}${numberFormat.format(monthlyNetProfit.toLong())} UGX",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (monthlyNetProfit >= 0) FarmUi.ink else FarmUi.coral
                )

                Text(
                    text = "Projected 1-Year Net Cash: ${numberFormat.format(annualNetProfit.toLong())} UGX",
                    style = MaterialTheme.typography.bodySmall,
                    color = FarmUi.textMuted
                )

                Spacer(Modifier.height(14.dp))

                // Breakdown row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Gross Revenue", style = MaterialTheme.typography.bodySmall, color = FarmUi.textMuted)
                        Text(
                            "${numberFormat.format(monthlyGrossRevenue.toLong())} UGX",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = FarmUi.green
                        )
                    }
                    Column {
                        Text("Total Costs", style = MaterialTheme.typography.bodySmall, color = FarmUi.textMuted)
                        Text(
                            "${numberFormat.format(totalMonthlyExpenses.toLong())} UGX",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = FarmUi.coral
                        )
                    }
                    Column {
                        Text("Cost per Liter", style = MaterialTheme.typography.bodySmall, color = FarmUi.textMuted)
                        Text(
                            "${String.format("%.0f", costToProduceOneLitre)} UGX/L",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = FarmUi.ink
                        )
                    }
                }
            }
        }

        // 12-Month Projected Trajectory Graph (Canvas)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = FarmUi.cardShape,
            colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
            border = BorderStroke(1.dp, FarmUi.cardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "12-Month Cumulative Cash Flow Trajectory",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = FarmUi.ink
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Simulated profit buildup over 12 calendar months",
                    style = MaterialTheme.typography.bodySmall,
                    color = FarmUi.textMuted
                )

                Spacer(Modifier.height(16.dp))

                ProfitTrajectoryCanvas(
                    monthlyProfit = monthlyNetProfit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Month 1", style = MaterialTheme.typography.labelSmall, color = FarmUi.textMuted)
                    Text("Month 6", style = MaterialTheme.typography.labelSmall, color = FarmUi.textMuted)
                    Text("Month 12", style = MaterialTheme.typography.labelSmall, color = FarmUi.textMuted)
                }
            }
        }

        // Interactive Levers (Sliders)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = FarmUi.cardShape,
            colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
            border = BorderStroke(1.dp, FarmUi.cardBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Adjust Simulation Levers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FarmUi.green
                )

                // 1. Milking Cows Count Stepper & Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Milking Herd Size",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = FarmUi.ink
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { cowCount = (cowCount - 1).coerceAtLeast(1) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = FarmUi.green)
                            }
                            Text(
                                "$cowCount cows",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = FarmUi.green,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(
                                onClick = { cowCount = (cowCount + 1).coerceAtMost(50) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = FarmUi.green)
                            }
                        }
                    }
                    Slider(
                        value = cowCount.toFloat(),
                        onValueChange = { cowCount = it.toInt() },
                        valueRange = 1f..30f,
                        steps = 28,
                        colors = SliderDefaults.colors(thumbColor = FarmUi.green, activeTrackColor = FarmUi.green)
                    )
                }

                // 2. Daily Milk Yield per Cow
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Avg. Yield per Cow",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = FarmUi.ink
                        )
                        Text(
                            "${String.format("%.1f", dailyYieldPerCow)} L/day (${String.format("%.0f", totalDailyMilk)} L herd total)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = FarmUi.teal
                        )
                    }
                    Slider(
                        value = dailyYieldPerCow,
                        onValueChange = { dailyYieldPerCow = it },
                        valueRange = 4f..30f,
                        steps = 25,
                        colors = SliderDefaults.colors(thumbColor = FarmUi.teal, activeTrackColor = FarmUi.teal)
                    )
                }

                // 3. Milk Selling Price
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Milk Selling Price",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = FarmUi.ink
                        )
                        Text(
                            "${numberFormat.format(milkPricePerLitre.toLong())} UGX/L",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = FarmUi.orange
                        )
                    }
                    Slider(
                        value = milkPricePerLitre,
                        onValueChange = { milkPricePerLitre = it },
                        valueRange = 800f..2500f,
                        steps = 16,
                        colors = SliderDefaults.colors(thumbColor = FarmUi.orange, activeTrackColor = FarmUi.orange)
                    )
                }

                // 4. Daily Feed Cost per Cow
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Daily Feed Cost per Cow",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = FarmUi.ink
                        )
                        Text(
                            "${numberFormat.format(dailyFeedCostPerCow.toLong())} UGX/day",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = FarmUi.coral
                        )
                    }
                    Slider(
                        value = dailyFeedCostPerCow,
                        onValueChange = { dailyFeedCostPerCow = it },
                        valueRange = 1000f..9000f,
                        steps = 15,
                        colors = SliderDefaults.colors(thumbColor = FarmUi.coral, activeTrackColor = FarmUi.coral)
                    )
                }
            }
        }

        // Smart Financial Health Advisory Pill
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = if (feedToRevenueRatio <= 55f) {
                if (isSystemInDarkTheme()) Color(0xFF1E3526) else Color(0xFFEBF7F0)
            } else {
                if (isSystemInDarkTheme()) Color(0xFF38291F) else Color(0xFFFFF2E6)
            },
            border = BorderStroke(
                1.dp,
                if (feedToRevenueRatio <= 55f) FarmUi.green.copy(alpha = 0.5f) else FarmUi.orange.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = if (feedToRevenueRatio <= 55f) FarmUi.green else FarmUi.orange
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (feedToRevenueRatio <= 55f) "Excellent Operational Efficiency" else "Feed Cost Warning",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (feedToRevenueRatio <= 55f) FarmUi.green else FarmUi.orange
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = if (feedToRevenueRatio <= 55f) {
                            "Feed accounts for ${String.format("%.1f", feedToRevenueRatio)}% of milk revenue (healthy benchmark is <55%). At ${String.format("%.0f", costToProduceOneLitre)} UGX/L cost, your farm produces strong net profits."
                        } else {
                            "Feed accounts for ${String.format("%.1f", feedToRevenueRatio)}% of revenue, which is high. Consider growing your own Napier grass or silage to reduce reliance on purchased commercial feed."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = FarmUi.ink
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetChip(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = FarmUi.surface,
        border = BorderStroke(1.dp, FarmUi.green.copy(alpha = 0.4f)),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = FarmUi.green,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ProfitTrajectoryCanvas(
    monthlyProfit: Float,
    modifier: Modifier = Modifier
) {
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800),
        label = "graphAnim"
    )

    val isPositive = monthlyProfit >= 0
    val lineColor = if (isPositive) Color(0xFF80E3A7) else Color(0xFFFFAAA6)
    val gridColor = if (isSystemInDarkTheme()) Color(0xFF384B3D) else Color(0xFFE2EBE2)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Horizontal Zero line (midpoint or baseline)
        val zeroY = if (isPositive) h * 0.82f else h * 0.25f
        drawLine(
            color = gridColor,
            start = Offset(0f, zeroY),
            end = Offset(w, zeroY),
            strokeWidth = 1.5f
        )

        // Generate 12 data points (Month 1 to Month 12 cumulative)
        val points = (1..12).map { month ->
            val cumulative = monthlyProfit * month
            val normX = ((month - 1) / 11f) * w
            val maxAbs = kotlin.math.max(kotlin.math.abs(monthlyProfit * 12), 1f)
            val normY = if (isPositive) {
                zeroY - ((cumulative / maxAbs) * (zeroY - 15f))
            } else {
                zeroY + ((kotlin.math.abs(cumulative) / maxAbs) * (h - zeroY - 15f))
            }
            Offset(normX, normY)
        }

        // Draw curve path
        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { i, pt ->
            val animatedPt = Offset(
                pt.x,
                zeroY + (pt.y - zeroY) * animProgress
            )
            if (i == 0) {
                path.moveTo(animatedPt.x, animatedPt.y)
                fillPath.moveTo(animatedPt.x, zeroY)
                fillPath.lineTo(animatedPt.x, animatedPt.y)
            } else {
                val prev = points[i - 1]
                val prevAnimated = Offset(prev.x, zeroY + (prev.y - zeroY) * animProgress)
                val cX = (prevAnimated.x + animatedPt.x) / 2f
                path.cubicTo(cX, prevAnimated.y, cX, animatedPt.y, animatedPt.x, animatedPt.y)
                fillPath.cubicTo(cX, prevAnimated.y, cX, animatedPt.y, animatedPt.x, animatedPt.y)
            }
        }

        fillPath.lineTo(points.last().x, zeroY)
        fillPath.close()

        // Draw gradient under curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.28f), Color.Transparent),
                startY = if (isPositive) 0f else zeroY,
                endY = if (isPositive) zeroY else h
            )
        )

        // Draw line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )

        // Draw point dots
        points.forEachIndexed { i, pt ->
            if (i == 0 || i == 5 || i == 11) {
                val animatedY = zeroY + (pt.y - zeroY) * animProgress
                drawCircle(
                    color = Color.White,
                    radius = 5.5f,
                    center = Offset(pt.x, animatedY)
                )
                drawCircle(
                    color = lineColor,
                    radius = 3.5f,
                    center = Offset(pt.x, animatedY)
                )
            }
        }
    }
}
