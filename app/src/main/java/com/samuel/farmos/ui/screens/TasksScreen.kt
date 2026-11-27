package com.samuel.farmos.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samuel.farmos.data.Recurrence
import com.samuel.farmos.data.Task
import com.samuel.farmos.data.TaskCategory
import com.samuel.farmos.ui.theme.FarmOsTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TaskViewTab {
    ACTIVE,
    OVERDUE,
    COMPLETED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<Task>,
    onAddTask: () -> Unit,
    onEditTask: (String) -> Unit,
    onToggleTaskComplete: (Task) -> Unit,
    onDeleteTask: (String) -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    var selectedTab by remember { mutableStateOf(TaskViewTab.ACTIVE) }
    var taskToComplete by remember { mutableStateOf<Task?>(null) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    val now = System.currentTimeMillis()
    val activeTasks = remember(tasks, now) {
        tasks.filter { !it.completed && it.dueDateMillis >= now }.sortedBy { it.dueDateMillis }
    }
    val overdueTasks = remember(tasks, now) {
        tasks.filter { !it.completed && it.dueDateMillis < now }.sortedBy { it.dueDateMillis }
    }
    val completedTasks = remember(tasks) {
        tasks.filter { it.completed }.sortedByDescending { it.dueDateMillis }
    }

    // Confirmation dialog before completing a task
    if (taskToComplete != null) {
        val task = taskToComplete!!
        AlertDialog(
            onDismissRequest = { taskToComplete = null },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = FarmUi.green,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Confirm Completion?",
                    fontWeight = FontWeight.Bold,
                    color = FarmUi.green
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "\"${task.title}\"",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FarmUi.ink
                    )
                    Text(
                        text = "This task will be marked as done and moved into your Completed Archive.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FarmUi.textMuted
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onToggleTaskComplete(task)
                        taskToComplete = null
                    }
                ) {
                    Text("Confirm Done", fontWeight = FontWeight.Bold, color = FarmUi.green)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToComplete = null }) {
                    Text("Cancel", color = FarmUi.textMuted)
                }
            },
            containerColor = FarmUi.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Smart Deletion Confirmation Dialog
    if (taskToDelete != null) {
        val task = taskToDelete!!
        val isDueWithinWeek = !task.completed && task.dueDateMillis >= now &&
                (task.dueDateMillis - now) <= (7L * 24 * 60 * 60 * 1000)

        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            icon = {
                Icon(
                    if (isDueWithinWeek) Icons.Default.Warning else Icons.Default.Delete,
                    contentDescription = null,
                    tint = if (isDueWithinWeek) Color(0xFFE5A93C) else FarmUi.coral,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (isDueWithinWeek) "Delete Active Task?" else "Delete Task?",
                    fontWeight = FontWeight.Bold,
                    color = if (isDueWithinWeek) Color(0xFFE5A93C) else FarmUi.coral
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "\"${task.title}\"",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FarmUi.ink
                    )
                    Text(
                        text = if (isDueWithinWeek) {
                            "This task is scheduled within the next 7 days. Are you sure you want to delete this chore?"
                        } else {
                            "This task will be permanently removed from your farm records."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = FarmUi.textMuted
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTask(task.id)
                        taskToDelete = null
                    }
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold, color = FarmUi.coral)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("Cancel", color = FarmUi.textMuted)
                }
            },
            containerColor = FarmUi.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Today's Tasks", fontWeight = FontWeight.Bold, color = FarmUi.green)
                        Text(
                            if (overdueTasks.isEmpty() && activeTasks.isEmpty()) "All caught up! 🎉" else "${activeTasks.size + overdueTasks.size} tasks scheduled",
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
            if (selectedTab != TaskViewTab.COMPLETED) {
                ExtendedFloatingActionButton(
                    onClick = onAddTask,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add Task", fontWeight = FontWeight.Bold) },
                    containerColor = if (isDark) Color(0xFF52D3C0) else FarmUi.teal,
                    contentColor = if (isDark) Color(0xFF003730) else Color.White,
                    shape = RoundedCornerShape(24.dp)
                )
            }
        },
        containerColor = FarmUi.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clipToBounds()
        ) {
            // Today on the farm banner card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                border = BorderStroke(1.dp, FarmUi.cardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(FarmUi.iconTeal, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Assignment,
                            contentDescription = null,
                            tint = FarmUi.teal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Today on the farm",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = FarmUi.ink
                        )
                        Text(
                            if (activeTasks.isEmpty() && overdueTasks.isEmpty()) "No urgent items. You're all caught up."
                            else "${activeTasks.size + overdueTasks.size} tasks require action today.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FarmUi.textMuted
                        )
                    }
                }
            }

            // Segmented Filter Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedTab == TaskViewTab.ACTIVE,
                        onClick = { selectedTab = TaskViewTab.ACTIVE },
                        label = { Text("Active (${activeTasks.size})", fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FarmUi.iconMint,
                            selectedLabelColor = FarmUi.green
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedTab == TaskViewTab.OVERDUE,
                        onClick = { selectedTab = TaskViewTab.OVERDUE },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (overdueTasks.isNotEmpty()) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = FarmUi.coral,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    "Overdue (${overdueTasks.size})",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FarmUi.iconCoral,
                            selectedLabelColor = FarmUi.coral
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedTab == TaskViewTab.COMPLETED,
                        onClick = { selectedTab = TaskViewTab.COMPLETED },
                        label = { Text("Completed (${completedTasks.size})", fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FarmUi.surfaceMint,
                            selectedLabelColor = FarmUi.green
                        )
                    )
                }
            }

            val currentList = when (selectedTab) {
                TaskViewTab.ACTIVE -> activeTasks
                TaskViewTab.OVERDUE -> overdueTasks
                TaskViewTab.COMPLETED -> completedTasks
            }

            if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FarmUi.surfaceMint,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (selectedTab) {
                                        TaskViewTab.ACTIVE -> Icons.Default.Assignment
                                        TaskViewTab.OVERDUE -> Icons.Default.CheckCircle
                                        TaskViewTab.COMPLETED -> Icons.Default.TaskAlt
                                    },
                                    contentDescription = null,
                                    tint = FarmUi.green,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Text(
                            text = when (selectedTab) {
                                TaskViewTab.ACTIVE -> "No active tasks right now."
                                TaskViewTab.OVERDUE -> "No overdue tasks! You're completely up to date."
                                TaskViewTab.COMPLETED -> "No completed tasks archived yet."
                            },
                            fontWeight = FontWeight.Bold,
                            color = FarmUi.ink,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = when (selectedTab) {
                                TaskViewTab.ACTIVE -> "Tap '+ Add Task' below to schedule farm chores."
                                TaskViewTab.OVERDUE -> "Active tasks will move here if their due date passes."
                                TaskViewTab.COMPLETED -> "Tasks marked as done will appear here, newest to oldest."
                            },
                            color = FarmUi.textMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(currentList, key = { it.id }) { task ->
                        val isOverdue = !task.completed && task.dueDateMillis < now

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.Button, onClick = { onEditTask(task.id) }),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isOverdue) FarmUi.surfaceWarm else FarmUi.taskCard
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isOverdue) FarmUi.coral.copy(alpha = 0.4f) else FarmUi.taskCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Checkbox / Done Trigger
                                Checkbox(
                                    checked = task.completed,
                                    onCheckedChange = { checked ->
                                        onToggleTaskComplete(task)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = FarmUi.green,
                                        checkmarkColor = if (isDark) Color(0xFF143820) else Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
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
                                            text = "· $dateStr${if (task.recurrence == Recurrence.NONE) "" else " · $recurrence"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isOverdue) FarmUi.coral else FarmUi.textMuted,
                                            fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(
                                        onClick = { onEditTask(task.id) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Edit", color = FarmUi.green, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    }

                                    // Calendar Export Icon
                                    IconButton(
                                        onClick = {
                                            syncTaskToCalendar(context, task)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Event,
                                            contentDescription = "Sync to Calendar",
                                            tint = FarmUi.textMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Delete Action
                                    IconButton(
                                        onClick = { taskToDelete = task },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete Task",
                                            tint = if (isOverdue) FarmUi.coral else FarmUi.textMuted,
                                            modifier = Modifier.size(18.dp)
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

/**
 * Opens the native Android Calendar application at the current date.
 */
private fun openPhoneCalendar(context: Context) {
    val calendarUri = Uri.parse("content://com.android.calendar/time/${System.currentTimeMillis()}")
    val intent = Intent(Intent.ACTION_VIEW).setData(calendarUri).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open calendar app", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Direct 1-tap sync of a specific task into Google Calendar / Samsung Calendar.
 */
private fun syncTaskToCalendar(context: Context, task: Task) {
    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, task.title)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, task.dueDateMillis)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, task.dueDateMillis + (60 * 60 * 1000L))
        putExtra(CalendarContract.Events.DESCRIPTION, "FarmOS Task - Category: ${task.category.name}")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No calendar application available", Toast.LENGTH_SHORT).show()
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
                    dueDateMillis = System.currentTimeMillis() + 3600000
                ),
                Task(
                    id = "2",
                    title = "Deworm calves",
                    category = TaskCategory.HEALTH,
                    completed = false,
                    dueDateMillis = System.currentTimeMillis() - 86400000
                ),
                Task(
                    id = "3",
                    title = "Inspect fence line",
                    category = TaskCategory.OTHER,
                    completed = true,
                    dueDateMillis = System.currentTimeMillis() - 172800000
                )
            ),
            onAddTask = {},
            onEditTask = {},
            onToggleTaskComplete = {},
            onDeleteTask = {},
            onBack = {}
        )
    }
}
