package com.samuel.farmos.data

enum class TaskCategory { FEEDING, HEALTH, BREEDING, MAINTENANCE, OTHER }
enum class Recurrence { NONE, DAILY, WEEKLY, MONTHLY }

data class Task(
    val id: String = "",
    val title: String = "",
    val category: TaskCategory = TaskCategory.OTHER,
    val linkedAnimalId: String? = null,
    val dueDateMillis: Long = 0L,
    val recurrence: Recurrence = Recurrence.NONE,
    val completed: Boolean = false
)
