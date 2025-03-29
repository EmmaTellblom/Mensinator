package com.mensinator.app.business

interface INotificationScheduler {
    /**
     * Checks if there is enough data to schedule a period reminder notification, then schedules it.
     */
    suspend fun schedulePeriodNotification()
}
