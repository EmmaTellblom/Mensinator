package com.mensinator.app

import java.time.LocalDate

interface INotificationScheduler {
    fun scheduleNotification(notificationDate: LocalDate)
    fun cancelNotification(notificationId: Int)
}
