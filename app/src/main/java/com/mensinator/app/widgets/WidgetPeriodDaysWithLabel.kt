package com.mensinator.app.widgets

import android.content.Context
import androidx.core.graphics.createBitmap
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
            // TODO: Fix
            val data = currentState<WidgetData>()
            WidgetContentWithLabel("7 days", "until period starts", false)
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
            WidgetContentWithLabel(data.daysUntilPeriodWithText, "", true)
        }
    }
}

