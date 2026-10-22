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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.Task
import com.samuel.farmos.data.TaskCategory
import com.samuel.farmos.ui.components.FarmEmptyState
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
                        Text("Today's Tasks", fontWeight = FontWeight.Bold)
                        Text(if (tasks.isEmpty()) "All caught up!" else "${tasks.size} tasks on your farm", style = MaterialTheme.typography.bodySmall, color = FarmUi.textMuted)
                    }
                },
                colors = FarmUi.topAppBarColors()
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTask,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Task") },
                containerColor = FarmUi.green,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = FarmUi.background
        ) {
            if (tasks.isEmpty()) {
                FarmEmptyState(
                    icon = Icons.Default.Assignment,
                    title = "No tasks yet",
                    message = "Tasks you create appear here, and you can set a due date and a repeat schedule for each one.",
                    actionLabel = "Add Task",
                    onAction = onAddTask
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            shape = FarmUi.cardShape,
                            colors = CardDefaults.cardColors(containerColor = FarmUi.surfaceMint)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Today on the farm", fontWeight = FontWeight.Bold, color = FarmUi.green)
                                Text(if (tasks.none { !it.completed }) "No urgent items. You're all caught up." else "Keep track of today's work and follow-ups.", color = FarmUi.textMuted, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    items(tasks) { task ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.Button, onClick = { onEditTask(task.id) }),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.completed,
                                    onCheckedChange = { onToggleTaskComplete(task) }
                                )

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = task.title,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (task.completed) FarmUi.textMuted else FarmUi.ink
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    val dateStr = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(task.dueDateMillis))
                                    Text(
                                        text = "Category: ${task.category.name} · Due: $dateStr",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FarmUi.textMuted
                                    )
                                }
                                TextButton(onClick = { onEditTask(task.id) }) { Text("Edit", color = FarmUi.green) }
                            }
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
