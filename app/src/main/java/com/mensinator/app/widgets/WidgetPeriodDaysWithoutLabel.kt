package com.mensinator.app.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.core.graphics.createBitmap
import androidx.glance.GlanceId
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.state.GlanceStateDefinition
import com.mensinator.app.R

class WidgetPeriodDaysWithoutLabelReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WidgetPeriodDaysWithoutLabel
}

object WidgetPeriodDaysWithoutLabel : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<WidgetData>
        get() = WidgetDataStore.stateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val data = currentState<WidgetData>()
            WidgetContent(data)
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        super.providePreview(context, widgetCategory)
        provideContent {
            val data = WidgetData(
                daysUntilPeriodWithoutText = "10",
                daysUntilPeriodWithText = "10 days left",
                daysUntilPeriodBitmap = createBitmap(100, 100),
                nextPeriod = null
            )
            WidgetContent(data)
        }
    }

    @Composable
    private fun WidgetContent(data: WidgetData) {
        val context = LocalContext.current
        MensinatorGlanceTheme {
            WidgetContentWithoutLabel(
                data.daysUntilPeriodWithoutText,
                context.getString(R.string.widget_period_abbreviation),
                true
            )
        }
    }
}
