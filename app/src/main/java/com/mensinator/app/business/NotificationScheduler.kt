package com.mensinator.app.business


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.mensinator.app.NotificationReceiver
import java.time.LocalDate
import java.time.ZoneId

class NotificationScheduler(
    private val context: Context
) : INotificationScheduler {

    override fun scheduleNotification(notificationDate: LocalDate, messageText: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancelAll()
        Log.d("NotificationScheduler", "Notification canceled")
        val notificationTimeInMillis = notificationDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_NOTIFICATION
            putExtra(NotificationReceiver.MESSAGE_TEXT_KEY, messageText)
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

    override fun cancelNotification(notificationId: Int) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)
    }
}