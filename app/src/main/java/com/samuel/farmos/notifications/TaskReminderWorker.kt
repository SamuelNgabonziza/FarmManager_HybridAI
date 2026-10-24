package com.samuel.farmos.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker

class TaskReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): ListenableWorker.Result {
        val title = inputData.getString("title") ?: "Farm task due"
        val category = inputData.getString("category") ?: ""
        val taskId = inputData.getString("taskId")
        val notificationId = taskId?.hashCode() ?: System.currentTimeMillis().toInt()

        val message = if (category.isNotBlank()) "Category: $category • Tap to view task" else "Tap to view tasks in FarmOS"
        NotificationHelper.showTaskReminder(
            context = applicationContext,
            notificationId = notificationId,
            title = "Task Due: $title",
            message = message,
            taskId = taskId
        )

        return ListenableWorker.Result.success()
    }
}
