package com.mensinator.app.widgets

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Helper class to update widgets when period calculations change.
 * This ensures widgets are updated only when the period date calculation results in a new value.
 */
object PeriodCalculationWidgetUpdater : KoinComponent {
    
    private val context: Context by inject()
    
    private var lastKnownPeriodDate: String? = null
    
    /**
     * Call this method when period calculation might have changed.
     * It will only update widgets if the calculated period date is different from the last known value.
     */
    fun updateWidgetsIfPeriodChanged(newPeriodDate: java.time.LocalDate?) {
        val newPeriodDateString = newPeriodDate?.toString() ?: "null"
        
        if (lastKnownPeriodDate != newPeriodDateString) {
            lastKnownPeriodDate = newPeriodDateString
            updateWidgets()
        }
    }
    
    private fun updateWidgets() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Emit to midnight trigger to force widget data refresh
                MidnightTrigger.midnightTrigger.emit(Unit)
                WidgetInstances.map { receiver ->
                    launch { receiver.glanceAppWidget.updateAll(context) }
                }
            } catch (e: Exception) {
                android.util.Log.e("PeriodCalculationWidgetUpdater", "Failed to update widgets", e)
            }
        }
    }
}
