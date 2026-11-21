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

abstract class BaseWidgetReceiver : GlanceAppWidgetReceiver() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        MidnightWorker.scheduleNextMidnight(context)
    }
}

/**
 * One concrete [BaseWidget] subclass per design. Distinct classes are required so Glance
 * can map each placed widget to the right receiver when refreshing — see [BaseWidget].
 */
class PeriodWidgetWithLabelWithBackground : BaseWidget(showLabel = true, showBackground = true)
class PeriodWidgetWithoutLabelWithBackground : BaseWidget(showLabel = false, showBackground = true)
class PeriodWidgetWithLabelWithoutBackground : BaseWidget(showLabel = true, showBackground = false)
class PeriodWidgetWithoutLabelWithoutBackground : BaseWidget(showLabel = false, showBackground = false)

class WidgetPeriodDaysWithLabelWithBackgroundReceiver : BaseWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PeriodWidgetWithLabelWithBackground()
}

class WidgetPeriodDaysWithoutLabelWithBackgroundReceiver : BaseWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PeriodWidgetWithoutLabelWithBackground()
}

class WidgetPeriodDaysWithLabelWithoutBackgroundReceiver : BaseWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PeriodWidgetWithLabelWithoutBackground()
}

class WidgetPeriodDaysWithoutLabelWithoutBackgroundReceiver : BaseWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PeriodWidgetWithoutLabelWithoutBackground()
}
