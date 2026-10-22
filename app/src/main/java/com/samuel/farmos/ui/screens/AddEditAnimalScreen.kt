package com.samuel.farmos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.Animal
import com.samuel.farmos.data.AnimalStatus
import com.samuel.farmos.data.BreedSuggestions
import com.samuel.farmos.data.Gender
import com.samuel.farmos.data.Species
import com.samuel.farmos.ui.components.FarmDatePickerField
import com.samuel.farmos.ui.theme.FarmOsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAnimalScreen(
    animal: Animal? = null,
    onSave: (Animal) -> Unit,
    onDelete: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    var tagNumber by rememberSaveable(animal?.id) { mutableStateOf(animal?.tagNumber ?: "") }
    var name by rememberSaveable(animal?.id) { mutableStateOf(animal?.name ?: "") }
    var species by rememberSaveable(animal?.id) { mutableStateOf(animal?.species ?: Species.COW) }
    var breed by rememberSaveable(animal?.id) { mutableStateOf(animal?.breed ?: "") }
    var gender by rememberSaveable(animal?.id) { mutableStateOf(animal?.gender ?: Gender.FEMALE) }
    var status by rememberSaveable(animal?.id) { mutableStateOf(animal?.status ?: AnimalStatus.HEALTHY) }
    var weightKgText by rememberSaveable(animal?.id) { mutableStateOf(animal?.weightKg?.toString() ?: "") }
    var isCastrated by rememberSaveable(animal?.id) { mutableStateOf(animal?.isCastrated) }
    var birthDate by rememberSaveable(animal?.id) { mutableStateOf(animal?.birthDate) }

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
            isCastrated = it.isCastrated
            birthDate = it.birthDate
        }
    }

    var breedExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    var tagNumberError by remember { mutableStateOf(false) }
    var weightError by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showSaveConfirm by remember { mutableStateOf(false) }

    val weightValue = weightKgText.toDoubleOrNull()
    val isWeightValid = weightKgText.isBlank() || (weightValue != null && weightValue > 0.0)
    fun saveAnimal() {
        onSave((animal ?: Animal()).copy(
            tagNumber = tagNumber.trim(), name = name.trim(), species = species, breed = breed.trim(),
            gender = gender, isCastrated = if (gender == Gender.MALE) isCastrated else null,
            birthDate = birthDate, status = status, weightKg = weightValue
        ))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (animal == null) "Add Animal" else "Edit Animal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        val trimmedTag = tagNumber.trim()
                        tagNumberError = trimmedTag.isEmpty()
                        weightError = !isWeightValid
                        if (tagNumberError || weightError) return@TextButton
                        showSaveConfirm = true
                    }) { Text("Save", fontWeight = FontWeight.Bold) }
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
                        Text("Species", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(Species.COW, Species.GOAT, Species.SHEEP).forEach { option ->
                                FilterChip(
                                    selected = species == option,
                                    onClick = { species = option; breed = "" },
                                    label = { Text(when (option) { Species.COW -> "🐄 Cow"; Species.GOAT -> "🐐 Goat"; else -> "🐑 Sheep" }) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = tagNumber,
                            onValueChange = {
                                tagNumber = it
                                if (it.isNotBlank()) tagNumberError = false
                            },
                            label = { Text("Tag / ID Number *") },
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

                        Text("Gender", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Gender.values().forEach { option ->
                                FilterChip(
                                    selected = gender == option,
                                    onClick = { gender = option; if (option == Gender.FEMALE) isCastrated = null },
                                    label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }

                        if (gender == Gender.MALE) {
                            Text("Castrated", fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(false to "No", true to "Yes").forEach { (value, label) ->
                                    FilterChip(
                                        selected = isCastrated == value,
                                        onClick = { isCastrated = value },
                                        label = { Text(label) }
                                    )
                                }
                            }
                        }

                        FarmDatePickerField(
                            label = "Birth Date",
                            initialMillis = birthDate,
                            onDateSelected = { birthDate = it }
                        )

                        ExposedDropdownMenuBox(
                            expanded = statusExpanded,
                            onExpandedChange = { statusExpanded = !statusExpanded }
                        ) {
                            OutlinedTextField(
                                value = status.name.lowercase().replaceFirstChar { it.uppercase() },
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
                                        text = { Text(st.name.lowercase().replaceFirstChar { it.uppercase() }) },
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

    if (showSaveConfirm) {
        AlertDialog(
            onDismissRequest = { showSaveConfirm = false },
            title = { Text("Save Animal?") },
            text = { Text("Are you sure you want to save this animal with Tag #${tagNumber.trim()}?") },
            confirmButton = {
                TextButton(onClick = { showSaveConfirm = false; saveAnimal() }) {
                    Text("Yes, Save", color = FarmUi.green, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showSaveConfirm = false }) { Text("Cancel") } },
            containerColor = MaterialTheme.colorScheme.surface
        )
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
