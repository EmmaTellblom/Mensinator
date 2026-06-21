package com.mensinator.app.widgets

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.mensinator.app.BuildConfig
import java.time.LocalDate

/**
 * Debug-only helper that lets us simulate the day rolling over without touching the
 * device's system clock (which messes with notifications, alarms, etc.).
 *
 * It stores a simple day offset that is added to "today" wherever the widgets compute
 * how many days remain until the next period. The whole thing is gated behind
 * [BuildConfig.DEBUG], so in a release build [getOffset] always returns 0, the
 * settings toggle is hidden, and the widgets behave exactly as before.
 */
object WidgetDebugDayShift {
    private const val PREFS = "widget_debug"
    private const val KEY_OFFSET = "simulated_day_offset"

    /** Number of days to add to the real "today" when rendering widgets. 0 in release builds. */
    fun getOffset(context: Context): Int {
        if (!BuildConfig.DEBUG) return 0
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_OFFSET, 0)
    }

    fun setOffset(context: Context, offset: Int) {
        if (!BuildConfig.DEBUG) return
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_OFFSET, offset)
            .apply()
    }

    /** The simulated "today" used by the widgets. Equal to the real today in release builds. */
    fun today(context: Context): LocalDate = LocalDate.now().plusDays(getOffset(context).toLong())

    /** Re-render all widget instances so a changed offset is reflected immediately. */
    suspend fun refreshWidgets(context: Context) {
        MidnightTrigger.midnightTrigger.emit(Unit)
        WidgetInstances.forEach { it.glanceAppWidget.updateAll(context) }
    }
}
