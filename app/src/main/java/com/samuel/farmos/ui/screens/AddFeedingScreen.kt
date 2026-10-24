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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.Animal
import com.samuel.farmos.data.FeedingRecord
import com.samuel.farmos.ui.components.FarmDatePickerField
import com.samuel.farmos.ui.theme.FarmOsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeedingScreen(
    onSave: (FeedingRecord) -> Unit,
    onBack: () -> Unit,
    animals: List<Animal> = emptyList()
) {
    var feedType by remember { mutableStateOf("") }
    var quantityKgText by remember { mutableStateOf("") }
    var costUgxText by remember { mutableStateOf("") }
    var dateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var animalId by remember { mutableStateOf("") }
    var animalExpanded by remember { mutableStateOf(false) }
    val valid = feedType.isNotBlank() && (quantityKgText.toDoubleOrNull() ?: 0.0) > 0.0

    fun save() {
        val qty = quantityKgText.toDoubleOrNull() ?: return
        if (feedType.isNotBlank() && qty > 0) onSave(
            FeedingRecord(
                linkedAnimalId = animalId.takeIf(String::isNotBlank),
                feedType = feedType.trim(),
                quantityKg = qty,
                costUgx = costUgxText.toDoubleOrNull(),
                dateMillis = dateMillis
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Feeding Record", fontWeight = FontWeight.Bold, color = FarmUi.green) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = FarmUi.green)
                    }
                },
                actions = {
                    TextButton(onClick = ::save, enabled = valid) {
                        Text("Save", fontWeight = FontWeight.Bold, color = if (valid) FarmUi.green else FarmUi.textMuted)
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
                    FarmFormLabel("Feed Type *")
                    OutlinedTextField(
                        value = feedType,
                        onValueChange = { feedType = it },
                        placeholder = { Text("e.g. Silage, Hay, Concentrate") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )

                    FarmFormLabel("Animal")
                    ExposedDropdownMenuBox(expanded = animalExpanded, onExpandedChange = { animalExpanded = !animalExpanded }) {
                        OutlinedTextField(
                            value = animals.firstOrNull { it.id == animalId }?.let { "${it.tagNumber} · ${it.name.ifBlank { it.breed }}" }.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("General (whole farm)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = animalExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )
                        ExposedDropdownMenu(expanded = animalExpanded, onDismissRequest = { animalExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("General (whole farm)") },
                                onClick = { animalId = ""; animalExpanded = false }
                            )
                            animals.forEach { animal ->
                                DropdownMenuItem(
                                    text = { Text("${animal.tagNumber} · ${animal.name.ifBlank { animal.breed }}") },
                                    onClick = { animalId = animal.id; animalExpanded = false }
                                )
                            }
                        }
                    }

                    FarmDatePickerField(label = "Date", initialMillis = dateMillis, onDateSelected = { dateMillis = it })

                    FarmFormLabel("Quantity (kg) *")
                    OutlinedTextField(
                        value = quantityKgText,
                        onValueChange = { quantityKgText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        placeholder = { Text("0") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )

                    FarmFormLabel("Cost (UGX - Optional)")
                    OutlinedTextField(
                        value = costUgxText,
                        onValueChange = { costUgxText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("0") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddFeedingScreenPreview() {
    FarmOsTheme { AddFeedingScreen({}, {}) }
}
