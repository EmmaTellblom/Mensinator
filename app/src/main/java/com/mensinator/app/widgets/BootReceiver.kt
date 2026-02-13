package com.mensinator.app.widgets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Use goAsync() to ensure work completes even if receiver would otherwise be killed
            val pendingResult = goAsync()
            scope.launch {
                try {
                    MidnightWorker.scheduleNextMidnight(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
