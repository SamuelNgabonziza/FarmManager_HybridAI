package com.samuel.farmos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.ui.platform.LocalContext
import com.samuel.farmos.util.FarmPdfExporter
import com.samuel.farmos.util.FarmShareUtils
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.samuel.farmos.ui.components.FarmDeleteButton
import com.samuel.farmos.ui.theme.FarmOsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAnimalScreen(
    animal: Animal? = null,
    existingAnimals: List<Animal> = emptyList(),
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
    var duplicateTagError by remember { mutableStateOf(false) }
    var weightError by remember { mutableStateOf(false) }
    var showSaveConfirm by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val weightValue = weightKgText.toDoubleOrNull()
    val isWeightValid = weightKgText.isBlank() || (weightValue != null && weightValue > 0.0)

    fun saveAnimal() {
        onSave(
            (animal ?: Animal()).copy(
                tagNumber = tagNumber.trim(),
                name = name.trim(),
                species = species,
                breed = breed.trim(),
                gender = gender,
                isCastrated = if (gender == Gender.MALE) isCastrated else null,
                birthDate = birthDate,
                status = status,
                weightKg = weightValue
            )
        )
    }

    fun validateAndPrompt() {
        val trimmedTag = tagNumber.trim()
        tagNumberError = trimmedTag.isEmpty()
        duplicateTagError = trimmedTag.isNotBlank() && existingAnimals.any {
            it.tagNumber.equals(trimmedTag, ignoreCase = true) && it.id != (animal?.id ?: "")
        }
        weightError = !isWeightValid
        if (tagNumberError || duplicateTagError || weightError) return
        saveAnimal()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (animal == null) "Add Animal" else "Edit Animal", fontWeight = FontWeight.Bold, color = FarmUi.green) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = FarmUi.green)
                    }
                },
                actions = {
                    if (animal != null) {
                        IconButton(onClick = { showShareDialog = true }) {
                            Icon(Icons.Default.Share, contentDescription = "Share with Vet", tint = FarmUi.green)
                        }
                    }
                    TextButton(onClick = ::validateAndPrompt) {
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
            // Section 1: Classification & ID
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
                                Icons.Default.Pets,
                                contentDescription = null,
                                tint = FarmUi.teal,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Livestock Identity",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = FarmUi.ink
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FarmFormLabel("Species")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(Species.COW to "🐄 Cow", Species.GOAT to "🐐 Goat", Species.SHEEP to "🐑 Sheep").forEach { (option, label) ->
                                val isSelected = species == option
                                Surface(
                                    onClick = {
                                        species = option
                                        breed = ""
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) FarmUi.iconMint else FarmUi.surfaceWarm,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) FarmUi.green else FarmUi.cardBorder
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) FarmUi.green else FarmUi.ink,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FarmFormLabel("Tag / ID Number *")
                        OutlinedTextField(
                            value = tagNumber,
                            onValueChange = {
                                tagNumber = it
                                if (it.isNotBlank()) {
                                    tagNumberError = false
                                    duplicateTagError = false
                                }
                            },
                            placeholder = { Text("e.g. C-101 or EAR-042") },
                            isError = tagNumberError || duplicateTagError,
                            supportingText = if (tagNumberError) {
                                { Text("Tag number is required") }
                            } else if (duplicateTagError) {
                                { Text("An animal with tag #${tagNumber.trim()} already exists") }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    }
                }
            }

            // Section 2: Breed & Demographics
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
                            text = "Breed & Gender",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = FarmUi.ink
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FarmFormLabel("Gender")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(Gender.FEMALE to "Female ♀", Gender.MALE to "Male ♂").forEach { (option, label) ->
                                val isSelected = gender == option
                                Surface(
                                    onClick = {
                                        gender = option
                                        if (option == Gender.FEMALE) isCastrated = null
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) FarmUi.iconPeach else FarmUi.surfaceWarm,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) FarmUi.orange else FarmUi.cardBorder
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) FarmUi.orange else FarmUi.ink,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (gender == Gender.MALE) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FarmFormLabel("Castrated?")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(false to "No", true to "Yes").forEach { (value, label) ->
                                    val isSelected = isCastrated == value
                                    Surface(
                                        onClick = { isCastrated = value },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) FarmUi.iconPeach else FarmUi.surfaceWarm,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) FarmUi.orange else FarmUi.cardBorder
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSelected) FarmUi.orange else FarmUi.ink,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Birth & Weight
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                border = BorderStroke(1.dp, FarmUi.cardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)f,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(FarmUi.iconBlue, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = FarmUi.navy,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Birth & Weight Records",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = FarmUi.ink
                        )
                    }

                    FarmDatePickerField(
                        label = "Birth Date",
                        initialMillis = birthDate,
                        onDateSelected = { birthDate = it }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    }
                }
            }

            // Section 4: Lifecycle Status
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
                            text = "Animal Status",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = FarmUi.ink
                        )
                    }

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
                }
            }

            if (animal != null && onDelete != null) {
                FarmDeleteButton(
                    description = "this animal record",
                    onDelete = onDelete
                )
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

    if (showShareDialog && animal != null) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = {
                Text(
                    "Share Animal Record 🩺",
                    fontWeight = FontWeight.Bold,
                    color = FarmUi.green
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Export a verified medical passport or quick summary for animal #${animal.tagNumber}.",
                        color = FarmUi.ink
                    )
                    Button(
                        onClick = {
                            showShareDialog = false
                            val uri = FarmPdfExporter.generateAnimalPassportPdf(
                                context = context,
                                farmName = "FarmOS",
                                animal = animal,
                                healthHistory = emptyList()
                            )
                            if (uri != null) {
                                FarmShareUtils.sharePdf(context, uri, "Share Animal Passport #${animal.tagNumber}")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = FarmUi.buttonColors()
                    ) {
                        Text("📄 Export Clean 1-Page PDF", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            showShareDialog = false
                            FarmShareUtils.shareWhatsAppSummary(
                                context = context,
                                farmName = "FarmOS",
                                animal = animal,
                                healthHistory = emptyList()
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FarmUi.teal,
                            contentColor = Color.White
                        )
                    ) {
                        Text("💬 Share Summary via WhatsApp", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text("Close", color = FarmUi.textMuted)
                }
            },
            containerColor = FarmUi.dialogSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddEditAnimalScreenPreview() {
    FarmOsTheme {
        AddEditAnimalScreen(
            animal = Animal(
                id = "1",
                tagNumber = "C-101",
                name = "Bella",
                species = Species.COW,
                breed = "Friesian",
                gender = Gender.FEMALE,
                status = AnimalStatus.HEALTHY,
                weightKg = 380.0
            ),
            onSave = {},
            onBack = {}
        )
    }
}
