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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.Recurrence
import com.samuel.farmos.data.Task
import com.samuel.farmos.data.TaskCategory
import com.samuel.farmos.data.Animal
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
    // The due date is chosen with the Material date picker. A new task
    // defaults to today, which is what a task created "now" should be due.
    var dueDateMillis by rememberSaveable(task?.id) {
        mutableStateOf(task?.dueDateMillis?.takeIf { it > 0L } ?: startOfTodayMillis())
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // The task arrives asynchronously from Firestore, so the form is populated once
    // the document for this id shows up. Re-runs only when the id changes, so it
    // never overwrites what the user is typing.
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
                title = { Text(if (task == null) "Add Task" else "Edit Task", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = { TextButton(onClick = ::save, enabled = title.isNotBlank()) { Text("Save", fontWeight = FontWeight.Bold) } },
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
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Task Title *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )

                        ExposedDropdownMenuBox(expanded = animalExpanded, onExpandedChange = { animalExpanded = !animalExpanded }) {
                            OutlinedTextField(
                                value = animals.firstOrNull { it.id == linkedAnimalId }?.let { "${it.tagNumber} · ${it.name}" }.orEmpty(),
                                onValueChange = {}, readOnly = true,
                                label = { Text("Link to an animal (optional)") },
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

                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded }
                        ) {
                            OutlinedTextField(
                                value = category.name.lowercase().replaceFirstChar { it.uppercase() },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
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
                                TaskCategory.values().forEach { cat ->
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

                        ExposedDropdownMenuBox(
                            expanded = recurrenceExpanded,
                            onExpandedChange = { recurrenceExpanded = !recurrenceExpanded }
                        ) {
                            OutlinedTextField(
                                value = recurrence.name.lowercase().replaceFirstChar { it.uppercase() },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Recurrence") },
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

                        FarmDatePickerField(
                            label = "Due Date & Time",
                            initialMillis = dueDateMillis,
                            onDateSelected = { dueDateMillis = it }
                        )

                        if (onDelete != null) {
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
    }

    // Deleting a task cannot be undone, so it is confirmed first.
    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete task?") },
            text = {
                Text(
                    if (title.isNotBlank())
                        "\"$title\" will be permanently removed. This cannot be undone."
                    else
                        "This task will be permanently removed. This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

/** Midnight of the current day, in the device's own time zone. */
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
