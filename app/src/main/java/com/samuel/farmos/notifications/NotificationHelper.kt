package com.samuel.farmos.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.samuel.farmos.MainActivity
import com.samuel.farmos.R
import com.samuel.farmos.ui.navigation.Routes

object NotificationHelper {
    private const val CHANNEL_TASKS = "task_reminders"
    private const val CHANNEL_MILKING = "milking_reminders"
    private const val CHANNEL_BREEDING = "breeding_reminders"
    private const val CHANNEL_HEALTH = "health_reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val taskChannel = NotificationChannel(
                CHANNEL_TASKS,
                "Farm Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for scheduled daily farm tasks"
                enableVibration(true)
                setShowBadge(true)
            }

            val milkingChannel = NotificationChannel(
                CHANNEL_MILKING,
                "Milking Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Morning and evening milking reminders"
                enableVibration(true)
                setShowBadge(true)
            }

            val breedingChannel = NotificationChannel(
                CHANNEL_BREEDING,
                "Calving & Breeding Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Upcoming birth and heat cycle countdown alerts"
                enableVibration(true)
                setShowBadge(true)
            }

            val healthChannel = NotificationChannel(
                CHANNEL_HEALTH,
                "Health & Vaccine Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for medical treatments and booster vaccines"
                enableVibration(true)
                setShowBadge(true)
            }

            manager.createNotificationChannels(listOf(taskChannel, milkingChannel, breedingChannel, healthChannel))
        }
    }

    fun showTaskReminder(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        taskId: String? = null
    ) {
        showNotification(
            context = context,
            channelId = CHANNEL_TASKS,
            notificationId = notificationId,
            title = title,
            message = message,
            destination = Routes.TASKS_LIST,
            extraKey = "taskId",
            extraValue = taskId
        )
    }

    fun showMilkingAlert(
        context: Context,
        notificationId: Int = 1001,
        title: String = "Milking Time 🥛",
        message: String = "Time to record today's milk yield in FarmOS."
    ) {
        showNotification(
            context = context,
            channelId = CHANNEL_MILKING,
            notificationId = notificationId,
            title = title,
            message = message,
            destination = Routes.PRODUCTION_LIST
        )
    }

    fun showBreedingAlert(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        showNotification(
            context = context,
            channelId = CHANNEL_BREEDING,
            notificationId = notificationId,
            title = title,
            message = message,
            destination = Routes.BREEDING_LIST
        )
    }

    fun showHealthAlert(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        showNotification(
            context = context,
            channelId = CHANNEL_HEALTH,
            notificationId = notificationId,
            title = title,
            message = message,
            destination = Routes.HEALTH_LIST
        )
    }

    private fun showNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        message: String,
        destination: String,
        extraKey: String? = null,
        extraValue: String? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("destination", destination)
            if (extraKey != null && extraValue != null) {
                putExtra(extraKey, extraValue)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Handled when notification permission is pending on Android 13+
        }
    }
}
