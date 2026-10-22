package com.samuel.farmos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.Animal
import com.samuel.farmos.data.ProductionRecord
import com.samuel.farmos.data.ProductionType
import com.samuel.farmos.ui.components.FarmDatePickerField
import com.samuel.farmos.ui.theme.FarmOsTheme
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductionScreen(
    animals: List<Animal> = emptyList(),
    initialType: ProductionType = ProductionType.MILK,
    onSave: (ProductionRecord) -> Unit,
    onBack: () -> Unit
) {
    var type by rememberSaveable { mutableStateOf(initialType) }
    var quantityText by rememberSaveable { mutableStateOf("") }
    var selectedAnimalId by rememberSaveable { mutableStateOf("") }
    val selectedAnimal = animals.firstOrNull { it.id == selectedAnimalId }
    var dateMillis by rememberSaveable { mutableStateOf(Calendar.getInstance().timeInMillis) }
    var animalExpanded by remember { mutableStateOf(false) }
    val quantity = quantityText.toDoubleOrNull()
    val canSave = quantity != null && quantity > 0.0

    fun save() {
        if (!canSave) return
        onSave(
            ProductionRecord(
                type = type,
                linkedAnimalId = selectedAnimal?.id,
                dateMillis = dateMillis,
                quantity = quantity!!
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Production Record", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                actions = { TextButton(onClick = ::save, enabled = canSave) { Text("Save", fontWeight = FontWeight.Bold) } },
                colors = FarmUi.topAppBarColors()
            )
        },
        containerColor = FarmUi.background
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(16.dp).imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Type", color = FarmUi.green, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(selected = type == ProductionType.MILK, onClick = { type = ProductionType.MILK }, label = { Text("🥛 Milk") }, modifier = Modifier.weight(1f))
                FilterChip(selected = type == ProductionType.WEIGHT, onClick = { type = ProductionType.WEIGHT }, label = { Text("🐄 Weight") }, modifier = Modifier.weight(1f))
            }
            ExposedDropdownMenuBox(expanded = animalExpanded, onExpandedChange = { animalExpanded = !animalExpanded }) {
                OutlinedTextField(
                    value = selectedAnimal?.let { "${it.tagNumber} · ${it.name.ifBlank { it.breed }}" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select an animal") },
                    placeholder = { Text(if (animals.isEmpty()) "Add an animal first" else "Choose an animal") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = animalExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = FarmUi.fieldShape,
                    colors = FarmUi.textFieldColors()
                )
                ExposedDropdownMenu(expanded = animalExpanded, onDismissRequest = { animalExpanded = false }) {
                    animals.forEach { animal ->
                        DropdownMenuItem(
                            text = { Text("${animal.tagNumber} · ${animal.name.ifBlank { animal.breed }}") },
                            onClick = { selectedAnimalId = animal.id; animalExpanded = false }
                        )
                    }
                }
            }
            Text("Date", color = FarmUi.green, fontWeight = FontWeight.SemiBold)
            FarmDatePickerField(label = "", initialMillis = dateMillis, onDateSelected = { dateMillis = it })
            OutlinedTextField(
                value = quantityText,
                onValueChange = { quantityText = it },
                label = { Text(if (type == ProductionType.MILK) "Quantity (litres)" else "Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = FarmUi.fieldShape,
                colors = FarmUi.textFieldColors()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddProductionScreenPreview() {
    FarmOsTheme { AddProductionScreen(onSave = {}, onBack = {}) }
}
