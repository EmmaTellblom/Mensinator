package com.mensinator.app

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.glance.layout.Column
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MensinatorWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val daysUntilNextPeriod = getNextPeriodDate(context)
            WidgetUI(daysUntilNextPeriod)
        }
    }

    //TODO: Implement sharedPreferences correctly
    private fun getNextPeriodDate(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val nextPeriodDateString = sharedPreferences.getString("nextPeriodDate", "")

        // If no date is set in SharedPreferences, return a default message
        if (nextPeriodDateString.isNullOrEmpty()) {
            return "Next period date not set."
        }

        // Use SimpleDateFormat to parse the saved string into a Date object
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val nextPeriodDate: Date
        try {
            nextPeriodDate = sdf.parse(nextPeriodDateString) ?: throw Exception("Invalid date format")
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error: Invalid date format"
        }

        // Get today's date as a Date object
        val today = Date()

        // Calculate the difference in days between today's date and the next period date
        val diffInMillis = nextPeriodDate.time - today.time
        val daysUntilNextPeriod = diffInMillis / (1000 * 60 * 60 * 24) // Convert millis to days

        // Return a message based on the days difference
        return when {
            daysUntilNextPeriod > 0 -> "$daysUntilNextPeriod days until next period"
            daysUntilNextPeriod == 0L -> "Today is the day of your next period"
            else -> "Your next period was ${-daysUntilNextPeriod} days ago"
        }
    }

    @Composable
    fun WidgetUI(daysUntilNextPeriod: String) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = daysUntilNextPeriod,
                style = androidx.glance.text.TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 18.sp
                ),
                modifier = GlanceModifier.clickable(
                    actionStartActivity<MainActivity>()
                )
            )
        }
    }
}

class MensinatorWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = MensinatorWidget()

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        context?.let {
            CoroutineScope(Dispatchers.IO).launch {
                MensinatorWidget().updateAll(it)
            }
        }
    }
}

