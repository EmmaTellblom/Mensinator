package com.mensinator.app.widgets

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WidgetWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("WidgetWorker", "Updating widget")
            WidgetInstances.forEach {
                it.glanceAppWidget.updateAll(appContext)
            }
        return Result.success()
    }
}