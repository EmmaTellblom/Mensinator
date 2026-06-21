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

/**
 * Shared rendering logic for the period widgets. Each on-screen design is a distinct
 * subclass (see [WidgetInstances]) — this matters because Glance resolves which widget
 * instances to refresh in [androidx.glance.appwidget.GlanceAppWidget.updateAll] by the
 * concrete class. If several receivers shared one class, updateAll() would update the
 * wrong instances and designs would stomp on each other.
 */
abstract class BaseWidget(
    val showLabel: Boolean,
    val showBackground: Boolean,
) : GlanceAppWidget(), KoinComponent {

    private val calculationsHelper: CalculationsHelper by inject()
    private val appContext: Context by inject()

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = getData.collectAsState(WidgetData("", "", "", ""))
            WidgetContent(showLabel, showBackground, state)
        }
    }

    val getData = calculationsHelper.nextPeriod().combine(
        MidnightTrigger.midnightTrigger
    ) { nextPeriod, _ ->
        WidgetData(
            daysUntilPeriodWithoutText = formatDaysUntilPeriod(nextPeriod, NextPeriodFormat.OnlyDays),
            daysUntilPeriodWithText = formatDaysUntilPeriod(nextPeriod, NextPeriodFormat.MediumLengthText),
            daysUntilOvulationWithText = "",
            daysUntilOvulationWithoutText = ""
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
                    )
                )
            }
            WidgetContent(showLabel, showBackground, state)
        }
    }

    @Composable
    private fun WidgetContent(showLabel: Boolean, showBackground: Boolean, state: State<WidgetData>) {
        val data = state.value
        val context = LocalContext.current
        val label = context.getString(R.string.widget_period_abbreviation)

        MensinatorGlanceTheme {
            if (showLabel) {
                WidgetContentWithLabel(text = data.daysUntilPeriodWithText, showBackground = showBackground)
            } else {
                WidgetContentWithoutLabel(text = data.daysUntilPeriodWithoutText, label = label, showBackground = showBackground)
            }
        }
    }

    sealed interface NextPeriodFormat {
        data object OnlyDays : NextPeriodFormat
        data object MediumLengthText : NextPeriodFormat
    }

    private fun formatDaysUntilPeriod(date: LocalDate?, format: NextPeriodFormat): String {
        // date is null when no period has been tracked yet. Guard before calling
        // until(), which throws NullPointerException on a null temporal.
        if (date == null) {
            return when (format) {
                NextPeriodFormat.OnlyDays -> "?"
                NextPeriodFormat.MediumLengthText -> "Unknown"
            }
        }
        val daysUntilNextPeriod = WidgetDebugDayShift.today(appContext).until(date).days
        return when (format) {
            NextPeriodFormat.OnlyDays -> "$daysUntilNextPeriod"
            NextPeriodFormat.MediumLengthText -> "Period in $daysUntilNextPeriod days"
        }
    }
}
