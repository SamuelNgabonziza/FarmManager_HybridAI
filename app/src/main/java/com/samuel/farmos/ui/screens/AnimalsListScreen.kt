package com.samuel.farmos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.Animal
import com.samuel.farmos.data.AnimalStatus
import com.samuel.farmos.data.Species
import com.samuel.farmos.ui.components.FarmEmptyState
import com.samuel.farmos.ui.theme.FarmOsTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val herdSpecies = listOf(Species.COW, Species.GOAT, Species.SHEEP)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalsListScreen(
    animals: List<Animal>,
    onAddAnimal: () -> Unit,
    onEditAnimal: (String) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSpecies by remember { mutableStateOf<Species?>(Species.COW) }

    val filteredAnimals = remember(animals, searchQuery, selectedSpecies) {
        animals.filter { animal ->
            val matchesQuery = searchQuery.isBlank() ||
                animal.tagNumber.contains(searchQuery, ignoreCase = true) ||
                animal.name.contains(searchQuery, ignoreCase = true) ||
                animal.breed.contains(searchQuery, ignoreCase = true) ||
                animal.status.name.contains(searchQuery, ignoreCase = true)
            val matchesSpecies = selectedSpecies == null || animal.species == selectedSpecies
            matchesQuery && matchesSpecies
        }
    }

    val tableScrollState = rememberScrollState()
    val tableHeaderBg = if (isSystemInDarkTheme()) Color(0xFF1E3A28) else Color(0xFFDFF8EC)
    val tableHeaderTextColor = if (isSystemInDarkTheme()) Color(0xFF80E3A7) else Color(0xFF26733A)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Animals", fontWeight = FontWeight.Bold, color = FarmUi.green)
                        Text(
                            "${filteredAnimals.size} total registered",
                            style = MaterialTheme.typography.bodySmall,
                            color = FarmUi.textMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = FarmUi.green)
                    }
                },
                colors = FarmUi.topAppBarColors()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAnimal,
                containerColor = FarmUi.green,
                contentColor = if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White,
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Add Animal", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = FarmUi.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Species filter pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                herdSpecies.forEach { species ->
                    val selected = selectedSpecies == species
                    val (emoji, label) = when (species) {
                        Species.COW -> "🐄" to "Cow"
                        Species.GOAT -> "🐐" to "Goat"
                        else -> "🐑" to "Sheep"
                    }
                    val selectedBg = if (isSystemInDarkTheme()) Color(0xFF80E3A7) else Color(0xFF26733A)
                    val selectedText = if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(role = Role.Tab) { selectedSpecies = if (selected) null else species },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) selectedBg else FarmUi.surface,
                        border = BorderStroke(
                            1.dp,
                            if (selected) selectedBg else FarmUi.cardBorder
                        )
                    ) {
                        Text(
                            text = "$emoji $label",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                            color = if (selected) selectedText else FarmUi.ink,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Search box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by tag, name, breed, or status") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FarmUi.green) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(24.dp),
                singleLine = false,
                minLines = 1,
                maxLines = 2,
                colors = FarmUi.textFieldColors()
            )

            Spacer(Modifier.height(8.dp))

            // The table header is ALWAYS displayed below the search bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(tableScrollState)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .background(tableHeaderBg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell("Tag", 88.dp, header = true, headerColor = tableHeaderTextColor)
                        TableCell("Name", 100.dp, header = true, headerColor = tableHeaderTextColor)
                        TableCell("Breed", 126.dp, header = true, headerColor = tableHeaderTextColor)
                        TableCell("Gender", 104.dp, header = true, headerColor = tableHeaderTextColor)
                        TableCell("Castrated", 100.dp, header = true, headerColor = tableHeaderTextColor)
                        TableCell("Birth Date", 118.dp, header = true, headerColor = tableHeaderTextColor)
                        TableCell("Weight (kg)", 108.dp, header = true, headerColor = tableHeaderTextColor)
                        TableCell("Status", 105.dp, header = true, headerColor = tableHeaderTextColor)
                    }

                    if (filteredAnimals.isNotEmpty()) {
                        filteredAnimals.forEach { animal ->
                            Row(
                                modifier = Modifier
                                    .background(FarmUi.surface)
                                    .clickable(role = Role.Button) { onEditAnimal(animal.id) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCell(animal.tagNumber, 88.dp, textColor = FarmUi.green)
                                TableCell(animal.name.ifBlank { "—" }, 100.dp)
                                TableCell(animal.breed.ifBlank { "—" }, 126.dp)
                                TableCell(animal.gender.name.lowercase().replaceFirstChar { it.uppercase() }, 104.dp)
                                TableCell(animal.isCastrated?.let { if (it) "Yes" else "No" } ?: "—", 100.dp)
                                TableCell(animal.birthDate?.let { SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(it)) } ?: "—", 118.dp)
                                TableCell(animal.weightKg?.toString() ?: "—", 108.dp)
                                TableCell(animal.status.name.lowercase().replaceFirstChar { it.uppercase() }, 105.dp)
                            }
                        }
                    }
                }
            }

            if (filteredAnimals.isEmpty()) {
                FarmEmptyState(
                    icon = Icons.Default.Search,
                    title = if (animals.isEmpty() || (selectedSpecies != null && animals.none { it.species == selectedSpecies })) {
                        "No ${selectedSpecies?.name?.lowercase(Locale.getDefault()) ?: "animals"} found"
                    } else "No matches",
                    message = if (searchQuery.isBlank()) "Tap the + button to add one." else "Try another tag, name, breed, or status."
                )
            }
        }
    }
}

@Composable
private fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    header: Boolean = false,
    headerColor: Color = Color.Unspecified,
    textColor: Color = Color.Unspecified
) {
    Text(
        text = text,
        modifier = Modifier.width(width).padding(horizontal = 9.dp, vertical = if (header) 12.dp else 16.dp),
        color = if (header) headerColor else if (textColor != Color.Unspecified) textColor else FarmUi.ink,
        fontWeight = if (header) FontWeight.Bold else FontWeight.Normal,
        style = if (header) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
        maxLines = 2
    )
}

@Preview(showBackground = true)
@Composable
fun AnimalsListScreenPreview() {
    FarmOsTheme {
        AnimalsListScreen(
            animals = listOf(
                Animal(
                    id = "1", tagNumber = "GT 37", name = "Bella", species = Species.COW,
                    breed = "Guernsey", gender = com.samuel.farmos.data.Gender.FEMALE,
                    birthDate = System.currentTimeMillis(), weightKg = 505.0, status = AnimalStatus.HEALTHY
                )
            ),
            onAddAnimal = {},
            onEditAnimal = {},
            onBack = {}
        )
    }
}
