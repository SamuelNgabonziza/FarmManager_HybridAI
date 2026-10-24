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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.Animal
import com.samuel.farmos.data.Recurrence
import com.samuel.farmos.data.Task
import com.samuel.farmos.data.TaskCategory
import com.samuel.farmos.ui.components.FarmDatePickerField
import com.samuel.farmos.ui.theme.FarmOsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    animals: List<Animal> = emptyList(),
    task: Task? = null,
    onSave: (Task) -> Unit,
    onDelete: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    var title by rememberSaveable(task?.id) { mutableStateOf(task?.title ?: "") }
    var linkedAnimalId by rememberSaveable(task?.id) { mutableStateOf(task?.linkedAnimalId ?: "") }
    var category by rememberSaveable(task?.id) { mutableStateOf(task?.category ?: TaskCategory.OTHER) }
    var recurrence by rememberSaveable(task?.id) { mutableStateOf(task?.recurrence ?: Recurrence.NONE) }
    var dueDateMillis by rememberSaveable(task?.id) {
        mutableStateOf(task?.dueDateMillis?.takeIf { it > 0L } ?: startOfTodayMillis())
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(task?.id) {
        task?.let {
            title = it.title
            linkedAnimalId = it.linkedAnimalId ?: ""
            category = it.category
            recurrence = it.recurrence
            dueDateMillis = it.dueDateMillis.takeIf { millis -> millis > 0L } ?: startOfTodayMillis()
        }
    }

    var categoryExpanded by remember { mutableStateOf(false) }
    var recurrenceExpanded by remember { mutableStateOf(false) }
    var animalExpanded by remember { mutableStateOf(false) }

    fun save() {
        if (title.isNotBlank()) onSave((task ?: Task()).copy(
            title = title.trim(), category = category, linkedAnimalId = linkedAnimalId.ifBlank { null },
            recurrence = recurrence, dueDateMillis = dueDateMillis
        ))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (task == null) "Add Task" else "Edit Task", fontWeight = FontWeight.Bold, color = FarmUi.green) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = FarmUi.green)
                    }
                },
                actions = {
                    TextButton(onClick = ::save, enabled = title.isNotBlank()) {
                        Text("Save", fontWeight = FontWeight.Bold, color = if (title.isNotBlank()) FarmUi.green else FarmUi.textMuted)
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
                    FarmFormLabel("Task Title")
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("e.g. Morning Milking, Clean shed") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )

                    FarmFormLabel("Category")
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = if (category == TaskCategory.HEALTH) "Vaccination" else category.name.lowercase().replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )
                        ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                            TaskCategory.values().forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(if (cat == TaskCategory.HEALTH) "Vaccination" else cat.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    onClick = { category = cat; categoryExpanded = false }
                                )
                            }
                        }
                    }

                    FarmFormLabel("Link to an animal (optional)")
                    ExposedDropdownMenuBox(expanded = animalExpanded, onExpandedChange = { animalExpanded = !animalExpanded }) {
                        OutlinedTextField(
                            value = animals.firstOrNull { it.id == linkedAnimalId }?.let { "${it.tagNumber} · ${it.name}" }.orEmpty(),
                            onValueChange = {}, readOnly = true,
                            placeholder = { Text("None (general task)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = animalExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(), shape = FarmUi.fieldShape, colors = FarmUi.textFieldColors()
                        )
                        ExposedDropdownMenu(expanded = animalExpanded, onDismissRequest = { animalExpanded = false }) {
                            DropdownMenuItem(text = { Text("None (general task)") }, onClick = { linkedAnimalId = ""; animalExpanded = false })
                            animals.forEach { animal ->
                                DropdownMenuItem(text = { Text("${animal.tagNumber} · ${animal.name.ifBlank { animal.breed }}") }, onClick = { linkedAnimalId = animal.id; animalExpanded = false })
                            }
                        }
                    }

                    FarmDatePickerField(
                        label = "Due Date & Time",
                        initialMillis = dueDateMillis,
                        includeTime = true,
                        onDateSelected = { dueDateMillis = it }
                    )

                    FarmFormLabel("Repeat")
                    ExposedDropdownMenuBox(
                        expanded = recurrenceExpanded,
                        onExpandedChange = { recurrenceExpanded = !recurrenceExpanded }
                    ) {
                        OutlinedTextField(
                            value = recurrence.name.lowercase().replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = recurrenceExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = recurrenceExpanded,
                            onDismissRequest = { recurrenceExpanded = false }
                        ) {
                            Recurrence.values().forEach { rec ->
                                DropdownMenuItem(
                                    text = { Text(rec.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        recurrence = rec
                                        recurrenceExpanded = false
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
                            Text("Delete Task")
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete task?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = {
                Text(
                    if (title.isNotBlank())
                        "\"$title\" will be permanently removed. This cannot be undone."
                    else
                        "This task will be permanently removed. This cannot be undone.",
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

private fun startOfTodayMillis(): Long = java.util.Calendar.getInstance().apply {
    set(java.util.Calendar.HOUR_OF_DAY, 0)
    set(java.util.Calendar.MINUTE, 0)
    set(java.util.Calendar.SECOND, 0)
    set(java.util.Calendar.MILLISECOND, 0)
}.timeInMillis

@Preview(showBackground = true)
@Composable
fun AddEditTaskScreenPreview() {
    FarmOsTheme {
        AddEditTaskScreen(
            task = Task(
                id = "1",
                title = "Vaccinate Cattle",
                category = TaskCategory.HEALTH,
                recurrence = Recurrence.MONTHLY,
                dueDateMillis = System.currentTimeMillis()
            ),
            onSave = {},
            onDelete = {},
            onBack = {}
        )
    }
}
