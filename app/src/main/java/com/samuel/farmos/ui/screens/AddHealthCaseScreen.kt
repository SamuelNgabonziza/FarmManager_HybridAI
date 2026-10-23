package com.samuel.farmos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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

@OptIn(ExperimentalMaterial3Api::class)
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
                title = { Text("New Health Case", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = ::save, enabled = canSave) {
                        Text("Save", fontWeight = FontWeight.Bold, color = if (canSave) FarmUi.green else FarmUi.textMuted)
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                border = BorderStroke(1.dp, FarmUi.cardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (animals.isEmpty()) {
                        FarmFormLabel("Animal Tag / ID *")
                        OutlinedTextField(
                            value = animalTag,
                            onValueChange = { animalTag = it },
                            placeholder = { Text("Enter animal tag") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )
                    } else {
                        FarmFormLabel("Animal")
                        ExposedDropdownMenuBox(expanded = animalMenuExpanded, onExpandedChange = { animalMenuExpanded = !animalMenuExpanded }) {
                            OutlinedTextField(
                                value = selectedAnimal?.let { "${it.tagNumber} · ${it.name}" }.orEmpty(),
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Select an animal") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = animalMenuExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = FarmUi.fieldShape,
                                colors = FarmUi.textFieldColors()
                            )
                            ExposedDropdownMenu(expanded = animalMenuExpanded, onDismissRequest = { animalMenuExpanded = false }) {
                                animals.forEach { animal ->
                                    DropdownMenuItem(
                                        text = { Text("${animal.tagNumber} · ${animal.name.ifBlank { animal.breed }}") },
                                        onClick = { selectedAnimalId = animal.id; animalMenuExpanded = false }
                                    )
                                }
                            }
                        }
                    }

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

                    FarmFormLabel("Duration (days)")
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it.filter(Char::isDigit) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )

                    FarmFormLabel("Temperature (°C, optional)")
                    OutlinedTextField(
                        value = temperatureText,
                        onValueChange = { temperatureText = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )

                    ChoiceRow("Appetite", listOf("Normal" to Appetite.NORMAL, "Reduced" to Appetite.POOR, "None" to Appetite.NONE), appetite) { appetite = it }
                    ChoiceRow("Water Intake", listOf("Normal" to WaterIntake.NORMAL, "Reduced" to WaterIntake.REDUCED, "Increased" to WaterIntake.INCREASED), waterIntake) { waterIntake = it }

                    FarmFormLabel("Clinical Signs (select all that apply)")
                    val signRows = listOf(
                        listOf("Fever" to ClinicalSign.FEVER, "Cough" to ClinicalSign.COUGHING),
                        listOf("Diarrhea" to ClinicalSign.DIARRHEA, "Weakness" to ClinicalSign.WEAKNESS),
                        listOf("Swelling" to ClinicalSign.SWELLING, "Nasal discharge" to ClinicalSign.NASAL_DISCHARGE),
                        listOf("Skin lesions" to ClinicalSign.SKIN_LESIONS, "Difficulty breathing" to ClinicalSign.DIFFICULTY_BREATHING)
                    )
                    signRows.forEach { pair ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            pair.forEach { (label, sign) ->
                                Row(
                                    Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = sign in selectedSigns,
                                        onCheckedChange = { checked ->
                                            selectedSigns = if (checked) selectedSigns + sign else selectedSigns - sign
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = FarmUi.green)
                                    )
                                    Text(label, style = MaterialTheme.typography.bodyMedium, color = FarmUi.ink)
                                }
                            }
                        }
                    }

                    FarmFormLabel("Additional Notes (optional)")
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> ChoiceRow(title: String, choices: List<Pair<String, T>>, selected: T, onSelected: (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FarmFormLabel(title)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            choices.forEach { (label, value) ->
                FilterChip(
                    selected = selected == value,
                    onClick = { onSelected(value) },
                    label = { Text(label) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddHealthCaseScreenPreview() {
    FarmOsTheme { AddHealthCaseScreen(animals = listOf(Animal(id = "1", tagNumber = "101", name = "Bella")), onSave = {}, onBack = {}) }
}
