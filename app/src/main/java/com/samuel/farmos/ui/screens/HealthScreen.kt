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
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.HealthCase
import com.samuel.farmos.data.HealthCaseStatus
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
    val active = cases.count { it.status == HealthCaseStatus.ACTIVE }
    val resolved = cases.count { it.status == HealthCaseStatus.RECOVERED || it.status == HealthCaseStatus.CLOSED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Cases", fontWeight = FontWeight.Bold, color = FarmUi.green) },
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
                onClick = onAddCase,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Report Illness", fontWeight = FontWeight.Bold) },
                containerColor = FarmUi.coral,
                contentColor = if (isSystemInDarkTheme()) Color(0xFF520014) else Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        },
        containerColor = FarmUi.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HealthCountCard("🔴 Active", active, Modifier.weight(1f))
                HealthCountCard("🟢 Resolved", resolved, Modifier.weight(1f))
            }

            if (cases.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No active health cases.", color = FarmUi.textMuted, style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(cases, key = { it.id }) { healthCase ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.Button) { onCaseDetail(healthCase.id) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                            border = BorderStroke(1.dp, FarmUi.cardBorder)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(FarmUi.iconCoral, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Favorite,
                                            contentDescription = null,
                                            tint = FarmUi.coral,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = healthCase.mainComplaint,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = FarmUi.ink
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        val date = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(healthCase.createdDateMillis))
                                        Text(
                                            "Animal: ${healthCase.linkedAnimalId} · Logged: $date",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = FarmUi.textMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val statusColor = if (healthCase.status == HealthCaseStatus.ACTIVE) FarmUi.coral else FarmUi.green
                                    val statusBg = statusColor.copy(alpha = 0.16f)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = statusBg
                                    ) {
                                        Text(
                                            text = healthCase.status.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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

@Composable
private fun HealthCountCard(title: String, count: Int, modifier: Modifier) {
    val cardBg = if (isSystemInDarkTheme()) Color(0xFF255D36) else Color(0xFF26733A)
    Card(
        modifier = modifier,
        shape = FarmUi.cardShape,
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Text(
                count.toString(),
                color = if (isSystemInDarkTheme()) Color(0xFF80E3A7) else Color(0xFFDFF2E4),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HealthScreenPreview() {
    FarmOsTheme {
        HealthScreen(
            cases = listOf(
                HealthCase(
                    id = "1",
                    mainComplaint = "Mild fever & loss of appetite",
                    status = HealthCaseStatus.ACTIVE,
                    createdDateMillis = System.currentTimeMillis()
                ),
                HealthCase(
                    id = "2",
                    mainComplaint = "Lameness in hind leg",
                    status = HealthCaseStatus.RECOVERED,
                    createdDateMillis = System.currentTimeMillis() - 86400000L * 5
                )
            ),
            onAddCase = {},
            onCaseDetail = {},
            onBack = {}
        )
    }
}
