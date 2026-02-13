package com.mensinator.app.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import org.koin.core.context.GlobalContext.get
import org.koin.dsl.module


val WidgetModule = module {
    single { WidgetPeriodDaysWithLabelWithBackgroundReceiver() }
    single { WidgetPeriodDaysWithoutLabelWithBackgroundReceiver() }
    single { WidgetPeriodDaysWithLabelWithoutBackgroundReceiver() }
    single { WidgetPeriodDaysWithoutLabelWithoutBackgroundReceiver() }
}

val WidgetInstances
    get() = listOf(
        get().get<WidgetPeriodDaysWithLabelWithBackgroundReceiver>(),
        get().get<WidgetPeriodDaysWithoutLabelWithBackgroundReceiver>(),
        get().get<WidgetPeriodDaysWithLabelWithoutBackgroundReceiver>(),
        get().get<WidgetPeriodDaysWithoutLabelWithoutBackgroundReceiver>(),
    )

class WidgetPeriodDaysWithLabelWithBackgroundReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BaseWidget(
        widgetType = WidgetType.Period,
        showLabel = true,
        showBackground = true
    )

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        MidnightWorker.scheduleNextMidnight(context)
    }
}

class WidgetPeriodDaysWithoutLabelWithBackgroundReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BaseWidget(
        widgetType = WidgetType.Period,
        showLabel = false,
        showBackground = true
    )

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        MidnightWorker.scheduleNextMidnight(context)
    }
}

class WidgetPeriodDaysWithLabelWithoutBackgroundReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BaseWidget(
        widgetType = WidgetType.Period,
        showLabel = true,
        showBackground = false
    )

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        MidnightWorker.scheduleNextMidnight(context)
    }
}

class WidgetPeriodDaysWithoutLabelWithoutBackgroundReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BaseWidget(
        widgetType = WidgetType.Period,
        showLabel = false,
        showBackground = false
    )

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        MidnightWorker.scheduleNextMidnight(context)
    }
}
