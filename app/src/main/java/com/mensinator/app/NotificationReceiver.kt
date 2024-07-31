package com.mensinator.app

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Static text for the notification
        val title = "Upcoming Period Reminder"
        val message = "Your period is starting soon."

        val notificationManager = NotificationManagerCompat.from(context)

        // Create notification channel (required for API 26+)
        val channelId = "default_channel_id"
        val channelName = "Default Channel"
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Channel for period reminders"
        }
        notificationManager.createNotificationChannel(channel)

        // Create and send the notification
        val notification = NotificationCompat.Builder(context, "default_channel_id")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.baseline_bloodtype_24) // Your notification icon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Check for notification permission
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(0, notification) // 0 is the notification ID
        }
    }
}
