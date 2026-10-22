package com.samuel.farmos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.BreedingEventType
import com.samuel.farmos.data.BreedingRecord
import com.samuel.farmos.data.Animal
import com.samuel.farmos.ui.components.FarmDatePickerField
import com.samuel.farmos.ui.theme.FarmOsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBreedingScreen(
    animals: List<Animal> = emptyList(),
    onSave: (BreedingRecord) -> Unit,
    onBack: () -> Unit
) {
    var eventType by remember { mutableStateOf(BreedingEventType.HEAT) }
    var notes by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }
    var animalId by remember { mutableStateOf("") }
    var animalExpanded by remember { mutableStateOf(false) }
    var eventDate by remember { mutableStateOf(System.currentTimeMillis()) }
    fun save() = onSave(BreedingRecord(linkedAnimalId = animalId, eventType = eventType, notes = notes.trim(), eventDateMillis = eventDate))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Breeding Event", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = { TextButton(onClick = ::save, enabled = animals.isEmpty() || animalId.isNotBlank()) { Text("Save", fontWeight = FontWeight.Bold) } },
                colors = FarmUi.topAppBarColors()
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = FarmUi.background
        ) {
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
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExposedDropdownMenuBox(expanded = animalExpanded, onExpandedChange = { animalExpanded = !animalExpanded }) {
                            OutlinedTextField(
                                value = animals.firstOrNull { it.id == animalId }?.let { "${it.tagNumber} · ${it.name.ifBlank { it.breed }}" }.orEmpty(),
                                onValueChange = {}, readOnly = true,
                                label = { Text("Animal") }, placeholder = { Text("Select an animal") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = animalExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(), shape = FarmUi.fieldShape, colors = FarmUi.textFieldColors()
                            )
                            ExposedDropdownMenu(expanded = animalExpanded, onDismissRequest = { animalExpanded = false }) {
                                animals.forEach { animal ->
                                    DropdownMenuItem(text = { Text("${animal.tagNumber} · ${animal.name.ifBlank { animal.breed }}") }, onClick = { animalId = animal.id; animalExpanded = false })
                                }
                            }
                        }
                        ExposedDropdownMenuBox(
                            expanded = typeExpanded,
                            onExpandedChange = { typeExpanded = !typeExpanded }
                        ) {
                            OutlinedTextField(
                                value = eventTypeLabel(eventType),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Event Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = FarmUi.fieldShape,
                                colors = FarmUi.textFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = typeExpanded,
                                onDismissRequest = { typeExpanded = false }
                            ) {
                                BreedingEventType.values().forEach { bType ->
                                    DropdownMenuItem(
                                        text = { Text(eventTypeLabel(bType)) },
                                        onClick = {
                                            eventType = bType
                                            typeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Text("Event Date", color = FarmUi.green, fontWeight = FontWeight.SemiBold)
                        FarmDatePickerField(label = "", initialMillis = eventDate, onDateSelected = { eventDate = it })

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )

                    }
                }
            }
        }
    }
}

private fun eventTypeLabel(type: BreedingEventType): String = when (type) {
    BreedingEventType.HEAT -> "Heat Detected"
    BreedingEventType.INSEMINATION -> "Insemination"
    BreedingEventType.PREGNANCY_CHECK -> "Pregnancy Check"
    BreedingEventType.BIRTH -> "Birth"
    BreedingEventType.ABORTION -> "Abortion"
}

@Preview(showBackground = true)
@Composable
fun AddBreedingScreenPreview() {
    FarmOsTheme {
        AddBreedingScreen(
            onSave = {},
            onBack = {}
        )
    }
}
