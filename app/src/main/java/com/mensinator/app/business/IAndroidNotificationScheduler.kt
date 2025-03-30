package com.mensinator.app.business

import java.time.LocalDate

interface IAndroidNotificationScheduler {
    fun scheduleNotification(
        messageText: String,
        notificationDate: LocalDate
    )

    fun cancelScheduledNotification()
}
