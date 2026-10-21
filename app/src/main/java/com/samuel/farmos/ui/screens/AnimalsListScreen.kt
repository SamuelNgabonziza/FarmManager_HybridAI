package com.samuel.farmos.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.Animal
import com.samuel.farmos.data.Species

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalsListScreen(
    animals: List<Animal>,
    onAddAnimal: () -> Unit,
    onEditAnimal: (String) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSpecies by remember { mutableStateOf<Species?>(null) }

    val filteredAnimals = remember(animals, searchQuery, selectedSpecies) {
        animals.filter { animal ->
            val matchesQuery = searchQuery.isBlank() ||
                animal.tagNumber.contains(searchQuery, ignoreCase = true) ||
                animal.name.contains(searchQuery, ignoreCase = true) ||
                animal.breed.contains(searchQuery, ignoreCase = true)
            val matchesSpecies = selectedSpecies == null || animal.species == selectedSpecies
            matchesQuery && matchesSpecies
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Animals (${filteredAnimals.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = FarmUi.topAppBarColors()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAnimal,
                containerColor = FarmUi.green,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Animal")
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = FarmUi.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by tag, name, or breed...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = FarmUi.fieldShape,
                    singleLine = true,
                    colors = FarmUi.textFieldColors()
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedSpecies == null,
                            onClick = { selectedSpecies = null },
                            label = { Text("All") }
                        )
                    }
                    items(Species.values()) { species ->
                        FilterChip(
                            selected = selectedSpecies == species,
                            onClick = { selectedSpecies = if (selectedSpecies == species) null else species },
                            label = { Text(species.name) }
                        )
                    }
                }

                if (filteredAnimals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No animals found",
                            color = FarmUi.textMuted,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredAnimals) { animal ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEditAnimal(animal.id) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Tag #${animal.tagNumber.ifBlank { animal.id.take(6) }}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = FarmUi.deepGreen
                                        )
                                        Text(
                                            text = animal.species.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = FarmUi.textMuted
                                        )
                                    }

                                    if (animal.name.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Name: ${animal.name}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = FarmUi.ink
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Breed: ${animal.breed.ifBlank { "Unspecified" }}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = FarmUi.textMuted
                                        )
                                        Text(
                                            text = "Status: ${animal.status.name}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = FarmUi.green
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
