package com.samuel.farmos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.ProductionRecord
import com.samuel.farmos.data.ProductionType
import com.samuel.farmos.ui.components.FarmDeleteButton
import com.samuel.farmos.ui.components.FarmEmptyState
import com.samuel.farmos.ui.components.LivestockHeatStressCard
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
                        Text("Production & Yields", fontWeight = FontWeight.Bold, color = FarmUi.green)
                        Text(
                            "Daily milk and animal weight tracking",
                            style = MaterialTheme.typography.bodySmall,
                            color = FarmUi.textMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = FarmUi.green)
                    }
                },
                colors = FarmUi.topAppBarColors()
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAddRecord(selectedType) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(if (selectedType == ProductionType.MILK) "Log Production" else "Log Weight", fontWeight = FontWeight.Bold) },
                containerColor = FarmUi.orange,
                contentColor = if (isSystemInDarkTheme()) Color(0xFF3D2005) else Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        },
        containerColor = FarmUi.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).clipToBounds(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val milkSelected = selectedType == ProductionType.MILK
                    val selectedBg = if (isSystemInDarkTheme()) Color(0xFF80E3A7) else Color(0xFF26733A)
                    val selectedText = if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(role = Role.Tab) { selectedType = ProductionType.MILK },
                        shape = RoundedCornerShape(16.dp),
                        color = if (milkSelected) selectedBg else FarmUi.surface,
                        border = BorderStroke(1.dp, if (milkSelected) selectedBg else FarmUi.cardBorder)
                    ) {
                        Text(
                            text = "🥛 Daily Milk",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                            color = if (milkSelected) selectedText else FarmUi.ink,
                            fontWeight = if (milkSelected) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(role = Role.Tab) { selectedType = ProductionType.WEIGHT },
                        shape = RoundedCornerShape(16.dp),
                        color = if (!milkSelected) selectedBg else FarmUi.surface,
                        border = BorderStroke(1.dp, if (!milkSelected) selectedBg else FarmUi.cardBorder)
                    ) {
                        Text(
                            text = "⚖️ Animal Weight",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                            color = if (!milkSelected) selectedText else FarmUi.ink,
                            fontWeight = if (!milkSelected) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val amberBg = if (isSystemInDarkTheme()) Color(0xFF382C18) else Color(0xFFFFF8E7)
                    val amberBorder = if (isSystemInDarkTheme()) Color(0xFF5E4924) else Color(0xFFFFE082)
                    val amberText = if (isSystemInDarkTheme()) Color(0xFFFFA94D) else Color(0xFFD97706)

                    val mintBg = if (isSystemInDarkTheme()) Color(0xFF1B3828) else Color(0xFFE6F7F0)
                    val mintBorder = if (isSystemInDarkTheme()) Color(0xFF2C5E40) else Color(0xFFA7F3D0)
                    val mintText = if (isSystemInDarkTheme()) Color(0xFF80E3A7) else Color(0xFF10B981)

                    SummaryCard(
                        title = "Today's Total",
                        value = "${formatAmount(todayTotal)} ${if (selectedType == ProductionType.MILK) "L" else "kg"}",
                        backgroundColor = amberBg,
                        borderColor = amberBorder,
                        textColor = amberText,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Last 7 Days",
                        value = "${formatAmount(weekTotal)} ${if (selectedType == ProductionType.MILK) "L" else "kg"}",
                        backgroundColor = mintBg,
                        borderColor = mintBorder,
                        textColor = mintText,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (selectedType == ProductionType.MILK) {
                item {
                    LivestockHeatStressCard()
                }
            }
            if (selectedRecords.isEmpty()) {
                item {
                    FarmEmptyState(
                        icon = if (selectedType == ProductionType.MILK) Icons.Default.LocalDrink else Icons.Default.ShowChart,
                        title = if (selectedType == ProductionType.MILK) "No milk records yet" else "No weight records yet",
                        message = "Tap + below to add a ${if (selectedType == ProductionType.MILK) "milk log" else "weight record"}.",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    )
                }
            } else {
                item {
                    Text(
                        text = if (selectedType == ProductionType.MILK) "Milk Logs" else "Weight Logs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FarmUi.green,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(selectedRecords, key = { it.id }) { record ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                        border = BorderStroke(1.dp, FarmUi.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (record.type == ProductionType.MILK) FarmUi.iconWarm else FarmUi.iconTeal,
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (record.type == ProductionType.MILK) Icons.Default.LocalDrink else Icons.Default.ShowChart,
                                    contentDescription = null,
                                    tint = if (record.type == ProductionType.MILK) FarmUi.orange else FarmUi.teal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${formatAmount(record.quantity)} ${if (record.type == ProductionType.MILK) "Litres" else "kg"}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FarmUi.ink
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault()).format(Date(record.dateMillis)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FarmUi.textMuted
                                )
                            }
                            FarmDeleteButton(
                                description = "production record",
                                onDelete = { onDeleteRecord(record.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    backgroundColor: Color,
    borderColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = FarmUi.textMuted,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

private fun formatAmount(amount: Double): String =
    if (amount % 1.0 == 0.0) amount.toInt().toString() else String.format(Locale.US, "%.1f", amount)

@Preview(showBackground = true)
@Composable
fun ProductionScreenPreview() {
    FarmOsTheme {
        ProductionScreen(
            records = emptyList(),
            onAddRecord = {},
            onDeleteRecord = {},
            onBack = {}
        )
    }
}
