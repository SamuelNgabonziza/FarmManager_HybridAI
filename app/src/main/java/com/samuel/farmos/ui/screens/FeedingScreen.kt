package com.samuel.farmos.ui.screens

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
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.FeedingRecord
import com.samuel.farmos.ui.components.FarmDeleteButton
import com.samuel.farmos.ui.components.FarmEmptyState
import com.samuel.farmos.ui.theme.FarmOsTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedingScreen(
    records: List<FeedingRecord>,
    onAddRecord: () -> Unit,
    onDeleteRecord: (String) -> Unit,
    onBack: () -> Unit
) {
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val fedToday = records.filter { it.dateMillis >= todayStart }.sumOf { it.quantityKg }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feeding Records", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = FarmUi.topAppBarColors()
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddRecord,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Log Feed") },
                containerColor = FarmUi.green,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = FarmUi.background
        ) {
            if (records.isEmpty()) {
                FarmEmptyState(
                    icon = Icons.Default.Restaurant,
                    title = "No feeding records yet",
                    message = "Log what each animal is fed and how much, along with the cost, to plan rations and track spend.",
                    actionLabel = "Add Record",
                    onAction = onAddRecord
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = FarmUi.cardShape,
                            colors = CardDefaults.cardColors(containerColor = FarmUi.surfaceMint)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Fed Today", color = FarmUi.textMuted, style = MaterialTheme.typography.bodySmall)
                                Text("${fedToday} kg", color = FarmUi.green, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }
                    items(records) { record ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = FarmUi.surfaceMint)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${record.feedType} - ${record.quantityKg} kg",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = FarmUi.deepGreen,
                                        modifier = Modifier.weight(1f)
                                    )
                                    val dateStr = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(record.dateMillis))
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FarmUi.textMuted
                                    )
                                    FarmDeleteButton(
                                        description = "${record.feedType} feeding record",
                                        onDelete = { onDeleteRecord(record.id) }
                                    )
                                }

                                if (record.costUgx != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Cost: UGX ${record.costUgx}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = FarmUi.ink
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
