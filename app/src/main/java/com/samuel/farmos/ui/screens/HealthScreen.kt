package com.samuel.farmos.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.HealthCase
import com.samuel.farmos.data.HealthCaseStatus
import com.samuel.farmos.ui.components.FarmEmptyState
import com.samuel.farmos.ui.theme.FarmOsTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    cases: List<HealthCase>,
    onAddCase: () -> Unit,
    onCaseDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Animal Health", fontWeight = FontWeight.Bold)
                        Text("Cases & vaccines", style = MaterialTheme.typography.bodySmall, color = FarmUi.textMuted)
                    }
                },
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
                onClick = onAddCase,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Report Illness") },
                containerColor = FarmUi.coral,
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
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HealthCountCard("Active", cases.count { it.status == HealthCaseStatus.ACTIVE }.toString(), FarmUi.surfaceCoral, Modifier.weight(1f))
                    HealthCountCard("Resolved", cases.count { it.status == HealthCaseStatus.RECOVERED || it.status == HealthCaseStatus.CLOSED }.toString(), FarmUi.surfaceMint, Modifier.weight(1f))
                }
                if (cases.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active health cases.", color = FarmUi.textMuted)
                    }
                } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 92.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(cases) { healthCase ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.Button, onClick = { onCaseDetail(healthCase.id) }),
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
                                        text = healthCase.mainComplaint,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = FarmUi.deepGreen,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = healthCase.status.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = FarmUi.green
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                val dateStr = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(healthCase.createdDateMillis))
                                Text(
                                    text = "Animal: ${healthCase.linkedAnimalId} · Logged: $dateStr",
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

@Composable
private fun HealthCountCard(title: String, count: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Card(modifier = modifier, shape = FarmUi.cardShape, colors = CardDefaults.cardColors(containerColor = color)) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = FarmUi.textMuted, style = MaterialTheme.typography.bodySmall)
            Text(count, color = FarmUi.green, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HealthScreenPreview() {
    FarmOsTheme {
        HealthScreen(
            cases = listOf(
                HealthCase(
                    id = "1",
                    linkedAnimalId = "101",
                    mainComplaint = "Limping left hind leg",
                    status = HealthCaseStatus.ACTIVE,
                    createdDateMillis = System.currentTimeMillis()
                )
            ),
            onAddCase = {},
            onCaseDetail = {},
            onBack = {}
        )
    }
}
