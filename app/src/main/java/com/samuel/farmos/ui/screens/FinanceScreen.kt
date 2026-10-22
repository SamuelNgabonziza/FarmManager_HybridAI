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
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.samuel.farmos.ui.components.FarmDeleteButton
import com.samuel.farmos.ui.components.FarmEmptyState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.FinanceRecord
import com.samuel.farmos.data.FinanceType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

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
                title = { Text("Finances", fontWeight = FontWeight.Bold) },
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
                text = { Text("Add Record") },
                containerColor = FarmUi.teal,
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
            Column(modifier = Modifier.fillMaxSize()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = FarmUi.surfaceMint)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "This Month",
                            style = MaterialTheme.typography.labelMedium,
                            color = FarmUi.textMuted
                        )
                        Text(
                            text = "Net Profit  UGX $netBalance",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (netBalance >= 0) FarmUi.deepGreen else MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Revenue", style = MaterialTheme.typography.labelSmall, color = FarmUi.textMuted)
                                Text("UGX $totalIncome", fontWeight = FontWeight.Bold, color = FarmUi.green)
                            }
                            Column {
                                Text("Expenses", style = MaterialTheme.typography.labelSmall, color = FarmUi.textMuted)
                                Text("UGX $totalExpense", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                if (records.isEmpty()) {
                    FarmEmptyState(
                        icon = Icons.Default.TrendingUp,
                        title = "No transactions yet",
                        message = "Record income and expenses to see a running net balance for the farm.",
                        actionLabel = "Add Transaction",
                        onAction = onAddRecord
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(records) { record ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                            text = "$prefix UGX ${record.amountUgx}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (isIncome) FarmUi.green else MaterialTheme.colorScheme.error
                                        )
                                        FarmDeleteButton(
                                            description = record.category.name,
                                            onDelete = { onDeleteRecord(record.id) }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    val dateStr = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(record.dateMillis))
                                    Text(
                                        text = "${record.description.ifBlank { "No description" }} · $dateStr",
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
