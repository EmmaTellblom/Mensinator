package com.mensinator.app.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.mensinator.app.MainActivity
import com.mensinator.app.R
import com.mensinator.app.business.IPeriodPrediction
import org.koin.compose.koinInject
import java.time.LocalDate

sealed interface NextPeriodFormat {
    data object OnlyDays : NextPeriodFormat
    data object MediumLengthText : NextPeriodFormat
}

// TODO: Data handling - Introduce repository/service to get updates?
// related: start worker that runs on midnight to ensure "remaining days" figure is correct

// TODO: Check what widgets to implement
// TODO: Check how to enable the user to select what text format should be used
// TODO: Handle widget taps
// TODO: Provide widget preview to system https://developer.android.com/develop/ui/views/appwidgets/enhance#updated-preview-with-glance
// TODO: Update widget when data changes https://developer.android.com/develop/ui/compose/glance/glance-app-widget#update-glanceappwidget
class MensinatorWidget() : GlanceAppWidget() {

    companion object {
        private val SMALL_BOX = DpSize(90.dp, 90.dp)
        private val BIG_BOX = DpSize(180.dp, 180.dp)
        private val ROW = DpSize(180.dp, 48.dp)
        private val LARGE_ROW = DpSize(300.dp, 48.dp)
        private val COLUMN = DpSize(48.dp, 180.dp)
        private val LARGE_COLUMN = DpSize(48.dp, 300.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(SMALL_BOX, BIG_BOX, ROW, LARGE_ROW, COLUMN, LARGE_COLUMN)
    )


    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetUI()
        }
    }

}

@Composable
@GlanceComposable
fun WidgetUI() {
    val periodPrediction = koinInject<IPeriodPrediction>()
    val predictedPeriodDate by periodPrediction.predictedPeriodDate.collectAsState()
    val daysUntilNextPeriod = formatDaysUntilPeriod(
        predictedPeriodDate,
        format = NextPeriodFormat.OnlyDays
    )

    Scaffold(
        backgroundColor = GlanceTheme.colors.widgetBackground,
        titleBar = {
            TitleBar(
                startIcon = ImageProvider(R.drawable.baseline_bloodtype_24),
                title = LocalContext.current.getString(R.string.app_name),
            )

        }
    ) {
        Text(
            text = daysUntilNextPeriod,
            modifier = GlanceModifier
                .padding(10.dp)
                .clickable(
                    actionStartActivity<MainActivity>()
                ).fillMaxSize(),
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            ),
        )
    }
}

private fun formatDaysUntilPeriod(
    date: LocalDate?,
    format: NextPeriodFormat
): String {
    val daysUntilNextPeriod = LocalDate.now().until(date).days
    return when (format) {
        NextPeriodFormat.MediumLengthText -> {
            if (date == null) {
                "Unknown"
            } else {
                "Period in $daysUntilNextPeriod days"
            }
        }
        NextPeriodFormat.OnlyDays -> {
            if (date == null) {
                "?"
            } else {
                "$daysUntilNextPeriod"
            }
        }
    }
}