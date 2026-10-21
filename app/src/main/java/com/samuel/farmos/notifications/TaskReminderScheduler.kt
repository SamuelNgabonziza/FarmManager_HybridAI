package com.samuel.farmos.notifications

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.samuel.farmos.data.Task
import java.util.concurrent.TimeUnit

object TaskReminderScheduler {
    private fun uniqueWorkName(taskId: String): String = "task_reminder_$taskId"

    fun schedule(context: Context, task: Task) {
        if (task.id.isBlank()) return
        val delayMillis = task.dueDateMillis - System.currentTimeMillis()
        if (delayMillis <= 0) return

        try {
            val data = Data.Builder()
                .putString("taskId", task.id)
                .putString("title", task.title)
                .putString("category", task.category.name)
                .putString("notificationId", task.id)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueWorkName(task.id),
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        } catch (e: Throwable) {
            Log.e("TaskReminderScheduler", "Failed to schedule reminder: ${e.message}", e)
        }
    }

    fun cancel(context: Context, taskId: String) {
        if (taskId.isBlank()) return
        try {
            WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName(taskId))
        } catch (e: Throwable) {
            Log.e("TaskReminderScheduler", "Failed to cancel reminder: ${e.message}", e)
        }
    }
}
