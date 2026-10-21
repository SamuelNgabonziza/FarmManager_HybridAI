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
        val notificationId = taskId?.hashCode() ?: 0

        val message = if (category.isNotBlank()) "Category: $category" else "Tap to view details"
        NotificationHelper.showTaskReminder(
            applicationContext,
            notificationId,
            "Task due: $title",
            message
        )

        return ListenableWorker.Result.success()
    }
}
