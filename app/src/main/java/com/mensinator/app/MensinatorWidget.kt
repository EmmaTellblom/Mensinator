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

class MensinatorWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            //val daysUntilNextPeriod = getNextPeriodDate(context)
            val daysUntilNextPeriod = "7 days to next period"
            WidgetUI(daysUntilNextPeriod)
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

