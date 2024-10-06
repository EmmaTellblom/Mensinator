package com.mensinator.app


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import java.time.LocalDate
import java.time.ZoneId

class NotificationScheduler(private val context: Context) {

    fun scheduleNotification(notificationDate: LocalDate) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancelAll()
        Log.d("NotificationScheduler", "Notification canceled")
        val notificationTimeInMillis = notificationDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_NOTIFICATION
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            notificationTimeInMillis,
            pendingIntent
        )

        Log.d("NotificationScheduler", "Notification scheduled for $notificationDate")
    }

    companion object {
        private const val ACTION_NOTIFICATION = "com.mensinator.app.SEND_NOTIFICATION"
    }

    fun cancelNotification(notificationId: Int) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)
    }
}