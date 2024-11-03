package com.mensinator.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Create and show the notification
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_bloodtype_24)
            .setContentTitle("Mensinator")
            .setContentText("Your period is about to start")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
            Log.d("NotificationReceiver", "Notification sent")
        } catch (e: SecurityException) {
            Log.e("NotificationReceiver", "Notification permission not available", e)
        }
    }

    companion object {
        private const val CHANNEL_ID = "1"
        private const val NOTIFICATION_ID = 1
    }
}
