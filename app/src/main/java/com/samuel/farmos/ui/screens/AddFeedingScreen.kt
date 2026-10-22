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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.FeedingRecord
import com.samuel.farmos.ui.theme.FarmOsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeedingScreen(
    onSave: (FeedingRecord) -> Unit,
    onBack: () -> Unit
) {
    var feedType by remember { mutableStateOf("") }
    var quantityKgText by remember { mutableStateOf("") }
    var costUgxText by remember { mutableStateOf("") }
    fun save() {
        val qty = quantityKgText.toDoubleOrNull() ?: 0.0
        if (feedType.isNotBlank() && qty > 0) onSave(FeedingRecord(
            feedType = feedType.trim(), quantityKg = qty, costUgx = costUgxText.toDoubleOrNull(), dateMillis = System.currentTimeMillis()
        ))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Feeding Record", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = { TextButton(onClick = ::save, enabled = feedType.isNotBlank() && (quantityKgText.toDoubleOrNull() ?: 0.0) > 0.0) { Text("Save", fontWeight = FontWeight.Bold) } },
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
                        OutlinedTextField(
                            value = feedType,
                            onValueChange = { feedType = it },
                            label = { Text("Feed Type (e.g. Silage, Hay, Concentrate) *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )

                        OutlinedTextField(
                            value = quantityKgText,
                            onValueChange = { quantityKgText = it },
                            label = { Text("Quantity (kg) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )

                        OutlinedTextField(
                            value = costUgxText,
                            onValueChange = { costUgxText = it },
                            label = { Text("Cost (UGX - Optional)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
}

@Preview(showBackground = true)
@Composable
fun AddFeedingScreenPreview() {
    FarmOsTheme {
        AddFeedingScreen(
            onSave = {},
            onBack = {}
        )
    }
}
