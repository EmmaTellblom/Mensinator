package com.mensinator.app

import java.time.LocalDate

interface INotificationScheduler {
    fun scheduleNotification(notificationDate: LocalDate, messageText: String)
    fun cancelNotification(notificationId: Int)
}
