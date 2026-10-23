package com.samuel.farmos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.HealthCase
import com.samuel.farmos.data.HealthCaseStatus
import com.samuel.farmos.ui.components.FarmDeleteButton
import com.samuel.farmos.ui.theme.FarmOsTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthCaseDetailScreen(
    healthCase: HealthCase?,
    onUpdateStatus: (HealthCaseStatus) -> Unit,
    onDelete: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Case Details", fontWeight = FontWeight.Bold, color = FarmUi.green) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = FarmUi.green)
                    }
                },
                actions = {
                    if (healthCase != null && onDelete != null) {
                        FarmDeleteButton(
                            description = "this health case",
                            onDelete = onDelete
                        )
                    }
                },
                colors = FarmUi.topAppBarColors()
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
            if (healthCase == null) {
                Text(
                    text = "Case not found",
                    modifier = Modifier.padding(16.dp),
                    color = FarmUi.textMuted
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                        border = BorderStroke(1.dp, FarmUi.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = healthCase.mainComplaint,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = FarmUi.green
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val dateStr = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(Date(healthCase.createdDateMillis))
                            Text("Animal Tag: ${healthCase.linkedAnimalId}", style = MaterialTheme.typography.bodyLarge, color = FarmUi.ink)
                            Text("Logged: $dateStr", style = MaterialTheme.typography.bodySmall, color = FarmUi.textMuted)
                            Text("Status: ${healthCase.status.name}", style = MaterialTheme.typography.bodyMedium, color = FarmUi.green, fontWeight = FontWeight.SemiBold)

                            if (healthCase.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Notes / Treatments:", fontWeight = FontWeight.Bold, color = FarmUi.ink)
                                Text(healthCase.notes, style = MaterialTheme.typography.bodyMedium, color = FarmUi.ink)
                            }

                            if (!healthCase.aiAnalysis.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("AI Analysis:", fontWeight = FontWeight.Bold, color = FarmUi.green)
                                Text(healthCase.aiAnalysis!!, style = MaterialTheme.typography.bodyMedium, color = FarmUi.ink)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                        border = BorderStroke(1.dp, FarmUi.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Update Case Status", fontWeight = FontWeight.Bold, color = FarmUi.ink)
                            Spacer(modifier = Modifier.height(8.dp))

                            HealthCaseStatus.values().forEach { st ->
                                Button(
                                    onClick = { onUpdateStatus(st) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    enabled = healthCase.status != st,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FarmUi.buttonColors()
                                ) {
                                    Text("Mark as ${st.name.lowercase().replaceFirstChar { it.uppercase() }}", fontWeight = FontWeight.Bold)
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
fun HealthCaseDetailScreenPreview() {
    FarmOsTheme {
        HealthCaseDetailScreen(
            healthCase = HealthCase(id = "1", mainComplaint = "Limping left leg", status = HealthCaseStatus.ACTIVE),
            onUpdateStatus = {},
            onBack = {}
        )
    }
}
