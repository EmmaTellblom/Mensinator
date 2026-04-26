package com.mensinator.app.widgets

import android.content.Context
import androidx.work.*
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay

class MidnightWorker(val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        private const val MIDNIGHT_UPDATE_DELAY_MS = 1000L

        fun scheduleNextMidnight(context: Context) {
            val now = ZonedDateTime.now()
            // Calculate the next midnight in the current time zone
            val nextMidnight = now
                .plusDays(1)
                .with(LocalTime.MIDNIGHT)

            // Calculate the duration between now and midnight
            val delay = Duration.between(now, nextMidnight).toMillis()

            val request = OneTimeWorkRequestBuilder<MidnightWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag("midnight_refresh")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "midnight_refresh",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        // Let the date boundary settle before recomputing countdown-based widget text.
        delay(MIDNIGHT_UPDATE_DELAY_MS)
        updateAllWidgets(applicationContext)
        // Schedule the next update for the following midnight
        scheduleNextMidnight(applicationContext)
        
        return Result.success()
    }
}
