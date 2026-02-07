package com.mensinator.app.widgets

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
}

class WidgetPeriodDaysWithoutLabelWithBackgroundReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BaseWidget(
        widgetType = WidgetType.Period,
        showLabel = false,
        showBackground = true
    )
}

class WidgetPeriodDaysWithLabelWithoutBackgroundReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BaseWidget(
        widgetType = WidgetType.Period,
        showLabel = true,
        showBackground = false
    )
}

class WidgetPeriodDaysWithoutLabelWithoutBackgroundReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BaseWidget(
        widgetType = WidgetType.Period,
        showLabel = false,
        showBackground = false
    )
}
