package com.mensinator.app.widgets

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.state.GlanceStateDefinition

class WidgetPeriodDaysWithLabelReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WidgetPeriodDaysWithLabel
}

object WidgetPeriodDaysWithLabel : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<WidgetData>
        get() = WidgetDataStore.stateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val data = currentState<WidgetData>()
            WidgetContent(data.daysUntilPeriodWithText)
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        super.providePreview(context, widgetCategory)
        provideContent {
            val data = WidgetData(
                daysUntilPeriodWithoutText = "10",
                daysUntilPeriodWithText = "10 days left",
                nextPeriod = null
            )
            WidgetContent(data.daysUntilPeriodWithText)
        }
    }
}

