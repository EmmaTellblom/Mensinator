package com.mensinator.app.business

import java.time.LocalDate

interface INotificationScheduler {
    fun scheduleNotification(notificationDate: LocalDate)
    fun cancelNotification(notificationId: Int)
}
