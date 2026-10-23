package com.samuel.farmos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.Task
import com.samuel.farmos.data.TaskCategory
import com.samuel.farmos.ui.theme.FarmOsTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<Task>,
    onAddTask: () -> Unit,
    onEditTask: (String) -> Unit,
    onToggleTaskComplete: (Task) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Today's Tasks", fontWeight = FontWeight.Bold, color = FarmUi.green)
                        Text(
                            if (tasks.none { !it.completed }) "All caught up! 🎉" else "${tasks.count { !it.completed }} tasks on your farm",
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
            ExtendedFloatingActionButton(
                onClick = onAddTask,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Task", fontWeight = FontWeight.Bold) },
                containerColor = if (isSystemInDarkTheme()) Color(0xFF52D3C0) else FarmUi.teal,
                contentColor = if (isSystemInDarkTheme()) Color(0xFF003730) else Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        },
        containerColor = FarmUi.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    shape = FarmUi.cardShape,
                    colors = CardDefaults.cardColors(containerColor = FarmUi.surfaceMint),
                    border = BorderStroke(1.dp, FarmUi.cardBorder)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Today on the farm", fontWeight = FontWeight.Bold, color = FarmUi.green)
                        Text(
                            if (tasks.none { !it.completed }) "No urgent items. You're all caught up." else "Keep track of today's work and follow-ups.",
                            color = FarmUi.textMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            items(tasks, key = { it.id }) { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = { onEditTask(task.id) }),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FarmUi.taskCard),
                    border = BorderStroke(1.dp, FarmUi.taskCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.completed,
                            onCheckedChange = { onToggleTaskComplete(task) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = FarmUi.green,
                                checkmarkColor = if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White
                            )
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 6.dp)
                        ) {
                            Text(
                                text = task.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                                color = if (task.completed) FarmUi.textMuted else FarmUi.ink
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val categoryName = when (task.category) {
                                    TaskCategory.HEALTH -> "Vaccination"
                                    TaskCategory.FEEDING -> "Feeding"
                                    TaskCategory.BREEDING -> "Breeding"
                                    TaskCategory.MAINTENANCE -> "Maintenance"
                                    TaskCategory.OTHER -> "Other"
                                }
                                val categoryColor = if (task.category == TaskCategory.HEALTH) Color(0xFFE55353) else FarmUi.green
                                val categoryContainer = if (task.category == TaskCategory.HEALTH) Color(0xFFE55353).copy(alpha = 0.2f) else FarmUi.iconMint
                                Surface(shape = RoundedCornerShape(6.dp), color = categoryContainer) {
                                    Text(
                                        categoryName,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = categoryColor
                                    )
                                }
                                val dateStr = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())
                                    .format(Date(task.dueDateMillis))
                                    .replace("AM", "am")
                                    .replace("PM", "pm")
                                val recurrence = task.recurrence.name.lowercase().replaceFirstChar { it.uppercase() }
                                Text(
                                    text = "· $dateStr${if (task.recurrence == com.samuel.farmos.data.Recurrence.NONE) "" else " · $recurrence"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FarmUi.textMuted
                                )
                            }
                        }
                        TextButton(onClick = { onEditTask(task.id) }) {
                            Text("Edit", color = FarmUi.green, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TasksScreenPreview() {
    FarmOsTheme {
        TasksScreen(
            tasks = listOf(
                Task(
                    id = "1",
                    title = "Clean milking parlor",
                    category = TaskCategory.MAINTENANCE,
                    completed = false,
                    dueDateMillis = System.currentTimeMillis()
                ),
                Task(
                    id = "2",
                    title = "Inspect fence line",
                    category = TaskCategory.OTHER,
                    completed = true,
                    dueDateMillis = System.currentTimeMillis()
                )
            ),
            onAddTask = {},
            onEditTask = {},
            onToggleTaskComplete = {},
            onBack = {}
        )
    }
}
