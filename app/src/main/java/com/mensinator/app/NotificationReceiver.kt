package com.mensinator.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "1"
        private const val NOTIFICATION_ID = 1
        const val ACTION_NOTIFICATION = "com.mensinator.app.SEND_NOTIFICATION"
        const val MESSAGE_TEXT_KEY = "messageText"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val messageText = intent.getStringExtra(MESSAGE_TEXT_KEY)
        // Create and show the notification
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_bloodtype_24)
            .setContentTitle("Mensinator")
            .setContentText(messageText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
            Log.d("NotificationReceiver", "Notification sent")
        } catch (e: SecurityException) {
            Log.e("NotificationReceiver", "Notification permission not available", e)
        }
    }
}
