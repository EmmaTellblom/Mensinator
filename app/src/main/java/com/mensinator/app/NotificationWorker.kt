package com.mensinator.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val context = applicationContext
        Log.d("NotificationWorker", "NotificationWorker started")

        // Create notification manager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel (required for API 26+)
        val channelId = "default_channel_id"
        val channelName = "Default Channel"
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Channel for period reminders"
        }
        notificationManager.createNotificationChannel(channel)

        // Create and send the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Upcoming Period Reminder")
            .setContentText("Your period is starting soon.")
            .setSmallIcon(R.drawable.baseline_bloodtype_24) // Your notification icon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Send the notification
        notificationManager.notify(0, notification) // 0 is the notification ID

        Log.d("NotificationWorker", "Notification sent successfully")
        return Result.success()
    }
}