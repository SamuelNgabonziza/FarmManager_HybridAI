package com.samuel.farmos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.Animal
import com.samuel.farmos.data.ClinicalSign
import com.samuel.farmos.data.FinanceCategory
import com.samuel.farmos.data.FinanceRecord
import com.samuel.farmos.data.FinanceType
import com.samuel.farmos.data.HealthCase
import com.samuel.farmos.data.HealthCaseStatus
import com.samuel.farmos.data.ProductionRecord
import com.samuel.farmos.data.ProductionType
import com.samuel.farmos.data.Task
import com.samuel.farmos.data.TaskCategory
import com.samuel.farmos.ui.screens.FarmUi
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickMilkBottomSheet(
    animals: List<Animal>,
    onDismiss: () -> Unit,
    onSave: (ProductionRecord) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var selectedAnimalId by remember { mutableStateOf(animals.firstOrNull()?.id ?: "") }
    var litersText by remember { mutableStateOf("") }
    var shift by remember { mutableStateOf("Morning") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FarmUi.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .imePadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = FarmUi.iconMint,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Opacity, contentDescription = null, tint = FarmUi.green)
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Quick Milk Log 🥛",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FarmUi.ink
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = FarmUi.textMuted)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Animal Selection
            Text("Select Animal", style = MaterialTheme.typography.labelMedium, color = FarmUi.textMuted)
            Spacer(Modifier.height(6.dp))
            if (animals.isEmpty()) {
                Text("No animals registered yet. Herd total will be recorded.", style = MaterialTheme.typography.bodySmall, color = FarmUi.textMuted)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        SelectChip(
                            label = "Herd Total",
                            selected = selectedAnimalId.isBlank(),
                            onClick = { selectedAnimalId = "" }
                        )
                    }
                    items(animals) { animal ->
                        SelectChip(
                            label = "${animal.tagNumber} (${animal.name.ifBlank { animal.breed }})",
                            selected = selectedAnimalId == animal.id,
                            onClick = { selectedAnimalId = animal.id }
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Shift Selection
            Text("Milking Shift", style = MaterialTheme.typography.labelMedium, color = FarmUi.textMuted)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Morning", "Afternoon", "Evening").forEach { item ->
                    SelectChip(
                        label = item,
                        selected = shift == item,
                        onClick = { shift = item }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Liters input
            OutlinedTextField(
                value = litersText,
                onValueChange = { litersText = it },
                label = { Text("Quantity in Liters (e.g. 12.5)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = FarmUi.textFieldColors()
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val amount = litersText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onSave(
                            ProductionRecord(
                                type = ProductionType.MILK,
                                linkedAnimalId = selectedAnimalId.ifBlank { null },
                                quantity = amount,
                                dateMillis = System.currentTimeMillis()
                            )
                        )
                    }
                },
                enabled = (litersText.toDoubleOrNull() ?: 0.0) > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FarmUi.green,
                    contentColor = if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White
                )
            ) {
                Text("Save Milk Log", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickHealthBottomSheet(
    animals: List<Animal>,
    onDismiss: () -> Unit,
    onSave: (HealthCase) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var selectedAnimalId by remember { mutableStateOf(animals.firstOrNull()?.id ?: "") }
    var selectedSign by remember { mutableStateOf(ClinicalSign.FEVER) }
    var complaint by remember { mutableStateOf("") }

    val quickSigns = listOf(
        ClinicalSign.FEVER to "Fever",
        ClinicalSign.LOSS_OF_APPETITE to "Off Feed",
        ClinicalSign.COUGHING to "Coughing",
        ClinicalSign.LAMENESS to "Limping",
        ClinicalSign.DIARRHEA to "Diarrhea",
        ClinicalSign.LETHARGY to "Weak/Lethargic"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FarmUi.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .imePadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Report Health Issue 🩺",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FarmUi.ink
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = FarmUi.textMuted)
                }
            }

            Spacer(Modifier.height(14.dp))

            Text("Select Animal", style = MaterialTheme.typography.labelMedium, color = FarmUi.textMuted)
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(animals) { animal ->
                    SelectChip(
                        label = "${animal.tagNumber} (${animal.name.ifBlank { animal.breed }})",
                        selected = selectedAnimalId == animal.id,
                        onClick = { selectedAnimalId = animal.id }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text("Observed Sign", style = MaterialTheme.typography.labelMedium, color = FarmUi.textMuted)
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quickSigns) { (sign, label) ->
                    SelectChip(
                        label = label,
                        selected = selectedSign == sign,
                        onClick = { selectedSign = sign }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = complaint,
                onValueChange = { complaint = it },
                label = { Text("Details or Notes (optional)") },
                singleLine = false,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = FarmUi.textFieldColors()
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    onSave(
                        HealthCase(
                            linkedAnimalId = selectedAnimalId,
                            mainComplaint = complaint.ifBlank { selectedSign.name.replace("_", " ") },
                            clinicalSigns = listOf(selectedSign),
                            status = HealthCaseStatus.ACTIVE,
                            createdDateMillis = System.currentTimeMillis()
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FarmUi.coral,
                    contentColor = if (isSystemInDarkTheme()) Color(0xFF4C1821) else Color.White
                )
            ) {
                Text("Record Health Case", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickExpenseBottomSheet(
    onDismiss: () -> Unit,
    onSave: (FinanceRecord) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(FinanceCategory.FEED) }
    var description by remember { mutableStateOf("") }

    val categories = listOf(
        FinanceCategory.FEED to "Feed",
        FinanceCategory.MEDICINE to "Medicine/Vet",
        FinanceCategory.LABOR to "Labor",
        FinanceCategory.EQUIPMENT to "Equipment",
        FinanceCategory.OTHER to "Other"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FarmUi.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .imePadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Quick Farm Expense 💰",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FarmUi.ink
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = FarmUi.textMuted)
                }
            }

            Spacer(Modifier.height(14.dp))

            Text("Category", style = MaterialTheme.typography.labelMedium, color = FarmUi.textMuted)
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { (cat, label) ->
                    SelectChip(
                        label = label,
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount (UGX / Currency)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = FarmUi.textFieldColors()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Short description (e.g. 50kg dairy meal)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = FarmUi.textFieldColors()
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onSave(
                            FinanceRecord(
                                type = FinanceType.EXPENSE,
                                category = selectedCategory,
                                amountUgx = amt,
                                description = description.ifBlank { selectedCategory.name },
                                dateMillis = System.currentTimeMillis()
                            )
                        )
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FarmUi.orange,
                    contentColor = if (isSystemInDarkTheme()) Color(0xFF3D2005) else Color.White
                )
            ) {
                Text("Save Expense", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTaskBottomSheet(
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.FEEDING) }
    var isTomorrow by remember { mutableStateOf(false) }

    val categories = listOf(
        TaskCategory.FEEDING to "Feeding",
        TaskCategory.HEALTH to "Health",
        TaskCategory.BREEDING to "Breeding",
        TaskCategory.MAINTENANCE to "Maintenance",
        TaskCategory.OTHER to "General"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FarmUi.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .imePadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Add Farm Task 📋",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FarmUi.ink
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = FarmUi.textMuted)
                }
            }

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task description (e.g. Disinfect calf pens)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = FarmUi.textFieldColors()
            )

            Spacer(Modifier.height(14.dp))

            Text("Category", style = MaterialTheme.typography.labelMedium, color = FarmUi.textMuted)
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { (cat, label) ->
                    SelectChip(
                        label = label,
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text("Due Date", style = MaterialTheme.typography.labelMedium, color = FarmUi.textMuted)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectChip(label = "Today", selected = !isTomorrow, onClick = { isTomorrow = false })
                SelectChip(label = "Tomorrow", selected = isTomorrow, onClick = { isTomorrow = true })
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val dueCal = Calendar.getInstance().apply {
                            if (isTomorrow) add(Calendar.DAY_OF_YEAR, 1)
                            set(Calendar.HOUR_OF_DAY, 18)
                            set(Calendar.MINUTE, 0)
                        }
                        onSave(
                            Task(
                                title = title.trim(),
                                category = selectedCategory,
                                dueDateMillis = dueCal.timeInMillis,
                                completed = false
                            )
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FarmUi.green,
                    contentColor = if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White
                )
            ) {
                Text("Create Task", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SelectChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val selectedBg = if (isSystemInDarkTheme()) Color(0xFF80E3A7) else Color(0xFF26733A)
    val selectedText = if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) selectedBg else FarmUi.surface,
        border = BorderStroke(1.dp, if (selected) selectedBg else FarmUi.cardBorder),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) selectedText else FarmUi.ink,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}
