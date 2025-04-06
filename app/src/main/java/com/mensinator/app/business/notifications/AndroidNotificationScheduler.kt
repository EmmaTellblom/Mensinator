package com.mensinator.app.business.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.mensinator.app.NotificationReceiver
import java.time.LocalDate
import java.time.ZoneId

/**
 * Allows scheduling/cancelling notifications on Android.
 */
class AndroidNotificationScheduler(
    private val context: Context,
    private val alarmManager: AlarmManager,
) : IAndroidNotificationScheduler {
    override fun scheduleNotification(
        messageText: String,
        notificationDate: LocalDate
    ) {
        val notificationTimeMillis =
            notificationDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            notificationTimeMillis,
            getPendingIntent(messageText)
        )
        Log.d("NotificationScheduler", "Notification scheduled for $notificationDate")
    }

    override fun cancelScheduledNotification() {
        alarmManager.cancel(getPendingIntent(messageText = null))
        Log.d("NotificationScheduler", "Notification cancelled")
    }

    private fun getPendingIntent(messageText: String?): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_NOTIFICATION
            messageText?.let { putExtra(NotificationReceiver.MESSAGE_TEXT_KEY, it) }
        }
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}