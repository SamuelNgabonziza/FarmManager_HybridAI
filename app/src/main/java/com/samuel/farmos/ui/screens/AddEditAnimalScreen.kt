package com.samuel.farmos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
                    }) {
                        Text("Save", fontWeight = FontWeight.Bold, color = FarmUi.green)
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
                    FarmFormLabel("Species")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(Species.COW to "🐄 Cow", Species.GOAT to "🐐 Goat", Species.SHEEP to "🐑 Sheep").forEach { (option, label) ->
                            val selected = species == option
                            val selectedBg = if (isSystemInDarkTheme()) Color(0xFF80E3A7) else Color(0xFF26733A)
                            val selectedText = if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(role = Role.Tab) {
                                        species = option
                                        breed = ""
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) selectedBg else FarmUi.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) selectedBg else FarmUi.cardBorder
                                )
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    color = if (selected) selectedText else FarmUi.ink,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    style = MaterialTheme.typography.titleSmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    FarmFormLabel("Tag / ID Number *")
                    OutlinedTextField(
                        value = tagNumber,
                        onValueChange = {
                            tagNumber = it
                            if (it.isNotBlank()) tagNumberError = false
                        },
                        placeholder = { Text("e.g. C-101 or EAR-042") },
                        isError = tagNumberError,
                        supportingText = if (tagNumberError) {
                            { Text("Tag number is required") }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )

                    FarmFormLabel("Name (optional)")
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("e.g. Bella, Daisy") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )

                    FarmFormLabel("Breed")
                    val breedOptions = BreedSuggestions.forSpecies(species)
                    if (breedOptions.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = breedExpanded,
                            onExpandedChange = { breedExpanded = !breedExpanded }
                        ) {
                            OutlinedTextField(
                                value = breed,
                                onValueChange = { breed = it },
                                placeholder = { Text("Type or pick a breed") },
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
                            placeholder = { Text("Type or pick a breed") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )
                    }

                    FarmFormLabel("Gender")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Gender.values().forEach { option ->
                            val selected = gender == option
                            val selectedBg = if (isSystemInDarkTheme()) Color(0xFF80E3A7) else Color(0xFF26733A)
                            val selectedText = if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(role = Role.Tab) {
                                        gender = option
                                        if (option == Gender.FEMALE) isCastrated = null
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) selectedBg else FarmUi.surface,
                                border = BorderStroke(1.dp, if (selected) selectedBg else FarmUi.cardBorder)
                            ) {
                                Text(
                                    text = option.name.lowercase().replaceFirstChar { it.uppercase() },
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    color = if (selected) selectedText else FarmUi.ink,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    style = MaterialTheme.typography.titleSmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    if (gender == Gender.MALE) {
                        FarmFormLabel("Castrated?")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(false to "No", true to "Yes").forEach { (value, label) ->
                                val selected = isCastrated == value
                                val selectedBg = if (isSystemInDarkTheme()) Color(0xFF80E3A7) else Color(0xFF26733A)
                                val selectedText = if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(role = Role.Tab) { isCastrated = value },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) selectedBg else FarmUi.surface,
                                    border = BorderStroke(1.dp, if (selected) selectedBg else FarmUi.cardBorder)
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        color = if (selected) selectedText else FarmUi.ink,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        style = MaterialTheme.typography.titleSmall,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    FarmDatePickerField(
                        label = "Birth Date",
                        initialMillis = birthDate,
                        onDateSelected = { birthDate = it }
                    )

                    FarmFormLabel("Weight (kg)")
                    OutlinedTextField(
                        value = weightKgText,
                        onValueChange = {
                            weightKgText = it
                            weightError = it.isNotBlank() && it.toDoubleOrNull() == null
                        },
                        placeholder = { Text("e.g. 350.5") },
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

                    FarmFormLabel("Status")
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded }
                    ) {
                        OutlinedTextField(
                            value = status.name.lowercase().replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
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

                    if (onDelete != null) {
                        Spacer(modifier = Modifier.height(8.dp))
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

    if (showSaveConfirm) {
        AlertDialog(
            onDismissRequest = { showSaveConfirm = false },
            title = {
                Text(
                    "Save Animal?",
                    fontWeight = FontWeight.Bold,
                    color = FarmUi.green
                )
            },
            text = {
                Text(
                    "Are you sure you want to save this animal with Tag #${tagNumber.trim()}?",
                    color = FarmUi.ink
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveConfirm = false
                        saveAnimal()
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = FarmUi.buttonColors()
                ) {
                    Text("Yes, Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirm = false }) {
                    Text("Cancel", color = FarmUi.textMuted)
                }
            },
            containerColor = FarmUi.dialogSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text("Delete animal?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            },
            text = {
                Text(
                    if (tagNumber.isNotBlank())
                        "This will permanently remove ${tagNumber.trim()} and its records. This cannot be undone."
                    else
                        "This will permanently remove this animal and its records. This cannot be undone.",
                    color = FarmUi.ink
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = FarmUi.textMuted)
                }
            },
            containerColor = FarmUi.dialogSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}
