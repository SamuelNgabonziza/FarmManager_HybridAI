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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.FinanceCategory
import com.samuel.farmos.data.FinanceRecord
import com.samuel.farmos.data.FinanceType
import com.samuel.farmos.ui.components.FarmDatePickerField
import com.samuel.farmos.ui.theme.FarmOsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFinanceScreen(
    onSave: (FinanceRecord) -> Unit,
    onBack: () -> Unit
) {
    var type by remember { mutableStateOf(FinanceType.EXPENSE) }
    var category by remember { mutableStateOf(FinanceCategory.OTHER) }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    fun save() {
        val amount = amountText.toDoubleOrNull() ?: 0.0
        if (amount > 0) onSave(
            FinanceRecord(
                type = type,
                category = category,
                amountUgx = amount,
                description = description.trim(),
                dateMillis = dateMillis
            )
        )
    }

    val canSave = (amountText.toDoubleOrNull() ?: 0.0) > 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Finance Record", fontWeight = FontWeight.Bold, color = FarmUi.green) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = FarmUi.green)
                    }
                },
                actions = {
                    TextButton(onClick = ::save, enabled = canSave) {
                        Text("Save", fontWeight = FontWeight.Bold, color = if (canSave) FarmUi.green else FarmUi.textMuted)
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
                    FarmFormLabel("Type")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isExpense = type == FinanceType.EXPENSE
                        val expenseBg = if (isSystemInDarkTheme()) Color(0xFF80E3A7) else Color(0xFF26733A)
                        val expenseText = if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(role = Role.Tab) { type = FinanceType.EXPENSE },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isExpense) expenseBg else FarmUi.surface,
                            border = BorderStroke(1.dp, if (isExpense) expenseBg else FarmUi.cardBorder)
                        ) {
                            Text(
                                text = "Expense",
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = if (isExpense) expenseText else FarmUi.ink,
                                fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Medium,
                                style = MaterialTheme.typography.titleSmall,
                                textAlign = TextAlign.Center
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(role = Role.Tab) { type = FinanceType.INCOME },
                            shape = RoundedCornerShape(12.dp),
                            color = if (!isExpense) expenseBg else FarmUi.surface,
                            border = BorderStroke(1.dp, if (!isExpense) expenseBg else FarmUi.cardBorder)
                        ) {
                            Text(
                                text = "Income",
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = if (!isExpense) expenseText else FarmUi.ink,
                                fontWeight = if (!isExpense) FontWeight.Bold else FontWeight.Medium,
                                style = MaterialTheme.typography.titleSmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    FarmFormLabel("Category")
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = category.name.lowercase().replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            FinanceCategory.values().forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        category = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    FarmDatePickerField(label = "Date", initialMillis = dateMillis, onDateSelected = { dateMillis = it })

                    FarmFormLabel("Amount (UGX) *")
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter(Char::isDigit) },
                        placeholder = { Text("0") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )

                    FarmFormLabel("Description (optional)")
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("e.g. Sold 20L milk to dairy") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
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
fun AddFinanceScreenPreview() {
    FarmOsTheme { AddFinanceScreen(onSave = {}, onBack = {}) }
}
