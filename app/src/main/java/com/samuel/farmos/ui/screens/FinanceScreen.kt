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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.FinanceRecord
import com.samuel.farmos.data.FinanceType
import com.samuel.farmos.ui.components.FarmDeleteButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    records: List<FinanceRecord>,
    onAddRecord: () -> Unit,
    onDeleteRecord: (String) -> Unit,
    onBack: () -> Unit
) {
    val monthStart = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val currentMonth = records.filter { it.dateMillis >= monthStart }
    val totalIncome = currentMonth.filter { it.type == FinanceType.INCOME }.sumOf { it.amountUgx }
    val totalExpense = currentMonth.filter { it.type == FinanceType.EXPENSE }.sumOf { it.amountUgx }
    val netBalance = totalIncome - totalExpense

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Farm Records", fontWeight = FontWeight.Bold, color = FarmUi.green) },
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
                text = { Text("Add Record", fontWeight = FontWeight.Bold) },
                containerColor = if (isSystemInDarkTheme()) Color(0xFF52D3C0) else FarmUi.teal,
                contentColor = if (isSystemInDarkTheme()) Color(0xFF003730) else Color.White,
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
            Column(modifier = Modifier.fillMaxSize()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                    border = BorderStroke(1.dp, FarmUi.cardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("This Month", style = MaterialTheme.typography.titleLarge, color = FarmUi.green, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Revenue", style = MaterialTheme.typography.titleMedium, color = FarmUi.ink)
                            Text("UGX ${totalIncome.toLong()}", style = MaterialTheme.typography.titleMedium, color = FarmUi.green, fontWeight = FontWeight.SemiBold)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Expenses", style = MaterialTheme.typography.titleMedium, color = FarmUi.ink)
                            Text("UGX ${totalExpense.toLong()}", style = MaterialTheme.typography.titleMedium, color = FarmUi.coral, fontWeight = FontWeight.SemiBold)
                        }
                        HorizontalDivider(color = FarmUi.cardBorder, modifier = Modifier.padding(vertical = 4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net Profit", style = MaterialTheme.typography.titleMedium, color = FarmUi.ink, fontWeight = FontWeight.Bold)
                            Text(
                                "UGX ${netBalance.toLong()}",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (netBalance >= 0) FarmUi.green else FarmUi.coral,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (records.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No finance records yet.", color = FarmUi.textMuted, style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
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
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = record.category.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = FarmUi.ink,
                                            modifier = Modifier.weight(1f)
                                        )
                                        val isIncome = record.type == FinanceType.INCOME
                                        val prefix = if (isIncome) "+ " else "- "
                                        Text(
                                            text = "$prefix UGX ${record.amountUgx.toLong()}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (isIncome) FarmUi.green else FarmUi.coral
                                        )
                                        FarmDeleteButton(
                                            description = "${record.category.name} record",
                                            onDelete = { onDeleteRecord(record.id) }
                                        )
                                    }
                                    val dateStr = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(record.dateMillis))
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FarmUi.textMuted
                                    )
                                    if (record.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = record.description,
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
}
