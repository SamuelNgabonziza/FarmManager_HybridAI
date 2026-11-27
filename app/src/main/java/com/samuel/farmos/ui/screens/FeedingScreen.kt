package com.samuel.farmos.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Restaurant
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.Animal
import com.samuel.farmos.data.FeedingRecord
import com.samuel.farmos.ui.components.FarmDeleteButton
import com.samuel.farmos.ui.components.FarmEmptyState
import com.samuel.farmos.ui.theme.FarmOsTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextOverflow
import com.samuel.farmos.ui.components.FeedRationCalculatorView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedingScreen(
    records: List<FeedingRecord>,
    onAddRecord: () -> Unit,
    onDeleteRecord: (String) -> Unit,
    onBack: () -> Unit,
    animals: List<Animal> = emptyList()
) {
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val fedToday = records.filter { it.dateMillis >= todayStart }.sumOf { it.quantityKg }

    val feedingCardBg = if (isSystemInDarkTheme()) Color(0xFF255D36) else Color(0xFF26733A)
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Feeding & Nutrition", fontWeight = FontWeight.Bold, color = FarmUi.green) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = FarmUi.green)
                        }
                    },
                    colors = FarmUi.topAppBarColors()
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = FarmUi.green,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = FarmUi.green
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Feed Logs",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) FarmUi.green else FarmUi.textMuted
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Ration Calculator",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) FarmUi.green else FarmUi.textMuted
                            )
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = onAddRecord,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Log Feed", fontWeight = FontWeight.Bold) },
                    containerColor = FarmUi.green,
                    contentColor = if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White,
                    shape = RoundedCornerShape(24.dp)
                )
            }
        },
        containerColor = FarmUi.background
    ) { padding ->
        if (selectedTab == 1) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).clipToBounds(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    FeedRationCalculatorView()
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).clipToBounds(),
                contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = FarmUi.cardShape,
                    colors = CardDefaults.cardColors(containerColor = feedingCardBg)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Fed Today", color = Color.White.copy(alpha = .8f), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${if (fedToday % 1.0 == 0.0) fedToday.toInt().toString() else String.format(Locale.US, "%.1f", fedToday)} kg",
                            color = if (isSystemInDarkTheme()) Color(0xFF80E3A7) else Color(0xFFDFF2E4),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }
            if (records.isEmpty()) {
                item {
                    FarmEmptyState(
                        icon = Icons.Default.Restaurant,
                        title = "No feeding records yet",
                        message = "Tap + Log Feed to record daily rations and feed costs.",
                        modifier = Modifier.fillMaxWidth().height(260.dp)
                    )
                }
            } else {
                items(records, key = { it.id }) { record ->
                    val animal = animals.firstOrNull { it.id == record.linkedAnimalId }
                    val linkedText = animal?.let { listOf(it.tagNumber, it.name).filter(String::isNotBlank).joinToString(" · ") }
                        ?.takeIf(String::isNotBlank) ?: "General (whole farm)"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = feedingCardBg)
                    ) {
                        Column(Modifier.padding(start = 16.dp, top = 14.dp, end = 8.dp, bottom = 14.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    record.feedType,
                                    Modifier.weight(1f),
                                    color = if (isSystemInDarkTheme()) Color(0xFF80E3A7) else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "${if (record.quantityKg % 1.0 == 0.0) record.quantityKg.toInt().toString() else record.quantityKg} kg",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                FarmDeleteButton(
                                    description = "${record.feedType} feeding record",
                                    onDelete = { onDeleteRecord(record.id) }
                                )
                            }
                            Text(linkedText, color = Color.White.copy(alpha = .85f), style = MaterialTheme.typography.bodySmall)
                            Text(
                                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(record.dateMillis)),
                                color = Color.White.copy(alpha = .75f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            record.costUgx?.let {
                                Text("Cost: UGX ${it.toLong()}", color = Color.White.copy(alpha = .9f), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Preview(showBackground = true)
@Composable
private fun FeedingScreenPreview() {
    FarmOsTheme {
        FeedingScreen(
            records = listOf(
                com.samuel.farmos.data.FeedingRecord(
                    id = "1",
                    feedType = "Dairy Meal & Silage",
                    quantityKg = 25.0,
                    costUgx = 45000.0,
                    dateMillis = System.currentTimeMillis()
                ),
                com.samuel.farmos.data.FeedingRecord(
                    id = "2",
                    feedType = "Hay & Mineral Lick",
                    quantityKg = 15.0,
                    costUgx = 20000.0,
                    dateMillis = System.currentTimeMillis() - 86400000L
                )
            ),
            onAddRecord = {},
            onDeleteRecord = {},
            onBack = {}
        )
    }
}
