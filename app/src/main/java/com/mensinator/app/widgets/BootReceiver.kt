package com.mensinator.app.widgets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Use goAsync() to ensure work completes even if receiver would otherwise be killed
            val pendingResult = goAsync()
            try {
                MidnightWorker.scheduleNextMidnight(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
