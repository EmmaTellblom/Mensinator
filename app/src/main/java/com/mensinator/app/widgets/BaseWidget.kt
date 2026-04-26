package com.mensinator.app.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.mensinator.app.R
import com.mensinator.app.business.CalculationsHelper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import java.time.temporal.ChronoUnit

sealed class WidgetType {
    data object Period : WidgetType()
    data object Ovulation : WidgetType()
}

class BaseWidget(
    val widgetType: WidgetType,
    val showLabel: Boolean,
    val showBackground: Boolean,
) : GlanceAppWidget(), KoinComponent {

    private val calculationsHelper: CalculationsHelper by inject()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = createWidgetData(LocalDate.now())
        provideContent {
            WidgetContent(widgetType, showLabel, showBackground, data)
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        super.providePreview(context, widgetCategory)
        provideContent {
            WidgetContent(
                widgetType = widgetType,
                showLabel = showLabel,
                showBackground = showBackground,
                data = WidgetData(
                    daysUntilPeriodWithoutText = "10",
                    daysUntilPeriodWithText = "Period in 10 days",
                    daysUntilOvulationWithoutText = "",
                    daysUntilOvulationWithText = "",
                )
            )
        }
    }

    private fun createWidgetData(referenceDate: LocalDate): WidgetData {
        val nextPeriod = calculationsHelper.calculateNextPeriod()

        return WidgetData(
            daysUntilPeriodWithoutText = formatDaysUntilPeriod(
                date = nextPeriod,
                format = NextPeriodFormat.OnlyDays,
                referenceDate = referenceDate,
            ),
            daysUntilPeriodWithText = formatDaysUntilPeriod(
                date = nextPeriod,
                format = NextPeriodFormat.MediumLengthText,
                referenceDate = referenceDate,
            ),
            daysUntilOvulationWithText = "",
            daysUntilOvulationWithoutText = "",
        )
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

    sealed interface NextPeriodFormat {
        data object OnlyDays : NextPeriodFormat
        data object MediumLengthText : NextPeriodFormat
    }

    private fun formatDaysUntilPeriod(
        date: LocalDate?,
        format: NextPeriodFormat,
        referenceDate: LocalDate,
    ): String {
        val daysUntilNextPeriod = date?.let { ChronoUnit.DAYS.between(referenceDate, it).toInt() }
        return when (format) {
            NextPeriodFormat.OnlyDays -> {
                if (daysUntilNextPeriod == null) {
                    "?"
                } else {
                    "$daysUntilNextPeriod"
                }
            }
            NextPeriodFormat.MediumLengthText -> {
                if (daysUntilNextPeriod == null) {
                    "Unknown"
                } else {
                    "Period in $daysUntilNextPeriod days"
                }
            }
        }
    }
}
