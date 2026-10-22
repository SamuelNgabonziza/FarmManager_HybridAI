package com.samuel.farmos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.ProductionRecord
import com.samuel.farmos.data.ProductionType
import com.samuel.farmos.ui.components.FarmDeleteButton
import com.samuel.farmos.ui.components.FarmEmptyState
import com.samuel.farmos.ui.theme.FarmOsTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionScreen(
    records: List<ProductionRecord>,
    onAddRecord: (ProductionType) -> Unit,
    onDeleteRecord: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf(ProductionType.MILK) }
    val now = Calendar.getInstance()
    val todayStart = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val weekStart = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, -6)
    }.timeInMillis
    val selectedRecords = records.filter { it.type == selectedType }.sortedByDescending { it.dateMillis }
    val todayTotal = selectedRecords.filter { it.dateMillis >= todayStart }.sumOf { it.quantity }
    val weekTotal = selectedRecords.filter { it.dateMillis >= weekStart }.sumOf { it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Production & Yields", fontWeight = FontWeight.Bold)
                        Text("Daily milk and animal weight tracking", style = MaterialTheme.typography.bodySmall, color = FarmUi.textMuted)
                    }
                },
                colors = FarmUi.topAppBarColors()
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAddRecord(selectedType) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(if (selectedType == ProductionType.MILK) "Log Production" else "Log Weight") },
                containerColor = FarmUi.orange,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        containerColor = FarmUi.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedType == ProductionType.MILK,
                        onClick = { selectedType = ProductionType.MILK },
                        label = { Text("🥛 Daily Milk") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == ProductionType.WEIGHT,
                        onClick = { selectedType = ProductionType.WEIGHT },
                        label = { Text("🐄 Animal Weight") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryCard("Today's Total", "${formatAmount(todayTotal)} ${if (selectedType == ProductionType.MILK) "L" else "kg"}", FarmUi.surfaceWarm, Modifier.weight(1f))
                    SummaryCard("Last 7 Days", "${formatAmount(weekTotal)} ${if (selectedType == ProductionType.MILK) "L" else "kg"}", FarmUi.surfaceMint, Modifier.weight(1f))
                }
            }
            if (selectedRecords.isEmpty()) {
                item {
                    FarmEmptyState(
                        icon = Icons.Default.ShowChart,
                        title = if (selectedType == ProductionType.MILK) "No milk records yet" else "No animal weights yet",
                        message = "Tap below to record your first ${if (selectedType == ProductionType.MILK) "milk yield" else "animal weight"}.",
                        actionLabel = if (selectedType == ProductionType.MILK) "Log Production" else "Log Weight",
                        onAction = { onAddRecord(selectedType) }
                    )
                }
            } else {
                items(selectedRecords, key = { it.id }) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = FarmUi.cardShape,
                        colors = CardDefaults.cardColors(containerColor = FarmUi.surfaceMint)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("${formatAmount(record.quantity)} ${if (record.type == ProductionType.MILK) "L" else "kg"}", fontWeight = FontWeight.Bold, color = FarmUi.green)
                                Spacer(Modifier.height(3.dp))
                                Text(SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(record.dateMillis)), style = MaterialTheme.typography.bodySmall, color = FarmUi.textMuted)
                            }
                            FarmDeleteButton(description = "Production record", onDelete = { onDeleteRecord(record.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Card(modifier = modifier, shape = FarmUi.cardShape, colors = CardDefaults.cardColors(containerColor = color)) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = FarmUi.textMuted)
            Spacer(Modifier.height(7.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = FarmUi.green)
        }
    }
}

private fun formatAmount(amount: Double): String = if (amount % 1.0 == 0.0) amount.toLong().toString() else String.format(Locale.getDefault(), "%.1f", amount)

@Preview(showBackground = true)
@Composable
fun ProductionScreenPreview() {
    FarmOsTheme { ProductionScreen(emptyList(), {}, {}, {}) }
}
