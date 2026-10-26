package com.samuel.farmos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CalendarMonth
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
import com.samuel.farmos.data.BreedingEventType
import com.samuel.farmos.data.BreedingRecord
import com.samuel.farmos.ui.components.FarmDeleteButton
import com.samuel.farmos.ui.components.GestationProgressRing
import com.samuel.farmos.ui.theme.FarmOsTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedingScreen(
    records: List<BreedingRecord>,
    onAddRecord: () -> Unit,
    onDeleteRecord: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Breeding Events", fontWeight = FontWeight.Bold, color = FarmUi.green) },
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
                onClick = onAddRecord,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Event", fontWeight = FontWeight.Bold) },
                containerColor = FarmUi.orange,
                contentColor = if (isSystemInDarkTheme()) Color(0xFF3D2005) else Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        },
        containerColor = FarmUi.background
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = FarmUi.background
        ) {
            if (records.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No breeding records yet.", color = FarmUi.textMuted, style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(records, key = { it.id }) { record ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                            border = BorderStroke(1.dp, FarmUi.cardBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(FarmUi.iconLavender, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = FarmUi.purple,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = record.eventType.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = FarmUi.purple
                                        )
                                        val dateStr = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(record.eventDateMillis))
                                        Text(
                                            text = dateStr,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = FarmUi.textMuted
                                        )
                                    }
                                    FarmDeleteButton(
                                        description = "${record.eventType.name} record",
                                        onDelete = { onDeleteRecord(record.id) }
                                    )
                                }

                                if (record.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Notes: ${record.notes}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = FarmUi.ink
                                    )
                                }

                                if (record.expectedDueDateMillis != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    val dueDate = record.expectedDueDateMillis
                                    val now = System.currentTimeMillis()
                                    val total = (dueDate - record.eventDateMillis).coerceAtLeast(1L)
                                    val elapsed = (now - record.eventDateMillis).coerceAtLeast(0L)
                                    val progress = (elapsed.toFloat() / total).coerceIn(0f, 1f)
                                    val daysRemaining = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(dueDate - now)

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(FarmUi.iconLavender.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        GestationProgressRing(progress = progress, daysLeft = daysRemaining, size = 44.dp)
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (daysRemaining <= 0) "Birth is due!" else "$daysRemaining days until expected birth",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (daysRemaining <= 7) FarmUi.coral else FarmUi.purple
                                            )
                                            val dueStr = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(dueDate))
                                            Text(
                                                text = "Target due date: $dueStr",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = FarmUi.textMuted
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
    }
}

@Preview(showBackground = true)
@Composable
fun BreedingScreenPreview() {
    FarmOsTheme {
        BreedingScreen(
            records = listOf(
                BreedingRecord(
                    id = "1",
                    eventType = BreedingEventType.INSEMINATION,
                    eventDateMillis = System.currentTimeMillis(),
                    notes = "Artificial insemination with Guernsey sire."
                ),
                BreedingRecord(
                    id = "2",
                    eventType = BreedingEventType.PREGNANCY_CHECK,
                    eventDateMillis = System.currentTimeMillis() - 86400000L * 30,
                    notes = "Confirmed pregnant by vet."
                )
            ),
            onAddRecord = {},
            onDeleteRecord = {},
            onBack = {}
        )
    }
}
