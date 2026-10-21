package com.samuel.farmos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.Animal
import com.samuel.farmos.data.AnimalStatus
import com.samuel.farmos.data.BreedSuggestions
import com.samuel.farmos.data.Gender
import com.samuel.farmos.data.Species

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAnimalScreen(
    animal: Animal? = null,
    onSave: (Animal) -> Unit,
    onDelete: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    var tagNumber by remember { mutableStateOf(animal?.tagNumber ?: "") }
    var name by remember { mutableStateOf(animal?.name ?: "") }
    var species by remember { mutableStateOf(animal?.species ?: Species.COW) }
    var breed by remember { mutableStateOf(animal?.breed ?: "") }
    var gender by remember { mutableStateOf(animal?.gender ?: Gender.FEMALE) }
    var status by remember { mutableStateOf(animal?.status ?: AnimalStatus.HEALTHY) }
    var weightKgText by remember { mutableStateOf(animal?.weightKg?.toString() ?: "") }

    // The animal arrives asynchronously from Firestore, so the form is populated
    // once the document for this id shows up. Re-runs only when the id changes,
    // so it never overwrites what the user is typing.
    LaunchedEffect(animal?.id) {
        animal?.let {
            tagNumber = it.tagNumber
            name = it.name
            species = it.species
            breed = it.breed
            gender = it.gender
            status = it.status
            weightKgText = it.weightKg?.toString() ?: ""
        }
    }

    var speciesExpanded by remember { mutableStateOf(false) }
    var breedExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    var tagNumberError by remember { mutableStateOf(false) }
    var weightError by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val weightValue = weightKgText.toDoubleOrNull()
    val isWeightValid = weightKgText.isBlank() || (weightValue != null && weightValue > 0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (animal == null) "Add Animal" else "Edit Animal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = tagNumber,
                            onValueChange = {
                                tagNumber = it
                                if (it.isNotBlank()) tagNumberError = false
                            },
                            label = { Text("Tag Number *") },
                            isError = tagNumberError,
                            supportingText = if (tagNumberError) {
                                { Text("Tag number is required") }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Animal Name (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )

                        ExposedDropdownMenuBox(
                            expanded = speciesExpanded,
                            onExpandedChange = { speciesExpanded = !speciesExpanded }
                        ) {
                            OutlinedTextField(
                                value = species.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Species") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = speciesExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = FarmUi.fieldShape,
                                colors = FarmUi.textFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = speciesExpanded,
                                onDismissRequest = { speciesExpanded = false }
                            ) {
                                Species.values().forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s.name) },
                                        onClick = {
                                            species = s
                                            breed = ""
                                            speciesExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        val breedOptions = BreedSuggestions.forSpecies(species)
                        if (breedOptions.isNotEmpty()) {
                            ExposedDropdownMenuBox(
                                expanded = breedExpanded,
                                onExpandedChange = { breedExpanded = !breedExpanded }
                            ) {
                                OutlinedTextField(
                                    value = breed,
                                    onValueChange = { breed = it },
                                    label = { Text("Breed") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = breedExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = FarmUi.fieldShape,
                                    colors = FarmUi.textFieldColors()
                                )
                                ExposedDropdownMenu(
                                    expanded = breedExpanded,
                                    onDismissRequest = { breedExpanded = false }
                                ) {
                                    breedOptions.forEach { suggestion ->
                                        DropdownMenuItem(
                                            text = { Text(suggestion) },
                                            onClick = {
                                                breed = suggestion
                                                breedExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = breed,
                                onValueChange = { breed = it },
                                label = { Text("Breed") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = FarmUi.fieldShape,
                                colors = FarmUi.textFieldColors()
                            )
                        }

                        ExposedDropdownMenuBox(
                            expanded = genderExpanded,
                            onExpandedChange = { genderExpanded = !genderExpanded }
                        ) {
                            OutlinedTextField(
                                value = gender.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Gender") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = FarmUi.fieldShape,
                                colors = FarmUi.textFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = genderExpanded,
                                onDismissRequest = { genderExpanded = false }
                            ) {
                                Gender.values().forEach { g ->
                                    DropdownMenuItem(
                                        text = { Text(g.name) },
                                        onClick = {
                                            gender = g
                                            genderExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = statusExpanded,
                            onExpandedChange = { statusExpanded = !statusExpanded }
                        ) {
                            OutlinedTextField(
                                value = status.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Health Status") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = FarmUi.fieldShape,
                                colors = FarmUi.textFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = statusExpanded,
                                onDismissRequest = { statusExpanded = false }
                            ) {
                                AnimalStatus.values().forEach { st ->
                                    DropdownMenuItem(
                                        text = { Text(st.name) },
                                        onClick = {
                                            status = st
                                            statusExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = weightKgText,
                            onValueChange = {
                                weightKgText = it
                                weightError = it.isNotBlank() && it.toDoubleOrNull() == null
                            },
                            label = { Text("Weight (kg)") },
                            isError = weightError,
                            supportingText = if (weightError) {
                                { Text("Enter a valid number") }
                            } else null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val trimmedTag = tagNumber.trim()
                                tagNumberError = trimmedTag.isEmpty()
                                weightError = !isWeightValid
                                if (tagNumberError || weightError) return@Button

                                val updated = (animal ?: Animal()).copy(
                                    tagNumber = trimmedTag,
                                    name = name.trim(),
                                    species = species,
                                    breed = breed.trim(),
                                    gender = gender,
                                    status = status,
                                    weightKg = weightValue
                                )
                                onSave(updated)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = FarmUi.buttonColors()
                        ) {
                            Text("Save Animal")
                        }

                        if (onDelete != null) {
                            Button(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = FarmUi.secondaryButtonColors()
                            ) {
                                Text("Delete Animal")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete animal?") },
            text = {
                Text(
                    if (tagNumber.isNotBlank())
                        "This will permanently remove ${tagNumber.trim()} and its records. This cannot be undone."
                    else
                        "This will permanently remove this animal and its records. This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
