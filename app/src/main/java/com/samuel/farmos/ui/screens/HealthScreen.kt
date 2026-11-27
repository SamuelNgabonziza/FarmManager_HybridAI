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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.Animal
import com.samuel.farmos.data.HealthCase
import com.samuel.farmos.data.HealthCaseStatus
import com.samuel.farmos.ui.components.CowSymptomDiagnosticView
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
    onBack: () -> Unit,
    animals: List<Animal> = emptyList(),
    farmName: String = "My Farm"
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val active = cases.count { it.status == HealthCaseStatus.ACTIVE }
    val resolved = cases.count { it.status == HealthCaseStatus.RECOVERED || it.status == HealthCaseStatus.CLOSED }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(FarmUi.surface)) {
                TopAppBar(
                    title = { Text("Health & Veterinary", fontWeight = FontWeight.Bold, color = FarmUi.green) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = FarmUi.green)
                        }
                    },
                    colors = FarmUi.topAppBarColors()
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = FarmUi.surface,
                    contentColor = FarmUi.green,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = FarmUi.green
                            )
                        }
                    },
                    divider = {
                        HorizontalDivider(color = FarmUi.cardBorder)
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Cases ($active)",
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
                                "🩺 Vet Triage Desk",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) FarmUi.green else FarmUi.textMuted
                            )
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddCase,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Report Illness", fontWeight = FontWeight.Bold) },
                containerColor = FarmUi.teal,
                contentColor = if (isSystemInDarkTheme()) Color(0xFF1D3530) else Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        },
        containerColor = FarmUi.background
    ) { padding ->
        if (selectedTab == 0) {
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
                    HealthCountCard("⚡ Active", active, Modifier.weight(1f))
                    HealthCountCard("🟢 Resolved", resolved, Modifier.weight(1f))
                }

                if (cases.isEmpty()) {
                    FarmEmptyState(
                        icon = Icons.Default.Favorite,
                        title = "No active health cases",
                        message = "Tap + Report Illness to log symptoms, treatments, or vet follow-ups."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().clipToBounds(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(cases, key = { it.id }) { healthCase ->
                            val isActive = healthCase.status == HealthCaseStatus.ACTIVE
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(role = Role.Button) { onCaseDetail(healthCase.id) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isActive) FarmUi.surfaceTeal else FarmUi.surface),
                                border = BorderStroke(1.dp, if (isActive) FarmUi.teal.copy(alpha = 0.35f) else FarmUi.cardBorder)
                            ) {
                                Column(Modifier.padding(18.dp)) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .background(if (isActive) FarmUi.iconTeal else FarmUi.iconMint, RoundedCornerShape(14.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Favorite,
                                                contentDescription = null,
                                                tint = if (isActive) FarmUi.teal else FarmUi.green,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = healthCase.mainComplaint,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = FarmUi.ink
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            val date = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(healthCase.createdDateMillis))
                                            Text(
                                                "Animal: ${healthCase.linkedAnimalId.ifBlank { "General" }} · Logged: $date",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = FarmUi.textMuted
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val statusColor = if (isActive) FarmUi.teal else FarmUi.green
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
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                item {
                    CowSymptomDiagnosticView(
                        animals = animals,
                        farmName = farmName,
                        onReportCaseWithDetails = { complaint, animalId ->
                            onAddCase()
                        }
                    )
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
