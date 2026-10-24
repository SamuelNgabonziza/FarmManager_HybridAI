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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.Animal
import com.samuel.farmos.data.Appetite
import com.samuel.farmos.data.ClinicalSign
import com.samuel.farmos.data.HealthCase
import com.samuel.farmos.data.WaterIntake
import com.samuel.farmos.ui.theme.FarmOsTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddHealthCaseScreen(
    animals: List<Animal> = emptyList(),
    onSave: (HealthCase) -> Unit,
    onBack: () -> Unit
) {
    var selectedAnimalId by rememberSaveable { mutableStateOf("") }
    var animalTag by rememberSaveable { mutableStateOf("") }
    var mainComplaint by rememberSaveable { mutableStateOf("") }
    var durationText by rememberSaveable { mutableStateOf("") }
    var temperatureText by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var appetite by rememberSaveable { mutableStateOf(Appetite.NORMAL) }
    var waterIntake by rememberSaveable { mutableStateOf(WaterIntake.NORMAL) }
    var selectedSigns by remember { mutableStateOf(setOf<ClinicalSign>()) }
    var animalMenuExpanded by remember { mutableStateOf(false) }
    val selectedAnimal = animals.firstOrNull { it.id == selectedAnimalId }
    val animalId = selectedAnimal?.id ?: animalTag.trim()
    val canSave = animalId.isNotBlank() && mainComplaint.isNotBlank()

    fun save() {
        if (!canSave) return
        onSave(
            HealthCase(
                linkedAnimalId = animalId,
                mainComplaint = mainComplaint.trim(),
                durationDays = durationText.toIntOrNull(),
                temperatureCelsius = temperatureText.toDoubleOrNull(),
                appetite = appetite,
                waterIntake = waterIntake,
                clinicalSigns = selectedSigns.toList(),
                notes = notes.trim()
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Health Case", fontWeight = FontWeight.Bold, color = FarmUi.green) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = FarmUi.green)
                    }
                },
                actions = {
                    TextButton(onClick = ::save, enabled = canSave) {
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.Bold,
                            color = if (canSave) FarmUi.green else FarmUi.textMuted
                        )
                    }
                },
                colors = FarmUi.topAppBarColors()
            )
        },
        containerColor = FarmUi.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF075C43), Color(0xFF009D8D))
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Record Health Case 🩺",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Log symptoms & observations for AI diagnosis",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.16f),
                        shape = CircleShape
                    ) {
                        Box(
                            modifier = Modifier.size(46.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Animal & Complaint Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                border = BorderStroke(1.dp, FarmUi.teal.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(FarmUi.iconTeal, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Pets,
                                contentDescription = null,
                                tint = FarmUi.teal,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Animal & Main Complaint",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = FarmUi.ink
                        )
                    }

                    if (animals.isEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FarmFormLabel("Animal Tag / ID *")
                            OutlinedTextField(
                                value = animalTag,
                                onValueChange = { animalTag = it },
                                placeholder = { Text("Enter animal tag or ID") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = FarmUi.fieldShape,
                                colors = FarmUi.textFieldColors()
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FarmFormLabel("Animal *")
                            ExposedDropdownMenuBox(
                                expanded = animalMenuExpanded,
                                onExpandedChange = { animalMenuExpanded = !animalMenuExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedAnimal?.let { "${it.tagNumber} · ${it.name.ifBlank { it.breed }}" }.orEmpty(),
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = { Text("Select an animal") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = animalMenuExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = FarmUi.fieldShape,
                                    colors = FarmUi.textFieldColors()
                                )
                                ExposedDropdownMenu(
                                    expanded = animalMenuExpanded,
                                    onDismissRequest = { animalMenuExpanded = false }
                                ) {
                                    animals.forEach { animal ->
                                        DropdownMenuItem(
                                            text = { Text("${animal.tagNumber} · ${animal.name.ifBlank { animal.breed }}") },
                                            onClick = {
                                                selectedAnimalId = animal.id
                                                animalMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FarmFormLabel("Main Complaint *")
                        OutlinedTextField(
                            value = mainComplaint,
                            onValueChange = { mainComplaint = it },
                            placeholder = { Text("e.g. Swollen lymph nodes, fever, weakness") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )
                    }
                }
            }

            // Clinical Vitals Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                border = BorderStroke(1.dp, FarmUi.cardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FarmFormLabel("Duration (days)")
                            OutlinedTextField(
                                value = durationText,
                                onValueChange = { durationText = it.filter(Char::isDigit) },
                                placeholder = { Text("e.g. 2") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = FarmUi.fieldShape,
                                colors = FarmUi.textFieldColors()
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FarmFormLabel("Temp (°C)")
                            OutlinedTextField(
                                value = temperatureText,
                                onValueChange = { temperatureText = it },
                                placeholder = { Text("e.g. 39.5") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = FarmUi.fieldShape,
                                colors = FarmUi.textFieldColors()
                            )
                        }
                    }
                }
            }

            // Appetite & Hydration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                border = BorderStroke(1.dp, FarmUi.cardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(FarmUi.iconPeach, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = FarmUi.orange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Appetite & Water Intake",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = FarmUi.ink
                        )
                    }

                    ChoicePills(
                        title = "Appetite",
                        choices = listOf(
                            Triple("Normal", "🥗", Appetite.NORMAL),
                            Triple("Reduced", "📉", Appetite.POOR),
                            Triple("None", "❌", Appetite.NONE)
                        ),
                        selected = appetite,
                        onSelected = { appetite = it }
                    )

                    ChoicePills(
                        title = "Water Intake",
                        choices = listOf(
                            Triple("Normal", "💧", WaterIntake.NORMAL),
                            Triple("Reduced", "📉", WaterIntake.REDUCED),
                            Triple("Increased", "📈", WaterIntake.INCREASED)
                        ),
                        selected = waterIntake,
                        onSelected = { waterIntake = it }
                    )
                }
            }

            // Clinical Signs Chips
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                border = BorderStroke(1.dp, FarmUi.cardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(FarmUi.iconTeal, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = FarmUi.teal,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Observed Symptoms",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = FarmUi.ink
                            )
                            Text(
                                text = "Tap all signs currently present on the animal",
                                style = MaterialTheme.typography.bodySmall,
                                color = FarmUi.textMuted
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClinicalSign.entries.forEach { sign ->
                            val isSelected = sign in selectedSigns
                            Surface(
                                onClick = {
                                    selectedSigns = if (isSelected) selectedSigns - sign else selectedSigns + sign
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) FarmUi.iconTeal else FarmUi.surfaceWarm,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) FarmUi.teal else FarmUi.cardBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = FarmUi.teal,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = formatClinicalSign(sign),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) FarmUi.teal else FarmUi.ink
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Notes Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                border = BorderStroke(1.dp, FarmUi.cardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(FarmUi.iconMint, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Assignment,
                                contentDescription = null,
                                tint = FarmUi.green,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Notes & Immediate Treatments",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = FarmUi.ink
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("e.g. Isolated animal, given 5ml antibiotic shot") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )
                }
            }

            // Primary Save Action
            Button(
                onClick = ::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = canSave,
                shape = RoundedCornerShape(16.dp),
                colors = FarmUi.buttonColors()
            ) {
                Text(
                    text = "Save Health Case",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun <T> ChoicePills(
    title: String,
    choices: List<Triple<String, String, T>>,
    selected: T,
    onSelected: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FarmFormLabel(title)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            choices.forEach { (label, emoji, value) ->
                val isSelected = selected == value
                Surface(
                    onClick = { onSelected(value) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) FarmUi.iconMint else FarmUi.surfaceWarm,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) FarmUi.green else FarmUi.cardBorder
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$emoji $label",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) FarmUi.green else FarmUi.ink
                        )
                    }
                }
            }
        }
    }
}

private fun formatClinicalSign(sign: ClinicalSign): String {
    return when (sign) {
        ClinicalSign.FEVER -> "Fever 🌡️"
        ClinicalSign.LOSS_OF_APPETITE -> "Loss of Appetite 🥗"
        ClinicalSign.DIARRHEA -> "Diarrhea 💧"
        ClinicalSign.COUGHING -> "Coughing 😷"
        ClinicalSign.LETHARGY -> "Lethargy 😴"
        ClinicalSign.WOUND -> "Wound 🩹"
        ClinicalSign.LAMENESS -> "Lameness 🐾"
        ClinicalSign.SWELLING -> "Swelling 🩹"
        ClinicalSign.NASAL_DISCHARGE -> "Nasal Discharge 🤧"
        ClinicalSign.SKIN_LESIONS -> "Skin Lesions 🩹"
        ClinicalSign.DIFFICULTY_BREATHING -> "Diff. Breathing 🫁"
        ClinicalSign.WEAKNESS -> "Weakness 🛏️"
        ClinicalSign.OTHER -> "Other ❓"
    }
}

@Preview(showBackground = true)
@Composable
fun AddHealthCaseScreenPreview() {
    FarmOsTheme {
        AddHealthCaseScreen(
            animals = listOf(
                Animal(id = "1", tagNumber = "101", name = "Bella", breed = "Friesian"),
                Animal(id = "2", tagNumber = "102", name = "Daisy", breed = "Jersey")
            ),
            onSave = {},
            onBack = {}
        )
    }
}
