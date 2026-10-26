package com.samuel.farmos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.samuel.farmos.ui.screens.FarmUi

@Composable
fun FeedRationCalculatorView(modifier: Modifier = Modifier) {
    // Portions in kg per cow per day
    var grassKg by remember { mutableFloatStateOf(20f) }
    var silageKg by remember { mutableFloatStateOf(10f) }
    var concentrateKg by remember { mutableFloatStateOf(4f) }

    // Unit prices (e.g. in UGX or currency per unit)
    var grassCostPerKg by remember { mutableStateOf("150") }
    var silageCostPerKg by remember { mutableStateOf("300") }
    var concentrateCostPerKg by remember { mutableStateOf("1500") }
    var milkPricePerLitre by remember { mutableStateOf("1400") }

    // Calculations
    val gCost = (grassCostPerKg.toDoubleOrNull() ?: 0.0) * grassKg
    val sCost = (silageCostPerKg.toDoubleOrNull() ?: 0.0) * silageKg
    val cCost = (concentrateCostPerKg.toDoubleOrNull() ?: 0.0) * concentrateKg
    val totalDailyFeedCost = gCost + sCost + cCost

    // Estimated daily milk output model:
    // Maintenance needs ~12kg dry matter baseline.
    // Concentrates provide high energy: approx 1.5 - 2.0 L milk per kg of dairy meal above maintenance
    val baseMilk = 4.0
    val forageContribution = (grassKg * 0.15 + silageKg * 0.25)
    val concentrateContribution = (concentrateKg * 1.8)
    val estimatedMilkLitres = (baseMilk + forageContribution + concentrateContribution).coerceAtLeast(0.0)

    val milkPrice = milkPricePerLitre.toDoubleOrNull() ?: 0.0
    val estimatedDailyRevenue = estimatedMilkLitres * milkPrice
    val estimatedDailyNetProfit = estimatedDailyRevenue - totalDailyFeedCost

    val summaryCardBg = if (isSystemInDarkTheme()) Color(0xFF1E3526) else Color(0xFFE8F8EE)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Performance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = FarmUi.cardShape,
            colors = CardDefaults.cardColors(containerColor = summaryCardBg),
            border = BorderStroke(1.dp, FarmUi.green.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = FarmUi.green)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Estimated Daily Outcome per Cow",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FarmUi.green
                    )
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Est. Milk Output", style = MaterialTheme.typography.bodySmall, color = FarmUi.textMuted)
                        Text(
                            "${String.format("%.1f", estimatedMilkLitres)} L",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = FarmUi.ink
                        )
                    }
                    Column {
                        Text("Feed Cost", style = MaterialTheme.typography.bodySmall, color = FarmUi.textMuted)
                        Text(
                            "${totalDailyFeedCost.toLong()} UGX",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = FarmUi.orange
                        )
                    }
                    Column {
                        Text("Est. Net Profit", style = MaterialTheme.typography.bodySmall, color = FarmUi.textMuted)
                        Text(
                            "${estimatedDailyNetProfit.toLong()} UGX",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (estimatedDailyNetProfit >= 0) FarmUi.green else FarmUi.coral
                        )
                    }
                }
            }
        }

        // Sliders Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = FarmUi.cardShape,
            colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
            border = BorderStroke(1.dp, FarmUi.cardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Daily Ration Composition (per cow)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = FarmUi.ink
                )

                Spacer(Modifier.height(12.dp))

                // Grass Slider
                Text(
                    "Fresh Napier / Pasture: ${grassKg.toInt()} kg",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = FarmUi.green
                )
                Slider(
                    value = grassKg,
                    onValueChange = { grassKg = it },
                    valueRange = 0f..40f,
                    steps = 39,
                    colors = SliderDefaults.colors(thumbColor = FarmUi.green, activeTrackColor = FarmUi.green)
                )

                Spacer(Modifier.height(8.dp))

                // Silage Slider
                Text(
                    "Maize Silage / Hay: ${silageKg.toInt()} kg",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = FarmUi.teal
                )
                Slider(
                    value = silageKg,
                    onValueChange = { silageKg = it },
                    valueRange = 0f..25f,
                    steps = 24,
                    colors = SliderDefaults.colors(thumbColor = FarmUi.teal, activeTrackColor = FarmUi.teal)
                )

                Spacer(Modifier.height(8.dp))

                // Concentrate Slider
                Text(
                    "Dairy Meal / Concentrates: ${String.format("%.1f", concentrateKg)} kg",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = FarmUi.orange
                )
                Slider(
                    value = concentrateKg,
                    onValueChange = { concentrateKg = it },
                    valueRange = 0f..12f,
                    steps = 23,
                    colors = SliderDefaults.colors(thumbColor = FarmUi.orange, activeTrackColor = FarmUi.orange)
                )
            }
        }

        // Unit Prices Configuration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = FarmUi.cardShape,
            colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
            border = BorderStroke(1.dp, FarmUi.cardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Local Feed Prices & Milk Selling Price",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = FarmUi.ink
                )
                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = concentrateCostPerKg,
                        onValueChange = { concentrateCostPerKg = it },
                        label = { Text("Meal/kg") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = FarmUi.textFieldColors()
                    )
                    OutlinedTextField(
                        value = milkPricePerLitre,
                        onValueChange = { milkPricePerLitre = it },
                        label = { Text("Milk/Litre") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = FarmUi.textFieldColors()
                    )
                }
            }
        }

        // Nutrition Insights & Practical Tip Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = if (isSystemInDarkTheme()) Color(0xFF233954) else Color(0xFFDDEAFF),
            border = BorderStroke(1.dp, FarmUi.navy.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = FarmUi.navy)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "Nutritional Insight",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = FarmUi.navy
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (concentrateKg > 6f) {
                            "High concentrate level. Ensure cows have constant access to clean water and adequate fiber to prevent rumen acidosis."
                        } else if (concentrateKg < 2f && estimatedMilkLitres > 12) {
                            "Diet may lack energy density for peak lactation. Increasing dairy meal by 1-2 kg could increase milk profit."
                        } else {
                            "Well-balanced forage-to-concentrate ratio. Good for sustained lactation and body condition score."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = FarmUi.ink
                    )
                }
            }
        }
    }
}
