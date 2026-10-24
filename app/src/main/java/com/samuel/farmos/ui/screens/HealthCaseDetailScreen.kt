package com.samuel.farmos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.Appetite
import com.samuel.farmos.data.ClinicalSign
import com.samuel.farmos.data.HealthCase
import com.samuel.farmos.data.HealthCaseStatus
import com.samuel.farmos.data.WaterIntake
import com.samuel.farmos.ui.components.FarmDeleteButton
import com.samuel.farmos.ui.theme.FarmOsTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Case not found",
                        modifier = Modifier.padding(16.dp),
                        color = FarmUi.textMuted,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Main Case Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                        border = BorderStroke(1.dp, FarmUi.coral.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(FarmUi.iconCoral, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = FarmUi.coral,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = healthCase.mainComplaint,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = FarmUi.ink
                                    )
                                    val dateStr = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(Date(healthCase.createdDateMillis))
                                    Text(
                                        text = "Logged: $dateStr",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FarmUi.textMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Animal: ${healthCase.linkedAnimalId.ifBlank { "General / Whole Farm" }}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FarmUi.ink
                                )

                                val statusColor = when (healthCase.status) {
                                    HealthCaseStatus.ACTIVE -> FarmUi.coral
                                    HealthCaseStatus.CRITICAL -> FarmUi.orange
                                    HealthCaseStatus.RECOVERED -> FarmUi.green
                                    HealthCaseStatus.CLOSED -> FarmUi.textMuted
                                }
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
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Observations Grid
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                        border = BorderStroke(1.dp, FarmUi.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(FarmUi.iconWarm, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = FarmUi.amber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Clinical Observations",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FarmUi.ink
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ObservationCard(
                                    label = "Temperature",
                                    value = healthCase.temperatureCelsius?.let { "$it °C" } ?: "Normal",
                                    iconEmoji = "🌡️",
                                    modifier = Modifier.weight(1f)
                                )
                                ObservationCard(
                                    label = "Duration",
                                    value = healthCase.durationDays?.let { "$it day${if (it == 1) "" else "s"}" } ?: "1 day",
                                    iconEmoji = "⏱️",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ObservationCard(
                                    label = "Appetite",
                                    value = healthCase.appetite.name.lowercase().replaceFirstChar { it.uppercase() },
                                    iconEmoji = "🥗",
                                    modifier = Modifier.weight(1f)
                                )
                                ObservationCard(
                                    label = "Water Intake",
                                    value = healthCase.waterIntake.name.lowercase().replaceFirstChar { it.uppercase() },
                                    iconEmoji = "💧",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Clinical Signs Chips
                    if (healthCase.clinicalSigns.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                            border = BorderStroke(1.dp, FarmUi.cardBorder)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(FarmUi.iconCoral, RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.MedicalServices,
                                            contentDescription = null,
                                            tint = FarmUi.coral,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Observed Signs",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = FarmUi.ink
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    healthCase.clinicalSigns.forEach { sign ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = FarmUi.iconCoral,
                                            border = BorderStroke(1.dp, FarmUi.coral.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = sign.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() },
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = FarmUi.coral,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Notes / Treatments Card
                    if (healthCase.notes.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                            border = BorderStroke(1.dp, FarmUi.cardBorder)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(FarmUi.iconWarm, RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Assignment,
                                            contentDescription = null,
                                            tint = FarmUi.orange,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Notes & Treatments",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = FarmUi.ink
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = healthCase.notes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FarmUi.ink
                                )
                            }
                        }
                    }

                    // AI Symptom Analysis Card
                    if (!healthCase.aiAnalysis.isNullOrBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = FarmUi.surfaceTeal),
                            border = BorderStroke(1.dp, FarmUi.teal.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(FarmUi.iconTeal, RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = FarmUi.teal,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "AI Symptom Diagnosis",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = FarmUi.teal
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = healthCase.aiAnalysis.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FarmUi.ink
                                )
                            }
                        }
                    }

                    // Status Update Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                        border = BorderStroke(1.dp, FarmUi.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(FarmUi.iconMint, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = FarmUi.green,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Update Case Status",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FarmUi.ink
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            HealthCaseStatus.values().forEach { st ->
                                Button(
                                    onClick = { onUpdateStatus(st) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    enabled = healthCase.status != st,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = FarmUi.buttonColors()
                                ) {
                                    Text(
                                        text = "Mark as ${st.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                        fontWeight = FontWeight.Bold
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
private fun ObservationCard(
    label: String,
    value: String,
    iconEmoji: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = FarmUi.surfaceWarm,
        border = BorderStroke(1.dp, FarmUi.cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$iconEmoji $label",
                style = MaterialTheme.typography.labelSmall,
                color = FarmUi.textMuted
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = FarmUi.ink
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HealthCaseDetailScreenPreview() {
    FarmOsTheme {
        HealthCaseDetailScreen(
            healthCase = HealthCase(
                id = "1",
                mainComplaint = "Mild fever & loss of appetite",
                status = HealthCaseStatus.ACTIVE,
                durationDays = 2,
                temperatureCelsius = 39.5,
                appetite = Appetite.POOR,
                waterIntake = WaterIntake.REDUCED,
                clinicalSigns = listOf(ClinicalSign.FEVER, ClinicalSign.LOSS_OF_APPETITE, ClinicalSign.LETHARGY),
                notes = "Given antibiotic shot by vet on Tuesday.",
                aiAnalysis = "High likelihood of tick-borne infection. Monitor temperature daily."
            ),
            onUpdateStatus = {},
            onBack = {}
        )
    }
}
