package com.mensinator.app.business.notifications


import android.content.Context
import android.util.Log
import com.mensinator.app.business.IPeriodDatabaseHelper
import com.mensinator.app.business.IPeriodPrediction
import com.mensinator.app.settings.IntSetting
import com.mensinator.app.settings.StringSetting
import com.mensinator.app.ui.ResourceMapper
import com.mensinator.app.utils.IDispatcherProvider
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Service that checks whether a notification should be scheduled or cancelled.
 * Does not perform the actual scheduling itself, instead it delegates this to [IAndroidNotificationScheduler].
 * This is done to be able to unit test this class without using Robolectric.
 */
class NotificationScheduler(
    private val context: Context,
    private val dbHelper: IPeriodDatabaseHelper,
    private val periodPrediction: IPeriodPrediction,
    private val dispatcherProvider: IDispatcherProvider,
    private val androidNotificationScheduler: IAndroidNotificationScheduler,
) : INotificationScheduler {

    private val defaultReminderDays = 2

    // Schedule notification for reminder
    // Check that reminders should be scheduled (reminder>0)
    // and that it's more then reminderDays left (do not schedule notifications where there's too few reminderDays left until period)
    override suspend fun schedulePeriodNotification() {
        withContext(dispatcherProvider.IO) {
            val periodReminderDays =
                dbHelper.getSettingByKey(IntSetting.REMINDER_DAYS.settingDbKey)?.value?.toIntOrNull() ?: defaultReminderDays
            val nextPeriodDate = periodPrediction.getPredictedPeriodDate()
            val initPeriodKeyOrCustomMessage =
                dbHelper.getStringSettingByKey(StringSetting.PERIOD_NOTIFICATION_MESSAGE.settingDbKey)
            val periodMessageText =
                ResourceMapper.getPeriodReminderMessage(initPeriodKeyOrCustomMessage, context)

            val notificationDate = getNotificationScheduleDate(periodReminderDays, nextPeriodDate)
            withContext(dispatcherProvider.Main) {
                if (notificationDate != null) {
                    androidNotificationScheduler.scheduleNotification(periodMessageText, notificationDate)
                } else {
                    // Make sure the scheduled notification is cancelled, if the user data/conditions become invalid.
                    androidNotificationScheduler.cancelScheduledNotification()
                }
            }
        }
    }

    // If the date checks pass, return the notification schedule date.
    private fun getNotificationScheduleDate(
        periodReminderDays: Int,
        nextPeriodDate: LocalDate?
    ): LocalDate? {
        if (periodReminderDays <= 0 || nextPeriodDate == null) return null

        val notificationDate = nextPeriodDate.minusDays(periodReminderDays.toLong())
        if (notificationDate.isBefore(LocalDate.now())) {
            Log.d(
                "CalendarScreen",
                "Notification not scheduled because the reminder date is in the past"
            )
            return null
        }

        return nextPeriodDate.minusDays(periodReminderDays.toLong())
    }
}
