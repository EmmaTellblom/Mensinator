package com.mensinator.app.business.notifications

import kotlinx.datetime.LocalDate

interface IAndroidNotificationScheduler {
    fun scheduleNotification(
        messageText: String,
        notificationDate: LocalDate
    )

    fun cancelScheduledNotification()
}
