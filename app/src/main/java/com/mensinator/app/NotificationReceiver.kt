package com.mensinator.app

import android.app.PendingIntent
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

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // FLAG_IMMUTABLE required for Android 12+
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_bloodtype_24)
            .setContentText(messageText) // See discussion at https://github.com/EmmaTellblom/Mensinator/issues/216
            .setContentIntent(pendingIntent)
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
