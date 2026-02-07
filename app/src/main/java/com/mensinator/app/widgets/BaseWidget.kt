package com.mensinator.app.widgets

import android.content.Context
import androidx.compose.runtime.*
import androidx.glance.GlanceId
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.mensinator.app.R
import com.mensinator.app.business.CalculationsHelper
import kotlinx.coroutines.flow.combine
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate

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

    override val stateDefinition: GlanceStateDefinition<*> =
        PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = getData.collectAsState(WidgetData("", "", "", "", null, null))
            WidgetContent(widgetType, showLabel, showBackground, state)
        }
    }

    val getData = combine(
        calculationsHelper.nextPeriod(),
        calculationsHelper.cycleDay(LocalDate.now()),
        MidnightTrigger.midnightTrigger,
    ) { nextPeriod, cycleDay, _ ->
        WidgetData(
            daysUntilPeriodWithoutText = formatDaysUntilPeriod(
                nextPeriod,
                NextPeriodFormat.OnlyDays
            ),
            daysUntilPeriodWithText = formatDaysUntilPeriod(
                nextPeriod,
                NextPeriodFormat.MediumLengthText
            ),
            daysUntilOvulationWithText = "",
            daysUntilOvulationWithoutText = "",
            cycleDay = cycleDay,
            nextPeriod = nextPeriod
        )
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        super.providePreview(context, widgetCategory)
        provideContent {
            val state = remember {
                mutableStateOf(
                    WidgetData(
                        daysUntilPeriodWithoutText = "10",
                        daysUntilPeriodWithText = "Period in 10 days",
                        daysUntilOvulationWithoutText = "",
                        daysUntilOvulationWithText = "",
                        cycleDay = 5,
                        nextPeriod = null
                    )
                )
            }
            WidgetContent(widgetType, showLabel, showBackground, state)
        }
    }

    @Composable
    private fun WidgetContent(
        widgetType: WidgetType,
        showLabel: Boolean,
        showBackground: Boolean,
        state: State<WidgetData>
    ) {
        val data = state.value
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
        format: NextPeriodFormat
    ): String {
        val daysUntilNextPeriod = LocalDate.now().until(date).days
        return when (format) {
            NextPeriodFormat.OnlyDays -> {
                if (date == null) {
                    "?"
                } else {
                    "$daysUntilNextPeriod"
                }
            }
            NextPeriodFormat.MediumLengthText -> {
                if (date == null) {
                    "Unknown"
                } else {
                    "Period in $daysUntilNextPeriod days"
                }
            }
        }
    }
}
