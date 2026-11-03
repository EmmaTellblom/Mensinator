package com.mensinator.app.widgets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import co.touchlab.kermit.Logger

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != "android.intent.action.QUICKBOOT_POWERON" &&
            action != "com.htc.intent.action.QUICKBOOT_POWERON"
        ) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                MidnightWorker.scheduleNextMidnight(appContext)
                MidnightTrigger.midnightTrigger.emit(Unit)
                WidgetInstances.map { receiver ->
                    async { receiver.glanceAppWidget.updateAll(appContext) }
                }.awaitAll()
            } catch (e: Exception) {
                Logger.withTag("BootReceiver").e(e) { "Failed to refresh widgets on boot" }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
