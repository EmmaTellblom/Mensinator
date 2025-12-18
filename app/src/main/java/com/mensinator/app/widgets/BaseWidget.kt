package com.mensinator.app.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.state.GlanceStateDefinition
import com.mensinator.app.R

sealed class WidgetType {
    data object Period : WidgetType()
    data object Ovulation : WidgetType()
}

class BaseWidget(
    val widgetType: WidgetType,
    val showLabel: Boolean,
    val showBackground: Boolean,
) : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<WidgetData>
        get() = WidgetDataStore.stateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val data = currentState<WidgetData>()
            WidgetContent(widgetType, showLabel, showBackground, data)
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        super.providePreview(context, widgetCategory)
        provideContent {
            val data = WidgetData(
                daysUntilPeriodWithoutText = "10",
                daysUntilPeriodWithText = "10 days left",
                daysUntilOvulationWithoutText = "20",
                daysUntilOvulationWithText = "20 days left",
                cycleDay = 5,
                nextPeriod = null
            )
            WidgetContent(widgetType, showLabel, showBackground, data)
        }
    }

    @Composable
    private fun WidgetContent(
        widgetType: WidgetType,
        showLabel: Boolean,
        showBackground: Boolean,
        data: WidgetData
    ) {
        val context = LocalContext.current
        val textWithoutLabel = when (widgetType) {
            WidgetType.Period -> data.daysUntilPeriodWithoutText
            WidgetType.Ovulation -> data.daysUntilOvulationWithoutText
        }
        val textWithLabel = when (widgetType) {
            WidgetType.Period -> data.daysUntilPeriodWithText
            WidgetType.Ovulation -> data.daysUntilOvulationWithText
        }
        val label = when (widgetType) {
            WidgetType.Period -> context.getString(R.string.widget_period_abbreviation)
            WidgetType.Ovulation -> context.getString(R.string.widget_ovulation_abbreviation)
        }

        MensinatorGlanceTheme {
            if (showLabel) {
                WidgetContentWithLabel(
                    text = textWithLabel,
                    showBackground = showBackground
                )
            } else {
                WidgetContentWithoutLabel(
                    text = textWithoutLabel,
                    label = label,
                    showBackground = showBackground
                )
            }
        }
    }
}
