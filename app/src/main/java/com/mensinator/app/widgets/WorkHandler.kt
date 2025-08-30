package com.mensinator.app.widgets

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object WorkHandler {
    fun scheduleWork(appContext: Context) {
        val now = LocalDateTime.now()
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay().plusSeconds(30)
        val initialDelay = Duration.between(now, nextMidnight)

        val workRequest = PeriodicWorkRequestBuilder<WidgetWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
            .build()

        Log.d("WorkHandler", "Scheduling work with initial delay: ${initialDelay.toMillis()} ms")
        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            "WidgetWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
}